package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.request.workshop.CreateWorkshopTemplateRequest;
import fpt.project.NeoNHS.dto.request.workshop.UpdateWorkshopTemplateRequest;
import fpt.project.NeoNHS.dto.response.workshop.WorkshopTemplateResponse;
import fpt.project.NeoNHS.enums.WorkshopStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    // Update
    WorkshopTemplateResponse updateWorkshopTemplate(String email, UUID id, UpdateWorkshopTemplateRequest request);

    // Delete
    void deleteWorkshopTemplate(String email, UUID id);
}
