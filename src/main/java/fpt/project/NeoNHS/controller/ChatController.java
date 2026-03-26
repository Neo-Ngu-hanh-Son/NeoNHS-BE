package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.dto.chat.ChatMessageDTO;
import fpt.project.NeoNHS.dto.chat.ChatMessageRequest;
import fpt.project.NeoNHS.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Handle incoming chat messages via STOMP.
     * Client sends to: /app/chat.send
     * Message is saved to MongoDB, then broadcast to both sender and receiver.
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageRequest request, Principal principal) {
        String senderId = principal.getName();
        log.info("Message from {} to {}: {}", senderId, request.getReceiverId(), request.getContent());

        // Save to MongoDB and get the DTO
        ChatMessageDTO savedMessage = chatService.sendMessage(senderId, request);

        // Send to the receiver's personal queue
        messagingTemplate.convertAndSendToUser(
                request.getReceiverId(),
                "/queue/messages",
                savedMessage);

        // Also send back to the sender's queue (for multi-device sync / confirmation)
        messagingTemplate.convertAndSendToUser(
                senderId,
                "/queue/messages",
                savedMessage);
    }
}
