package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.dto.request.notification.DeviceTokenRequest;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.PagedResponse;
import fpt.project.NeoNHS.dto.response.notification.NotificationResponse;
import fpt.project.NeoNHS.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "Notification API")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Get user notifications", description = "Get paginated notifications for current user")
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<NotificationResponse>>> getUserNotifications(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        return ResponseEntity.ok(ApiResponse.success("Notifications fetched successfully",
                notificationService.getUserNotifications(principal.getName(), page, size)));
    }

    @Operation(summary = "Mark notification as read")
    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable UUID id,
            Principal principal) {
        notificationService.markAsRead(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Marked as read successfully", null));
    }

    @Operation(summary = "Mark all notifications as read")
    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            Principal principal) {
        notificationService.markAllAsRead(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", null));
    }

    @Operation(summary = "Save push device token")
    @PostMapping("/push-token")
    public ResponseEntity<ApiResponse<Void>> savePushToken(
            Principal principal,
            @Valid @RequestBody DeviceTokenRequest request) {
        notificationService.saveDeviceToken(principal.getName(), request.getToken());
        return ResponseEntity.ok(ApiResponse.success("Token saved successfully", null));
    }
}
