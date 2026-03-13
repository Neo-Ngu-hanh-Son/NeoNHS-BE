package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.CheckinImage;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CheckinImageRepository extends JpaRepository<CheckinImage, UUID> {

    @EntityGraph(attributePaths = {
            "userCheckIn",
            "userCheckIn.checkinPoint",
            "userCheckIn.checkinPoint.point",
            "userCheckIn.checkinPoint.point.attraction"
    })
    List<CheckinImage> findAllByUserCheckIn_User_IdOrderByCreatedAtDesc(UUID userId);
}
