package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.response.PagedResponse;
import fpt.project.NeoNHS.dto.response.notification.NotificationResponse;
import fpt.project.NeoNHS.entity.User;
import java.util.UUID;

public interface NotificationService {
    void createAndSendNotification(User user, String title, String message, String type, UUID referenceId);

    PagedResponse<NotificationResponse> getUserNotifications(String email, int page, int size);

    void markAsRead(UUID notificationId, String email);

    void markAllAsRead(String email);

    void saveDeviceToken(String email, String token);
}
