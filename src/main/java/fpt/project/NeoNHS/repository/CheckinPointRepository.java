package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.CheckinPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CheckinPointRepository extends JpaRepository<CheckinPoint, UUID>, JpaSpecificationExecutor<CheckinPoint> {
    @Query("""
            SELECT cp
            FROM CheckinPoint cp
            WHERE cp.id IN :ids
            AND NOT EXISTS (
                SELECT 1
                FROM UserCheckIn uc
                WHERE uc.checkinPoint.id = cp.id
                AND uc.user.id = :userId
            )
            """)
    List<CheckinPoint> findAllCheckinPointThatUserNotCheckined(List<UUID> ids, UUID userId);
}
