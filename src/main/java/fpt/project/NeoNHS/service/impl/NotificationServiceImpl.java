package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.dto.response.PagedResponse;
import fpt.project.NeoNHS.dto.response.notification.NotificationResponse;
import fpt.project.NeoNHS.entity.Notification;
import fpt.project.NeoNHS.entity.User;
import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.repository.NotificationRepository;
import fpt.project.NeoNHS.repository.UserRepository;
import fpt.project.NeoNHS.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ExpoPushService expoPushService;

    @Override
    public void createAndSendNotification(User user, String title, String message, String type, UUID referenceId) {
        // 1. Lưu DB
        Notification notif = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .referenceId(referenceId)
                .isRead(false)
                .build();
        notif = notificationRepository.save(notif);

        // 2. Bắn Real-time qua WebSocket (cho Web & App)
        messagingTemplate.convertAndSendToUser(
                user.getId().toString(),
                "/queue/notifications",
                NotificationResponse.fromEntity(notif));

        // 3. Bắn Push Notification qua Expo (cho App khi tắt)
        if (user.getDeviceTokens() != null && !user.getDeviceTokens().isEmpty()) {
            Map<String, Object> data = new HashMap<>();
            data.put("notificationId", notif.getId());
            data.put("type", type);
            if (referenceId != null)
                data.put("referenceId", referenceId.toString());

            expoPushService.sendPushNotification(user.getDeviceTokens(), title, message, data);
        }
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    @Override
    public PagedResponse<NotificationResponse> getUserNotifications(String email, int page, int size) {
        User user = getUserByEmail(email);
        Page<Notification> notificationPage = notificationRepository.findByUserOrderByCreatedAtDesc(user,
                PageRequest.of(page, size));

        List<NotificationResponse> content = notificationPage.getContent().stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());

        return PagedResponse.<NotificationResponse>builder()
                .content(content)
                .page(notificationPage.getNumber())
                .size(notificationPage.getSize())
                .totalElements(notificationPage.getTotalElements())
                .totalPages(notificationPage.getTotalPages())
                .first(notificationPage.isFirst())
                .last(notificationPage.isLast())
                .empty(notificationPage.isEmpty())
                .build();
    }

    @Override
    public void markAsRead(UUID notificationId, String email) {
        User user = getUserByEmail(email);
        Notification notif = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId.toString()));

        if (!notif.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You don't have permission to mark this notification");
        }

        notif.setIsRead(true);
        notificationRepository.save(notif);
    }

    @Override
    public void markAllAsRead(String email) {
        User user = getUserByEmail(email);
        List<Notification> unreadNotifs = notificationRepository.findByUserAndIsReadFalse(user);

        unreadNotifs.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unreadNotifs);
    }

    @Override
    public void saveDeviceToken(String email, String token) {
        User user = getUserByEmail(email);
        if (user.getDeviceTokens() != null && !user.getDeviceTokens().contains(token)) {
            user.getDeviceTokens().add(token);
            userRepository.save(user);
        } else if (user.getDeviceTokens() == null) {
            user.setDeviceTokens(new java.util.ArrayList<>(List.of(token)));
            userRepository.save(user);
        }
    }
}
