package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.dto.chat.*;
import fpt.project.NeoNHS.dto.response.upload.ImageUploadResponse;
import fpt.project.NeoNHS.security.UserPrincipal;
import fpt.project.NeoNHS.service.ChatService;
import fpt.project.NeoNHS.service.ImageUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import fpt.project.NeoNHS.service.impl.CloudinaryImageUploadServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatRestController {

    private final ChatService chatService;
    private final CloudinaryImageUploadServiceImpl cloudinaryImageUploadService;

    /**
     * POST /api/chat/rooms
     * Create a new chat room or return existing one with the same participants.
     */
    @PostMapping("/rooms")
    public ResponseEntity<ChatRoomDTO> createChatRoom(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody CreateChatRoomRequest request) {

        ChatRoomDTO room = chatService.createChatRoom(
                currentUser.getId().toString(),
                request);

        return ResponseEntity.ok(room);
    }

    /**
     * GET /api/chat/rooms
     * List all chat rooms for the authenticated user.
     */
    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomDTO>> getUserChatRooms(
            @AuthenticationPrincipal UserPrincipal currentUser) {

        List<ChatRoomDTO> rooms = chatService.getUserChatRooms(
                currentUser.getId().toString());

        return ResponseEntity.ok(rooms);
    }

    /**
     * GET /api/chat/rooms/{roomId}/messages?page=0&size=20
     * Get paginated messages for a specific chat room.
     */
    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<Page<ChatMessageDTO>> getRoomMessages(
            @PathVariable String roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<ChatMessageDTO> messages = chatService.getRoomMessages(roomId, pageable);

        return ResponseEntity.ok(messages);
    }

    /**
     * GET /api/chat/users/{userId}
     * Get user information for chat participants.
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<ChatUserDTO> getChatUserInfo(@PathVariable String userId) {
        ChatUserDTO userInfo = chatService.getChatUserInfo(userId);
        return ResponseEntity.ok(userInfo);
    }

    /**
     * PATCH /api/chat/rooms/{roomId}/visibility
     * Hide/Archive a chat room (Swipe action).
     */
    @PatchMapping("/rooms/{roomId}/visibility")
    public ResponseEntity<?> toggleVisibility(
            @PathVariable String roomId,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody ToggleVisibilityRequest request) {

        chatService.toggleVisibility(roomId, currentUser.getId().toString(), request.isHidden());
        return ResponseEntity.ok().build();
    }

    /**
     * POST /api/chat/media
     * Upload an image before sending an IMAGE type message.
     */
    @PostMapping(value = "/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageUploadResponse> uploadMedia(@RequestParam("file") MultipartFile file) {
        ImageUploadResponse res = cloudinaryImageUploadService.uploadImage(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }
}
