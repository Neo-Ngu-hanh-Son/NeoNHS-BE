package fpt.project.NeoNHS.repository;

import fpt.project.NeoNHS.entity.WorkshopSession;
import fpt.project.NeoNHS.enums.SessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface WorkshopSessionRepository extends JpaRepository<WorkshopSession, UUID>, JpaSpecificationExecutor<WorkshopSession> {

    Page<WorkshopSession> findByWorkshopTemplateId(UUID templateId, Pageable pageable);

    Page<WorkshopSession> findByWorkshopTemplateVendorId(UUID vendorId, Pageable pageable);

    Page<WorkshopSession> findByWorkshopTemplateVendorUserEmail(String email, Pageable pageable);

    Page<WorkshopSession> findByStatusAndStartTimeAfter(SessionStatus status, LocalDateTime startTime, Pageable pageable);
}
