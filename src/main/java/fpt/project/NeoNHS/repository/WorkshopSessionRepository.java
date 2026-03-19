package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.WorkshopSession;
import fpt.project.NeoNHS.enums.SessionStatus;
import fpt.project.NeoNHS.repository.projection.VendorCountProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface WorkshopSessionRepository extends JpaRepository<WorkshopSession, UUID>, JpaSpecificationExecutor<WorkshopSession> {

    Page<WorkshopSession> findByWorkshopTemplateId(UUID templateId, Pageable pageable);

    Page<WorkshopSession> findByWorkshopTemplateVendorId(UUID vendorId, Pageable pageable);

    Page<WorkshopSession> findByWorkshopTemplateVendorUserEmail(String email, Pageable pageable);

    Page<WorkshopSession> findByStatusAndStartTimeAfter(SessionStatus status, LocalDateTime startTime, Pageable pageable);
    List<WorkshopSession> findByStatusAndStartTimeAfter(SessionStatus status, LocalDateTime startTime);

    @Query("""
        SELECT wt.vendor.id AS vendorId, COUNT(ws) AS count
        FROM WorkshopSession ws
        JOIN ws.workshopTemplate wt
        WHERE ws.deletedAt IS NULL
          AND wt.vendor.id IN :vendorIds
        GROUP BY wt.vendor.id
    """)
    List<VendorCountProjection> countSessionsByVendorIds(@Param("vendorIds") List<UUID> vendorIds);

    @Query("""
        SELECT ws FROM WorkshopSession ws
        JOIN FETCH ws.workshopTemplate wt
        WHERE wt.vendor.id = :vendorId
          AND ws.startTime >= :from
          AND ws.startTime < :to
          AND ws.deletedAt IS NULL
        ORDER BY ws.startTime ASC
    """)
    List<WorkshopSession> findByVendorIdAndStartTimeBetween(
            @Param("vendorId") UUID vendorId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    @Query("""
        SELECT COUNT(ws) FROM WorkshopSession ws
        JOIN ws.workshopTemplate wt
        WHERE wt.vendor.id = :vendorId
          AND ws.deletedAt IS NULL
          AND ws.status IN (fpt.project.NeoNHS.enums.SessionStatus.SCHEDULED, fpt.project.NeoNHS.enums.SessionStatus.ONGOING)
          AND ws.currentEnrolled > 0
    """)
    long countBookingsByVendorId(@Param("vendorId") UUID vendorId);

    @Query("""
        SELECT COUNT(ws) FROM WorkshopSession ws
        JOIN ws.workshopTemplate wt
        WHERE wt.vendor.id = :vendorId
          AND ws.deletedAt IS NULL
          AND ws.status IN (fpt.project.NeoNHS.enums.SessionStatus.SCHEDULED, fpt.project.NeoNHS.enums.SessionStatus.ONGOING)
          AND ws.currentEnrolled > 0
          AND ws.createdAt >= :since
    """)
    long countBookingsByVendorIdSince(@Param("vendorId") UUID vendorId, @Param("since") LocalDateTime since);
}
