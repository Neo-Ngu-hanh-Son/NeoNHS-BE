package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.chat.ChatMessageDTO;
import fpt.project.NeoNHS.dto.chat.ChatMessageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChatService {

    /**
     * Save a chat message to MongoDB and return the DTO for broadcasting.
     */
    ChatMessageDTO sendMessage(String senderId, ChatMessageRequest request);

    /**
     * Retrieve paginated chat history between two users.
     */
    Page<ChatMessageDTO> getChatHistory(String userId1, String userId2, Pageable pageable);
}
