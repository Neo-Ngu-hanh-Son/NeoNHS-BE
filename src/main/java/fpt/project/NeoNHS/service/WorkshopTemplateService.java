package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.request.workshop.CreateWorkshopTemplateRequest;
import fpt.project.NeoNHS.dto.request.workshop.UpdateWorkshopTemplateRequest;
import fpt.project.NeoNHS.dto.response.workshop.WorkshopTemplateResponse;
import fpt.project.NeoNHS.enums.WorkshopStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface WorkshopTemplateService {

    // Create
    WorkshopTemplateResponse createWorkshopTemplate(String email, CreateWorkshopTemplateRequest request);

    // Read
    WorkshopTemplateResponse getWorkshopTemplateById(UUID id);

    List<WorkshopTemplateResponse> getAllWorkshopTemplates();

    Page<WorkshopTemplateResponse> getAllWorkshopTemplates(Pageable pageable);

    List<WorkshopTemplateResponse> getMyWorkshopTemplates(String email);

    Page<WorkshopTemplateResponse> getMyWorkshopTemplates(String email, Pageable pageable);

    Page<WorkshopTemplateResponse> getWorkshopTemplatesByVendorId(UUID vendorId, Pageable pageable);

    // ==================== TOURIST ====================

    WorkshopTemplateResponse getActiveWorkshopTemplateById(UUID id);

    Page<WorkshopTemplateResponse> getActiveWorkshopTemplates(Pageable pageable);

    Page<WorkshopTemplateResponse> searchAndFilterActiveTemplates(
            String keyword,
            UUID tagId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Integer minDuration,
            Integer maxDuration,
            BigDecimal minRating,
            Pageable pageable
    );

    // Search & Filter (internal/admin)
    List<WorkshopTemplateResponse> searchWorkshopTemplates(
            String keyword,
            String name,
            WorkshopStatus status,
            UUID vendorId,
            UUID tagId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Integer minDuration,
            Integer maxDuration,
            BigDecimal minRating
    );

    // Update
    WorkshopTemplateResponse updateWorkshopTemplate(String email, UUID id, UpdateWorkshopTemplateRequest request);

    // Register/Submit for Approval
    WorkshopTemplateResponse registerWorkshopTemplate(String email, UUID id);

    // Approve/Reject (Admin only)
    WorkshopTemplateResponse approveWorkshopTemplate(String adminEmail, UUID id, String adminNote);

    WorkshopTemplateResponse rejectWorkshopTemplate(String adminEmail, UUID id, String adminNote);

    // Toggle Publish
    WorkshopTemplateResponse togglePublishWorkshopTemplate(String email, UUID id);

    // Delete
    void deleteWorkshopTemplate(String email, UUID id);
}
