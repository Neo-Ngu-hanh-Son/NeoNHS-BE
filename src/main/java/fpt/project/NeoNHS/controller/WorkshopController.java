package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.constants.PaginationConstants;
import fpt.project.NeoNHS.dto.request.workshop.CreateWorkshopTemplateRequest;
import fpt.project.NeoNHS.dto.request.workshop.UpdateWorkshopTemplateRequest;

import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.workshop.WorkshopTemplateResponse;
import fpt.project.NeoNHS.enums.WorkshopStatus;
import fpt.project.NeoNHS.service.WorkshopSessionService;
import fpt.project.NeoNHS.service.WorkshopTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<Page<WorkshopTemplateResponse>>> getAllWorkshopTemplates(
                        @RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE) int page,
                        @RequestParam(defaultValue = PaginationConstants.DEFAULT_SIZE) int size,
                        @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_BY) String sortBy,
                        @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_DIR) String sortDir) {

                Sort sort = sortDir.equalsIgnoreCase(PaginationConstants.SORT_ASC)
                                ? Sort.by(sortBy).ascending()
                                : Sort.by(sortBy).descending();

                Pageable pageable = PageRequest.of(page, size, sort);
                Page<WorkshopTemplateResponse> response = workshopTemplateService.getAllWorkshopTemplates(pageable);
                return ResponseEntity
                                .ok(ApiResponse.success(HttpStatus.OK, "Workshop templates retrieved successfully",
                                                response));
        }
//        @GetMapping("/templates/all")
//        public ResponseEntity<ApiResponse<List<WorkshopTemplateResponse>>> getAllWorkshopTemplatesWithoutPagination() {
//                List<WorkshopTemplateResponse> response = workshopTemplateService.getAllWorkshopTemplates();
//                return ResponseEntity
//                                .ok(ApiResponse.success(HttpStatus.OK, "Workshop templates retrieved successfully",
//                                                response));
//        }


        @GetMapping("/templates/my")
        @PreAuthorize("hasRole('VENDOR')")
        public ResponseEntity<ApiResponse<Page<WorkshopTemplateResponse>>> getMyWorkshopTemplates(
                        Principal principal,
                        @RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE) int page,
                        @RequestParam(defaultValue = PaginationConstants.DEFAULT_SIZE) int size,
                        @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_BY) String sortBy,
                        @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_DIR) String sortDir) {

                Sort sort = sortDir.equalsIgnoreCase(PaginationConstants.SORT_ASC)
                                ? Sort.by(sortBy).ascending()
                                : Sort.by(sortBy).descending();

                Pageable pageable = PageRequest.of(page, size, sort);
                Page<WorkshopTemplateResponse> response = workshopTemplateService
                                .getMyWorkshopTemplates(principal.getName(), pageable);
                return ResponseEntity
                                .ok(ApiResponse.success(HttpStatus.OK, "Your workshop templates retrieved successfully",
                                                response));
        }

//        @GetMapping("/templates/my/all")
//        @PreAuthorize("hasRole('VENDOR')")
//        public ResponseEntity<ApiResponse<List<WorkshopTemplateResponse>>> getMyWorkshopTemplatesWithoutPagination(
//                        Principal principal) {
//                List<WorkshopTemplateResponse> response = workshopTemplateService
//                                .getMyWorkshopTemplates(principal.getName());
//                return ResponseEntity
//                                .ok(ApiResponse.success(HttpStatus.OK, "Your workshop templates retrieved successfully",
//                                                response));
//        }

        // ==================== SEARCH & FILTER ====================

        @GetMapping("/templates/filter")
        public ResponseEntity<ApiResponse<List<WorkshopTemplateResponse>>> filterWorkshopTemplates(
                        @RequestParam(required = false) String keyword,
                        @RequestParam(required = false) String name,
                        @RequestParam(required = false) WorkshopStatus status,
                        @RequestParam(required = false) UUID vendorId,
                        @RequestParam(required = false) UUID tagId,
                        @RequestParam(required = false) BigDecimal minPrice,
                        @RequestParam(required = false) BigDecimal maxPrice,
                        @RequestParam(required = false) Integer minDuration,
                        @RequestParam(required = false) Integer maxDuration,
                        @RequestParam(required = false) BigDecimal minRating) {
                List<WorkshopTemplateResponse> response = workshopTemplateService.searchWorkshopTemplates(
                                keyword, name, status, vendorId, tagId,
                                minPrice, maxPrice, minDuration, maxDuration, minRating);
                return ResponseEntity
                                .ok(ApiResponse.success(HttpStatus.OK, "Workshop templates filtered successfully",
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
