package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.chat.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ChatService {

    /**
     * Create a new chat room. If a room with the exact same participants
     * already exists, return the existing one instead.
     */
    ChatRoomDTO createChatRoom(String creatorId, CreateChatRoomRequest request);

    /**
     * Get all chat rooms for a user, ordered by most recent activity.
     */
    List<ChatRoomDTO> getUserChatRooms(String userId);

    /**
     * Send a message to a chat room. Validates that the sender is a participant.
     */
    ChatMessageDTO sendMessage(String senderId, ChatMessageRequest request);

    /**
     * Get paginated messages for a specific chat room.
     */
    Page<ChatMessageDTO> getRoomMessages(String chatRoomId, Pageable pageable);

    /**
     * Get the participant list for a room (used by controller for broadcasting).
     */
    List<String> getRoomParticipants(String chatRoomId);
    
    /**
     * Get user information for chat participant
     */
    ChatUserDTO getChatUserInfo(String userId);
}
