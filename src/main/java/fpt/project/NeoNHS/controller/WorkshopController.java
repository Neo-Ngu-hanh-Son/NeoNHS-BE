package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.dto.request.workshop.CreateWorkshopTemplateRequest;
import fpt.project.NeoNHS.dto.request.workshop.UpdateWorkshopTemplateRequest;

import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.workshop.WorkshopTemplateResponse;
import fpt.project.NeoNHS.enums.WorkshopStatus;
import fpt.project.NeoNHS.service.WorkshopSessionService;
import fpt.project.NeoNHS.service.WorkshopTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workshops")
@RequiredArgsConstructor
public class WorkshopController {

        private final WorkshopTemplateService workshopTemplateService;
        private final WorkshopSessionService workshopSessionService;

        // ==================== CREATE ====================

        @PostMapping("/templates")
        @PreAuthorize("hasRole('VENDOR')")
        public ResponseEntity<ApiResponse<WorkshopTemplateResponse>> createWorkshopTemplate(
                        @Valid @RequestBody CreateWorkshopTemplateRequest request,
                        Principal principal) {
                WorkshopTemplateResponse response = workshopTemplateService.createWorkshopTemplate(
                                principal.getName(),
                                request);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.success(HttpStatus.CREATED, "Workshop template created successfully",
                                                response));
        }

        // ==================== READ ====================

        @GetMapping("/templates/{id}")
        public ResponseEntity<ApiResponse<WorkshopTemplateResponse>> getWorkshopTemplateById(@PathVariable UUID id) {
                WorkshopTemplateResponse response = workshopTemplateService.getWorkshopTemplateById(id);
                return ResponseEntity
                                .ok(ApiResponse.success(HttpStatus.OK, "Workshop template retrieved successfully",
                                                response));
        }

        @GetMapping("/templates")
        public ResponseEntity<ApiResponse<List<WorkshopTemplateResponse>>> getAllWorkshopTemplates() {
                List<WorkshopTemplateResponse> response = workshopTemplateService.getAllWorkshopTemplates();
                return ResponseEntity
                                .ok(ApiResponse.success(HttpStatus.OK, "Workshop templates retrieved successfully",
                                                response));
        }

        // @GetMapping("/templates/status/{status}")
        // public ResponseEntity<ApiResponse<List<WorkshopTemplateResponse>>>
        // getWorkshopTemplatesByStatus(
        // @PathVariable WorkshopStatus status) {
        // List<WorkshopTemplateResponse> response =
        // workshopTemplateService.getWorkshopTemplatesByStatus(status);
        // return ResponseEntity
        // .ok(ApiResponse.success(HttpStatus.OK, "Workshop templates retrieved
        // successfully", response));
        // }

        @GetMapping("/templates/my")
        @PreAuthorize("hasRole('VENDOR')")
        public ResponseEntity<ApiResponse<List<WorkshopTemplateResponse>>> getMyWorkshopTemplates(Principal principal) {
                List<WorkshopTemplateResponse> response = workshopTemplateService
                                .getMyWorkshopTemplates(principal.getName());
                return ResponseEntity
                                .ok(ApiResponse.success(HttpStatus.OK, "Your workshop templates retrieved successfully",
                                                response));
        }

        // ==================== UPDATE ====================

        @PutMapping("/templates/{id}")
        @PreAuthorize("hasRole('VENDOR')")
        public ResponseEntity<ApiResponse<WorkshopTemplateResponse>> updateWorkshopTemplate(
                        @PathVariable UUID id,
                        @Valid @RequestBody UpdateWorkshopTemplateRequest request,
                        Principal principal) {
                WorkshopTemplateResponse response = workshopTemplateService.updateWorkshopTemplate(
                                principal.getName(),
                                id,
                                request);
                return ResponseEntity
                                .ok(ApiResponse.success(HttpStatus.OK, "Workshop template updated successfully",
                                                response));
        }

        // ==================== DELETE ====================

        @DeleteMapping("/templates/{id}")
        @PreAuthorize("hasRole('VENDOR')")
        public ResponseEntity<ApiResponse<Void>> deleteWorkshopTemplate(
                        @PathVariable UUID id,
                        Principal principal) {
                workshopTemplateService.deleteWorkshopTemplate(principal.getName(), id);
                return ResponseEntity
                                .ok(ApiResponse.success(HttpStatus.OK, "Workshop template deleted successfully", null));
        }
}
