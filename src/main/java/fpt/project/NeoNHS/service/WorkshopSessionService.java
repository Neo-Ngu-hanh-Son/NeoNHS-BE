package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.request.workshop.CreateWorkshopSessionRequest;
import fpt.project.NeoNHS.dto.request.workshop.UpdateWorkshopSessionRequest;
import fpt.project.NeoNHS.dto.response.workshop.WorkshopSessionResponse;
import fpt.project.NeoNHS.entity.WorkshopSession;
import fpt.project.NeoNHS.enums.SessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface WorkshopSessionService {

    // ==================== CREATE ====================
    WorkshopSessionResponse createWorkshopSession(String email, CreateWorkshopSessionRequest request);

    List<WorkshopSessionResponse> createWorkshopSessionBatch(String email, List<CreateWorkshopSessionRequest> requests);

    // ==================== READ ====================
    WorkshopSessionResponse getWorkshopSessionById(UUID id);

    Page<WorkshopSessionResponse> getAllUpcomingSessions(Pageable pageable);

    Page<WorkshopSessionResponse> getMyWorkshopSessions(String email, Pageable pageable);

    Page<WorkshopSessionResponse> getSessionsByTemplateId(UUID templateId, Pageable pageable);

    // ==================== TOURIST ====================
    Page<WorkshopSessionResponse> getUpcomingSessionsByTemplateId(UUID templateId, Pageable pageable);

    // ==================== SEARCH & FILTER ====================
    Page<WorkshopSessionResponse> searchWorkshopSessions(
            String keyword,
            UUID vendorId,
            UUID tagId,
            SessionStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean availableOnly,
            Pageable pageable
    );

    // ==================== UPDATE ====================
    WorkshopSessionResponse updateWorkshopSession(String email, UUID id, UpdateWorkshopSessionRequest request);

    WorkshopSessionResponse updateWorkshopSessionStatus(String email, UUID id, SessionStatus status);

    // ==================== DELETE ====================
    void deleteWorkshopSession(String email, UUID id);

    WorkshopSessionResponse cancelWorkshopSession(String email, UUID id);

    // ==================== FINANCIAL / HELPERS ====================
    void creditVendorWallet(WorkshopSession session);
}
