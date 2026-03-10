package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.EventImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventImageRepository extends JpaRepository<EventImage, UUID> {

    Optional<EventImage> findByEventIdAndIsThumbnailTrue(UUID eventId);

    List<EventImage> findByEventIdAndDeletedAtIsNull(UUID eventId);
}
