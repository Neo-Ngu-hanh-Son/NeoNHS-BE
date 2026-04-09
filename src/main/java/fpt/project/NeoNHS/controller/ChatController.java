package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.dto.chat.ChatMessageDTO;
import fpt.project.NeoNHS.dto.chat.ChatMessageRequest;
import fpt.project.NeoNHS.dto.chat.ReadReceiptRequest;
import fpt.project.NeoNHS.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Handle incoming chat messages via STOMP.
     * Client sends to: /app/chat.send
     * Message is saved to MongoDB, then broadcast to ALL participants in the room.
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageRequest request, Principal principal) {
        String senderId = principal.getName();
        log.info("Message in room {} from {}: {}", request.getChatRoomId(), senderId, request.getContent());

        // Save to MongoDB and get the DTO
        ChatMessageDTO savedMessage = chatService.sendMessage(senderId, request);

        // Get all participants in the room
        List<String> participants = chatService.getRoomParticipants(request.getChatRoomId());

        // Broadcast to ALL participants (including sender for multi-device sync)
        for (String participantId : participants) {
            messagingTemplate.convertAndSendToUser(
                    participantId,
                    "/queue/messages",
                    savedMessage);
        }
    }

    @MessageMapping("/chat.typing.start")
    public void startTyping(@Payload Map<String, String> payload, Principal principal) {
        String chatRoomId = payload.get("chatRoomId");
        Map<String, Object> response = new HashMap<>();
        response.put("isTyping", true);
        response.put("senderId", principal.getName());
        messagingTemplate.convertAndSend("/topic/room/" + chatRoomId + "/typing", (Object) response);
    }

    @MessageMapping("/chat.typing.stop")
    public void stopTyping(@Payload Map<String, String> payload, Principal principal) {
        String chatRoomId = payload.get("chatRoomId");
        Map<String, Object> response = new HashMap<>();
        response.put("isTyping", false);
        response.put("senderId", principal.getName());
        messagingTemplate.convertAndSend("/topic/room/" + chatRoomId + "/typing", (Object) response);
    }

    @MessageMapping("/chat.read")
    public void markAsRead(@Payload ReadReceiptRequest request, Principal principal) {
        chatService.markAsRead(request.getChatRoomId(), principal.getName(), request.getLastReadMessageId());

        // Notify other participants that this user read the messages
        Map<String, Object> receipt = new HashMap<>();
        receipt.put("type", "READ_RECEIPT");
        receipt.put("readerId", principal.getName());
        receipt.put("lastReadMessageId", request.getLastReadMessageId());
        receipt.put("chatRoomId", request.getChatRoomId());

        List<String> participants = chatService.getRoomParticipants(request.getChatRoomId());
        for (String participantId : participants) {
            messagingTemplate.convertAndSendToUser(
                    participantId,
                    "/queue/messages",
                    receipt);
        }
    }
}
