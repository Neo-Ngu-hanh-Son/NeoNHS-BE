package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.document.ChatMessage;
import fpt.project.NeoNHS.document.ChatRoom;
import fpt.project.NeoNHS.dto.chat.*;
import fpt.project.NeoNHS.enums.MessageStatus;
import fpt.project.NeoNHS.enums.UserRole;
import fpt.project.NeoNHS.repository.mongo.ChatMessageRepository;
import fpt.project.NeoNHS.repository.mongo.ChatRoomRepository;
import fpt.project.NeoNHS.repository.UserRepository;
import fpt.project.NeoNHS.repository.VendorProfileRepository;
import fpt.project.NeoNHS.entity.User;
import fpt.project.NeoNHS.entity.VendorProfile;
import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final VendorProfileRepository vendorProfileRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    public ChatRoomDTO createChatRoom(String creatorId, CreateChatRoomRequest request) {
        // 1. Khởi tạo an toàn để tránh NullPointerException nếu Frontend không gửi
        // participantIds
        List<String> participants = new ArrayList<>();
        if (request.getParticipantIds() != null) {
            participants.addAll(request.getParticipantIds());
        }

        // 2. Đảm bảo người tạo (creator) luôn có trong phòng
        if (!participants.contains(creatorId)) {
            participants.add(creatorId);
        }

        // 3. Auto-assign Admin cho luồng Hỗ trợ hệ thống
        if ("SYSTEM_SUPPORT".equals(request.getRoomType())) {
            // Lấy ID Admin an toàn, ném lỗi rõ ràng nếu DB chưa có Admin
            String adminId = userRepository.findFirstByRole(UserRole.ADMIN)
                    .orElseThrow(() -> new RuntimeException("System Error: No admin found to handle support"))
                    .getId().toString(); // Đảm bảo ép về String nếu ID là ObjectId

            if (!participants.contains(adminId)) {
                participants.add(adminId);
            }
        }

        // 3b. Auto-resolve Vendor Profile ID → User ID cho luồng VENDOR_CHAT
        if ("VENDOR_CHAT".equals(request.getRoomType())) {
            List<String> resolvedParticipants = new ArrayList<>();
            for (String pid : participants) {
                if (pid.equals(creatorId)) {
                    // Creator is always a user ID, keep as-is
                    resolvedParticipants.add(pid);
                    continue;
                }
                try {
                    // Try to find a VendorProfile with this ID
                    java.util.UUID vendorProfileUuid = java.util.UUID.fromString(pid);
                    Optional<VendorProfile> vendorProfile = vendorProfileRepository.findById(vendorProfileUuid);
                    if (vendorProfile.isPresent()) {
                        // Resolve to the actual user ID owning this vendor profile
                        String vendorUserId = vendorProfile.get().getUser().getId().toString();
                        resolvedParticipants.add(vendorUserId);
                        log.info("VENDOR_CHAT: Resolved vendor profile {} -> user {}", pid, vendorUserId);
                    } else {
                        // Not a vendor profile ID, assume it's already a user ID
                        resolvedParticipants.add(pid);
                    }
                } catch (IllegalArgumentException e) {
                    // Not a valid UUID, keep as-is
                    resolvedParticipants.add(pid);
                }
            }
            participants = resolvedParticipants;
        }

        // 4. Sort participants để query trùng lặp (dedup) chính xác
        List<String> sortedParticipants = new ArrayList<>(participants);
        Collections.sort(sortedParticipants);

        // 5. Kiểm tra phòng đã tồn tại
        Optional<ChatRoom> existingRoom = chatRoomRepository
                .findByExactParticipants(sortedParticipants, sortedParticipants.size());

        if (existingRoom.isPresent()) {
            log.info("Returning existing chat room: {}", existingRoom.get().getId());

            // Xử lý unhide (nếu phòng đang bị ẩn thì hiện lại)
            ChatRoom room = existingRoom.get();
            if (room.getHiddenBy() != null && room.getHiddenBy().contains(creatorId)) {
                room.getHiddenBy().remove(creatorId);
                chatRoomRepository.save(room);
            }

            return toRoomDTO(room, creatorId);
        }

        // 6. Tạo phòng mới
        ChatRoom room = ChatRoom.builder()
                .name(request.getName())
                .participants(sortedParticipants)
                .roomType(request.getRoomType() == null ? "STANDARD" : request.getRoomType())
                .createdAt(LocalDateTime.now())
                .build();

        ChatRoom saved = chatRoomRepository.save(room);
        log.info("Created new chat room {} with {} participants", saved.getId(), participants.size());

        return toRoomDTO(saved, creatorId);
    }

    @Override
    public List<ChatRoomDTO> getUserChatRooms(String userId) {
        return chatRoomRepository
                .findByParticipantsContainingOrderByLastMessageAtDesc(userId)
                .stream()
                .map(room -> toRoomDTO(room, userId))
                .toList();
    }

    @Override
    public ChatMessageDTO sendMessage(String senderId, ChatMessageRequest request) {
        // Validate that the sender is a participant in the room
        ChatRoom room = chatRoomRepository.findById(request.getChatRoomId())
                .orElseThrow(() -> new RuntimeException("Chat room not found: " + request.getChatRoomId()));

        if (!room.getParticipants().contains(senderId)) {
            throw new RuntimeException("User " + senderId + " is not a participant in room " + request.getChatRoomId());
        }

        // Save the message
        ChatMessage message = ChatMessage.builder()
                .chatRoomId(request.getChatRoomId())
                .senderId(senderId)
                .content(request.getContent())
                .timestamp(LocalDateTime.now())
                .status(MessageStatus.SENT)
                .messageType(request.getMessageType() == null ? "TEXT" : request.getMessageType())
                .mediaUrl(request.getMediaUrl())
                .metadata(request.getMetadata())
                .build();

        ChatMessage saved = chatMessageRepository.save(message);

        // Update the room's last message info and unread counts
        String preview = request.getContent();
        if (preview != null && preview.length() > 100) {
            preview = preview.substring(0, 100) + "...";
        }
        room.setLastMessageAt(saved.getTimestamp());
        room.setLastMessagePreview(preview);
        room.setLastMessageSenderId(senderId);

        // Unhide room and increment unread count for everyone except the sender
        for (String participantId : room.getParticipants()) {
            if (!participantId.equals(senderId)) {
                room.getUnreadCounts().put(participantId, room.getUnreadCounts().getOrDefault(participantId, 0) + 1);
                room.getHiddenBy().remove(participantId);
            }
        }

        chatRoomRepository.save(room);

        log.info("Message saved in room {}: {} -> {}", request.getChatRoomId(), senderId, request.getContent());

        return toMessageDTO(saved);
    }

    @Override
    public Page<ChatMessageDTO> getRoomMessages(String chatRoomId, Pageable pageable) {
        return chatMessageRepository.findByChatRoomId(chatRoomId, pageable)
                .map(this::toMessageDTO);
    }

    @Override
    public List<String> getRoomParticipants(String chatRoomId) {
        return chatRoomRepository.findById(chatRoomId)
                .map(ChatRoom::getParticipants)
                .orElse(List.of());
    }

    @Override
    public void toggleVisibility(String roomId, String userId, boolean isHidden) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found: " + roomId));

        if (isHidden) {
            if (!room.getHiddenBy().contains(userId)) {
                room.getHiddenBy().add(userId);
            }
        } else {
            room.getHiddenBy().remove(userId);
        }
        chatRoomRepository.save(room);
    }

    @Override
    public void markAsRead(String roomId, String userId, String lastReadMessageId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found: " + roomId));

        // 1. Reset unread counts
        room.getUnreadCounts().put(userId, 0);
        chatRoomRepository.save(room);

        // 2. Perform bulk update in Mongo
        Query query = new Query(Criteria.where("chatRoomId").is(roomId)
                .and("status").ne(MessageStatus.READ)
                .and("_id").lte(lastReadMessageId)
                .and("senderId").ne(userId));

        Update update = new Update().set("status", MessageStatus.READ);
        mongoTemplate.updateMulti(query, update, ChatMessage.class);

        log.info("Marked messages as READ in room {} up to count for user {}", roomId, userId);
    }

    private ChatMessageDTO toMessageDTO(ChatMessage message) {
        return ChatMessageDTO.builder()
                .id(message.getId())
                .chatRoomId(message.getChatRoomId())
                .senderId(message.getSenderId())
                .content(message.getContent())
                .timestamp(message.getTimestamp())
                .status(message.getStatus())
                .messageType(message.getMessageType())
                .mediaUrl(message.getMediaUrl())
                .metadata(message.getMetadata())
                .build();
    }

    private ChatRoomDTO toRoomDTO(ChatRoom room, String requesterId) {
        return ChatRoomDTO.builder()
                .id(room.getId())
                .name(room.getName())
                .participants(room.getParticipants())
                .createdAt(room.getCreatedAt())
                .lastMessageAt(room.getLastMessageAt())
                .lastMessagePreview(room.getLastMessagePreview())
                .lastMessageSenderId(room.getLastMessageSenderId())
                .roomType(room.getRoomType())
                .unreadCount(room.getUnreadCounts().getOrDefault(requesterId, 0))
                .isHidden(room.getHiddenBy().contains(requesterId))
                .build();
    }

    @Override
    public ChatUserDTO getChatUserInfo(String userId) {
        User user = userRepository.findById(java.util.UUID.fromString(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return ChatUserDTO.builder()
                .id(user.getId().toString())
                .fullname(user.getFullname())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .build();
    }
}
