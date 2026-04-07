package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.Notification;
import fpt.project.NeoNHS.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Page<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    List<Notification> findByUserAndIsReadFalse(User user);
}
