package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.security.UserPrincipal;
import fpt.project.NeoNHS.service.AiChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * REST controller for AI chatbot interactions.
 * Provides SSE streaming endpoint for real-time AI responses.
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class AiChatController {

    private final AiChatService aiChatService;

    /**
     * GET /api/chat/rooms/{roomId}/ai-reply?message=...
     * <p>
     * Streams AI response as Server-Sent Events.
     * Events:
     * - "message": partial text chunk { "text": "..." }
     * - "done": completion signal { "fullText": "..." }
     * - "transfer": handover to admin { "message": "..." }
     * - "error": error occurred { "error": "..." }
     */
    @GetMapping(value = "/rooms/{roomId}/ai-reply", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter getAiReply(
            @PathVariable String roomId,
            @RequestParam String message,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        log.info("[AiChat] User {} asking AI in room {}: {}", currentUser.getId(), roomId, message);
    
        return aiChatService.streamAiReply(
                roomId,
                currentUser.getId().toString(),
                message);
    }
}
