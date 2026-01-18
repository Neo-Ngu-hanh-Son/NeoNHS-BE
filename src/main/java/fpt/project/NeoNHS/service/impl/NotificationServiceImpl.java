package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.repository.NotificationRepository;
import fpt.project.NeoNHS.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
}
