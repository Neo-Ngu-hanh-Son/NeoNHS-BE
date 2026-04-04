package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.document.ChatMessage;
import fpt.project.NeoNHS.document.ChatRoom;
import fpt.project.NeoNHS.dto.chat.*;
import fpt.project.NeoNHS.enums.MessageStatus;
import fpt.project.NeoNHS.repository.mongo.ChatMessageRepository;
import fpt.project.NeoNHS.repository.mongo.ChatRoomRepository;
import fpt.project.NeoNHS.repository.UserRepository;
import fpt.project.NeoNHS.entity.User;
import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Override
    public ChatRoomDTO createChatRoom(String creatorId, CreateChatRoomRequest request) {
        // Ensure the creator is included in participants
        List<String> participants = new ArrayList<>(request.getParticipantIds());
        if (!participants.contains(creatorId)) {
            participants.add(creatorId);
        }

        // Sort participants for consistent dedup matching
        List<String> sortedParticipants = new ArrayList<>(participants);
        Collections.sort(sortedParticipants);

        // Check if a room with exactly these participants already exists
        Optional<ChatRoom> existingRoom = chatRoomRepository
                .findByExactParticipants(sortedParticipants, sortedParticipants.size());

        if (existingRoom.isPresent()) {
            log.info("Returning existing chat room: {}", existingRoom.get().getId());
            return toRoomDTO(existingRoom.get());
        }

        // Create new room
        ChatRoom room = ChatRoom.builder()
                .name(request.getName())
                .participants(sortedParticipants)
                .createdAt(LocalDateTime.now())
                .build();

        ChatRoom saved = chatRoomRepository.save(room);
        log.info("Created chat room {} with {} participants", saved.getId(), participants.size());

        return toRoomDTO(saved);
    }

    @Override
    public List<ChatRoomDTO> getUserChatRooms(String userId) {
        return chatRoomRepository
                .findByParticipantsContainingOrderByLastMessageAtDesc(userId)
                .stream()
                .map(this::toRoomDTO)
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
                .build();

        ChatMessage saved = chatMessageRepository.save(message);

        // Update the room's last message info
        String preview = request.getContent();
        if (preview != null && preview.length() > 100) {
            preview = preview.substring(0, 100) + "...";
        }
        room.setLastMessageAt(saved.getTimestamp());
        room.setLastMessagePreview(preview);
        room.setLastMessageSenderId(senderId);
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

    private ChatMessageDTO toMessageDTO(ChatMessage message) {
        return ChatMessageDTO.builder()
                .id(message.getId())
                .chatRoomId(message.getChatRoomId())
                .senderId(message.getSenderId())
                .content(message.getContent())
                .timestamp(message.getTimestamp())
                .status(message.getStatus())
                .build();
    }

    private ChatRoomDTO toRoomDTO(ChatRoom room) {
        return ChatRoomDTO.builder()
                .id(room.getId())
                .name(room.getName())
                .participants(room.getParticipants())
                .createdAt(room.getCreatedAt())
                .lastMessageAt(room.getLastMessageAt())
                .lastMessagePreview(room.getLastMessagePreview())
                .lastMessageSenderId(room.getLastMessageSenderId())
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
