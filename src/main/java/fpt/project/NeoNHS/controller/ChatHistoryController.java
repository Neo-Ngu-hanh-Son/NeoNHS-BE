package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.dto.chat.ChatMessageDTO;
import fpt.project.NeoNHS.security.UserPrincipal;
import fpt.project.NeoNHS.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatHistoryController {

    private final ChatService chatService;

    /**
     * GET /api/chat/history?otherUserId=<UUID>&page=0&size=20
     * Returns paginated chat history between the authenticated user and another
     * user.
     * Messages are sorted by timestamp descending (newest first).
     */
    @GetMapping("/history")
    public ResponseEntity<Page<ChatMessageDTO>> getChatHistory(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam String otherUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));

        Page<ChatMessageDTO> history = chatService.getChatHistory(
                currentUser.getId().toString(),
                otherUserId,
                pageable);

        return ResponseEntity.ok(history);
    }
}
