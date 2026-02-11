package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.request.workshop.CreateWorkshopSessionRequest;
import fpt.project.NeoNHS.dto.request.workshop.UpdateWorkshopSessionRequest;
import fpt.project.NeoNHS.dto.response.workshop.WorkshopSessionResponse;
import fpt.project.NeoNHS.enums.SessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public interface WorkshopSessionService {

    // ==================== CREATE ====================
    WorkshopSessionResponse createWorkshopSession(String email, CreateWorkshopSessionRequest request);

    // ==================== READ ====================
    WorkshopSessionResponse getWorkshopSessionById(UUID id);

    Page<WorkshopSessionResponse> getAllUpcomingSessions(Pageable pageable);

    Page<WorkshopSessionResponse> getMyWorkshopSessions(String email, Pageable pageable);

    Page<WorkshopSessionResponse> getSessionsByTemplateId(UUID templateId, Pageable pageable);

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

    // ==================== DELETE ====================
    void deleteWorkshopSession(String email, UUID id);

    WorkshopSessionResponse cancelWorkshopSession(String email, UUID id);
}
