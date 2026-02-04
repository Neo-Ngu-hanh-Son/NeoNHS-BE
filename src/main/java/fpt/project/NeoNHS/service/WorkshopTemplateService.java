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

    Page<WorkshopTemplateResponse> getAllWorkshopTemplates(Pageable pageable);

    Page<WorkshopTemplateResponse> getWorkshopTemplatesByStatus(WorkshopStatus status, Pageable pageable);

    List<WorkshopTemplateResponse> getMyWorkshopTemplates(String email);

    // Search & Filter with Pagination
    Page<WorkshopTemplateResponse> searchWorkshopTemplates(
            String keyword,
            String name,
            WorkshopStatus status,
            UUID vendorId,
            UUID tagId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Integer minDuration,
            Integer maxDuration,
            BigDecimal minRating,
            Pageable pageable
    );

    // Update
    WorkshopTemplateResponse updateWorkshopTemplate(String email, UUID id, UpdateWorkshopTemplateRequest request);

    // Delete
    void deleteWorkshopTemplate(String email, UUID id);
}
