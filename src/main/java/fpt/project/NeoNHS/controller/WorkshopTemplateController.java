package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.constants.PaginationConstants;
import fpt.project.NeoNHS.dto.request.workshop.CreateWorkshopTemplateRequest;
import fpt.project.NeoNHS.dto.request.workshop.RejectWorkshopTemplateRequest;
import fpt.project.NeoNHS.dto.request.workshop.UpdateWorkshopTemplateRequest;

import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.workshop.WorkshopTemplateResponse;
import fpt.project.NeoNHS.enums.WorkshopStatus;
import fpt.project.NeoNHS.service.WorkshopTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Workshop Template Management", description = "APIs for managing workshop templates - the blueprints that require admin approval")
public class WorkshopTemplateController {

        private final WorkshopTemplateService workshopTemplateService;

        // ==================== CREATE WORKSHOP TEMPLATE ====================

        @Operation(
                summary = "Create a new workshop template (Vendor only)",
                description = """
                        Creates a new workshop template in DRAFT status. Only verified vendors can create templates.
                        
                        **Requirements:**
                        - Vendor role required
                        - Vendor profile must be verified
                        - All required fields must be provided
                        
                        **Required Fields:**
                        - name: Template title
                        - shortDescription: Brief summary
                        - fullDescription: Detailed description
                        - defaultPrice: Base price for the workshop
                        - estimatedDuration: Duration in minutes
                        - minParticipants: Minimum number of participants
                        - maxParticipants: Maximum number of participants
                        - tagIds: At least one category/tag
                        - imageUrls: At least one image URL
                        
                        **Optional Fields:**
                        - thumbnailIndex: Index of the thumbnail image (default: 0)
                        
                        **After Creation:**
                        - Status is set to DRAFT
                        - Vendor can edit the template
                        - Vendor can submit for approval when ready
                        """
        )
        @ApiResponses(value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "201",
                        description = "Workshop template created successfully",
                        content = @Content(mediaType = "application/json",
                                         schema = @Schema(implementation = WorkshopTemplateResponse.class))
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "Bad Request - Validation failed or vendor not verified"
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "401",
                        description = "Unauthorized - Valid JWT token required"
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Forbidden - Vendor role required"
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Not Found - Vendor profile or tags not found"
                )
        })
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

        // ==================== READ WORKSHOP TEMPLATE ====================

        @Operation(
                summary = "Get workshop template by ID",
                description = """
                        Retrieves detailed information about a specific workshop template.
                        
                        **Returns:**
                        - Complete template information including:
                          * Basic details (name, descriptions, pricing)
                          * Participants limits
                          * Status and approval information
                          * Associated images and tags
                          * Vendor information
                          * Ratings and reviews count
                        
                        **Access:**
                        - Public endpoint (no authentication required)
                        - Available for all template statuses
                        """
        )
        @ApiResponses(value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Template retrieved successfully",
                        content = @Content(mediaType = "application/json",
                                         schema = @Schema(implementation = WorkshopTemplateResponse.class))
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Not Found - Template does not exist"
                )
        })
        @GetMapping("/templates/{id}")
        public ResponseEntity<ApiResponse<WorkshopTemplateResponse>> getWorkshopTemplateById(
                        @Parameter(description = "Workshop Template ID", required = true)
                        @PathVariable UUID id) {
                WorkshopTemplateResponse response = workshopTemplateService.getWorkshopTemplateById(id);
                return ResponseEntity
                                .ok(ApiResponse.success(HttpStatus.OK, "Workshop template retrieved successfully",
                                                response));
        }

        @Operation(
                summary = "Get all workshop templates with pagination (Admin only)",
                description = """
                        Retrieves all workshop templates with pagination and sorting. Admin access only.
                        
                        **Access:**
                        - Admin role required
                        - Returns templates in all statuses (DRAFT, PENDING, ACTIVE, REJECTED)
                        
                        **Pagination Parameters:**
                        - page: Page number (default: 1)
                        - size: Items per page (default: 10)
                        - sortBy: Field to sort by (default: "createdAt")
                        - sortDir: Sort direction - "asc" or "desc" (default: "desc")
                        
                        **Common Sort Fields:**
                        - createdAt: Creation date
                        - updatedAt: Last update date
                        - name: Template name
                        - defaultPrice: Price
                        - status: Template status
                        
                        **Use Cases:**
                        - Admin dashboard to review all templates
                        - Monitoring pending approvals
                        - Template management
                        """
        )
        @ApiResponses(value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Templates retrieved successfully",
                        content = @Content(mediaType = "application/json")
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "401",
                        description = "Unauthorized - Valid JWT token required"
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Forbidden - Admin role required"
                )
        })
        @GetMapping("/templates")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<Page<WorkshopTemplateResponse>>> getAllWorkshopTemplates(
                        @Parameter(description = "Page number (1-based)", example = "1")
                        @RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE) int page,
                        @Parameter(description = "Number of items per page", example = "10")
                        @RequestParam(defaultValue = PaginationConstants.DEFAULT_SIZE) int size,
                        @Parameter(description = "Field to sort by", example = "createdAt")
                        @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_BY) String sortBy,
                        @Parameter(description = "Sort direction: asc or desc", example = "desc")
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


        @Operation(
                summary = "Get vendor's own workshop templates with pagination (Vendor only)",
                description = """
                        Retrieves all workshop templates created by the authenticated vendor with pagination.
                        
                        **Access:**
                        - Vendor role required
                        - Returns only templates owned by the authenticated vendor
                        
                        **Pagination Parameters:**
                        - page: Page number (default: 1)
                        - size: Items per page (default: 10)
                        - sortBy: Field to sort by (default: "createdAt")
                        - sortDir: Sort direction - "asc" or "desc" (default: "desc")
                        
                        **Returns:**
                        - Templates in all statuses (DRAFT, PENDING, ACTIVE, REJECTED)
                        - Includes rejection reasons if applicable
                        - Shows approval status and timestamps
                        
                        **Use Cases:**
                        - Vendor dashboard to manage their templates
                        - View template statuses and approval progress
                        - Access drafts for editing
                        - Review rejected templates with reasons
                        """
        )
        @ApiResponses(value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Templates retrieved successfully",
                        content = @Content(mediaType = "application/json")
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "401",
                        description = "Unauthorized - Valid JWT token required"
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Forbidden - Vendor role required"
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Not Found - Vendor profile not found"
                )
        })
        @GetMapping("/templates/my")
        @PreAuthorize("hasRole('VENDOR')")
        public ResponseEntity<ApiResponse<Page<WorkshopTemplateResponse>>> getMyWorkshopTemplates(
                        Principal principal,
                        @Parameter(description = "Page number (1-based)", example = "1")
                        @RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE) int page,
                        @Parameter(description = "Number of items per page", example = "10")
                        @RequestParam(defaultValue = PaginationConstants.DEFAULT_SIZE) int size,
                        @Parameter(description = "Field to sort by", example = "createdAt")
                        @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_BY) String sortBy,
                        @Parameter(description = "Sort direction: asc or desc", example = "desc")
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

        // ==================== SEARCH & FILTER WORKSHOP TEMPLATE ====================

        @Operation(
                summary = "Filter and search workshop templates",
                description = """
                        Advanced search and filtering for workshop templates with multiple criteria.
                        
                        **Search Capabilities:**
                        - Keyword search (searches in name and descriptions)
                        - Filter by name (exact or partial match)
                        - Filter by status (DRAFT, PENDING, ACTIVE, REJECTED)
                        - Filter by vendor
                        - Filter by tag/category
                        - Price range filtering
                        - Duration range filtering
                        - Minimum rating filtering
                        
                        **Query Parameters (all optional):**
                        - keyword: Search in name and descriptions
                        - name: Filter by template name
                        - status: Filter by template status
                        - vendorId: Filter by specific vendor
                        - tagId: Filter by specific tag/category
                        - minPrice: Minimum price
                        - maxPrice: Maximum price
                        - minDuration: Minimum duration (minutes)
                        - maxDuration: Maximum duration (minutes)
                        - minRating: Minimum average rating
                        
                        **Access:**
                        - Public endpoint (no authentication required)
                        
                        **Use Cases:**
                        - Public workshop catalog/marketplace
                        - User browsing and searching for workshops
                        - Filtering by categories or price ranges
                        - Finding highly-rated workshops
                        
                        **Example Queries:**
                        - Find yoga workshops: ?keyword=yoga
                        - Price range $50-$100: ?minPrice=50&maxPrice=100
                        - Active templates only: ?status=ACTIVE
                        - Duration 60-120 minutes: ?minDuration=60&maxDuration=120
                        - Highly rated (4+): ?minRating=4.0
                        """
        )
        @ApiResponses(value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Templates filtered successfully",
                        content = @Content(mediaType = "application/json")
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "Bad Request - Invalid filter parameters"
                )
        })
        @GetMapping("/templates/filter")
        public ResponseEntity<ApiResponse<List<WorkshopTemplateResponse>>> filterWorkshopTemplates(
                        @Parameter(description = "Keyword to search in name and descriptions", example = "yoga")
                        @RequestParam(required = false) String keyword,
                        @Parameter(description = "Filter by template name", example = "Beginner Yoga")
                        @RequestParam(required = false) String name,
                        @Parameter(description = "Filter by status", example = "ACTIVE")
                        @RequestParam(required = false) WorkshopStatus status,
                        @Parameter(description = "Filter by vendor ID")
                        @RequestParam(required = false) UUID vendorId,
                        @Parameter(description = "Filter by tag/category ID")
                        @RequestParam(required = false) UUID tagId,
                        @Parameter(description = "Minimum price", example = "50.00")
                        @RequestParam(required = false) BigDecimal minPrice,
                        @Parameter(description = "Maximum price", example = "100.00")
                        @RequestParam(required = false) BigDecimal maxPrice,
                        @Parameter(description = "Minimum duration in minutes", example = "60")
                        @RequestParam(required = false) Integer minDuration,
                        @Parameter(description = "Maximum duration in minutes", example = "120")
                        @RequestParam(required = false) Integer maxDuration,
                        @Parameter(description = "Minimum average rating", example = "4.0")
                        @RequestParam(required = false) BigDecimal minRating) {
                List<WorkshopTemplateResponse> response = workshopTemplateService.searchWorkshopTemplates(
                                keyword, name, status, vendorId, tagId,
                                minPrice, maxPrice, minDuration, maxDuration, minRating);
                return ResponseEntity
                                .ok(ApiResponse.success(HttpStatus.OK, "Workshop templates filtered successfully",
                                                response));
        }

        // ==================== UPDATE WORKSHOP TEMPLATE ====================

        @Operation(
                summary = "Update a workshop template (Vendor only)",
                description = """
                        Updates an existing workshop template. Only templates in DRAFT or REJECTED status can be updated.
                        
                        **Requirements:**
                        - Vendor role required
                        - Must be the template owner
                        - Template must be in DRAFT or REJECTED status
                        
                        **Editable Statuses:**
                        - DRAFT: Can be freely edited
                        - REJECTED: Can be edited to address rejection reasons
                        
                        **Cannot Edit:**
                        - PENDING: Template is under admin review (locked)
                        - ACTIVE: Template is live (locked)
                        
                        **Updatable Fields:**
                        - name: Template title
                        - shortDescription: Brief summary
                        - fullDescription: Detailed description
                        - defaultPrice: Base price
                        - estimatedDuration: Duration in minutes
                        - minParticipants: Minimum participants
                        - maxParticipants: Maximum participants
                        - tagIds: Category/tag associations
                        - imageUrls: Workshop images
                        - thumbnailIndex: Which image is the thumbnail
                        
                        **Validation:**
                        - minParticipants ≤ maxParticipants
                        - thumbnailIndex must be valid (within imageUrls array)
                        - All tag IDs must exist
                        
                        **After Update:**
                        - Template remains in current status
                        - updatedAt timestamp is updated
                        - Changes are saved immediately
                        
                        **Use Case:**
                        - Fix issues mentioned in rejection reason
                        - Improve template before submission
                        - Update pricing or participant limits
                        """
        )
        @ApiResponses(value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Template updated successfully",
                        content = @Content(mediaType = "application/json",
                                         schema = @Schema(implementation = WorkshopTemplateResponse.class))
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "Bad Request - Validation failed or template cannot be edited (wrong status)"
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "401",
                        description = "Unauthorized - Valid JWT token required"
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Forbidden - Vendor role required or not template owner"
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Not Found - Template does not exist"
                )
        })
        @PutMapping("/templates/{id}")
        @PreAuthorize("hasRole('VENDOR')")
        public ResponseEntity<ApiResponse<WorkshopTemplateResponse>> updateWorkshopTemplate(
                        @Parameter(description = "Workshop Template ID", required = true)
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

        // ==================== REGISTER/SUBMIT FOR APPROVAL WORKSHOP TEMPLATE ====================

        @Operation(
                summary = "Submit workshop template for admin approval",
                description = """
                        Submits a DRAFT or REJECTED workshop template for admin review.
                        
                        **Requirements:**
                        - Template must be in DRAFT or REJECTED status
                        - All mandatory fields must be completed:
                          * Title/Name
                          * Short Description
                          * Full Description
                          * Price
                          * Duration
                          * Min/Max Participants
                          * At least one image
                          * At least one category/tag
                        
                        **Workflow:**
                        1. Vendor completes template fields
                        2. Vendor clicks Register/Submit
                        3. System validates completeness
                        4. Status changes to PENDING
                        5. Template is locked for editing until admin review
                        
                        **After Submission:**
                        - Template cannot be edited while PENDING
                        - Admin will approve (→ ACTIVE) or reject (→ REJECTED)
                        - If rejected, vendor can update and resubmit
                        """
        )
        @ApiResponses(value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Template submitted successfully",
                        content = @Content(mediaType = "application/json",
                                         schema = @Schema(implementation = WorkshopTemplateResponse.class))
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "Bad Request - Template incomplete, wrong status, or validation failed"
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "401",
                        description = "Unauthorized - Valid JWT token required"
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Forbidden - Vendor role required or not template owner"
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Not Found - Template does not exist"
                )
        })
        @PostMapping("/templates/{id}/register")
        @PreAuthorize("hasRole('VENDOR')")
        public ResponseEntity<ApiResponse<WorkshopTemplateResponse>> registerWorkshopTemplate(
                        @Parameter(description = "Workshop Template ID", required = true)
                        @PathVariable UUID id,
                        Principal principal) {
                WorkshopTemplateResponse response = workshopTemplateService.registerWorkshopTemplate(
                                principal.getName(),
                                id);
                return ResponseEntity
                                .ok(ApiResponse.success(HttpStatus.OK,
                                                "Template submitted successfully. Please wait for admin approval.",
                                                response));
        }

        // ==================== APPROVE/REJECT WORKSHOP TEMPLATE (ADMIN ONLY) ====================

        @Operation(
                summary = "Approve a workshop template (Admin only)",
                description = """
                        Approves a PENDING workshop template, making it ACTIVE and available for scheduling.
                        
                        **Requirements:**
                        - Admin role required
                        - Template must be in PENDING status
                        
                        **Workflow:**
                        1. Admin reviews the submitted template
                        2. Admin clicks Approve
                        3. Status changes from PENDING → ACTIVE
                        4. Approval timestamp and admin ID are recorded
                        5. Template becomes available for vendors to create sessions
                        
                        **After Approval:**
                        - Vendor can no longer edit the template
                        - Vendor can create workshop sessions based on this template
                        - Template appears in public listings
                        """
        )
        @ApiResponses(value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Template approved successfully",
                        content = @Content(mediaType = "application/json",
                                         schema = @Schema(implementation = WorkshopTemplateResponse.class))
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "Bad Request - Template is not in PENDING status"
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "401",
                        description = "Unauthorized - Valid JWT token required"
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Forbidden - Admin role required"
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Not Found - Template does not exist"
                )
        })
        @PostMapping("/templates/{id}/approve")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<WorkshopTemplateResponse>> approveWorkshopTemplate(
                        @Parameter(description = "Workshop Template ID", required = true)
                        @PathVariable UUID id,
                        Principal principal) {
                WorkshopTemplateResponse response = workshopTemplateService.approveWorkshopTemplate(
                                principal.getName(),
                                id);
                return ResponseEntity
                                .ok(ApiResponse.success(HttpStatus.OK,
                                                "Workshop template approved successfully",
                                                response));
        }

        @Operation(
                summary = "Reject a workshop template (Admin only)",
                description = """
                        Rejects a PENDING workshop template with a reason, allowing the vendor to fix and resubmit.
                        
                        **Requirements:**
                        - Admin role required
                        - Template must be in PENDING status
                        - Reject reason is mandatory
                        
                        **Workflow:**
                        1. Admin reviews the submitted template
                        2. Admin finds issues (incomplete info, policy violations, etc.)
                        3. Admin clicks Reject and provides detailed reason
                        4. Status changes from PENDING → REJECTED
                        5. Rejection reason is stored and shown to vendor
                        
                        **After Rejection:**
                        - Vendor can view the rejection reason
                        - Vendor can edit the template to fix issues
                        - Vendor can resubmit for approval
                        - Previous approval data is cleared
                        
                        **Common Rejection Reasons:**
                        - Inappropriate content
                        - Missing required information
                        - Low quality images
                        - Unclear description
                        - Pricing concerns
                        """
        )
        @ApiResponses(value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Template rejected successfully",
                        content = @Content(mediaType = "application/json",
                                         schema = @Schema(implementation = WorkshopTemplateResponse.class))
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "Bad Request - Template is not in PENDING status or reject reason is missing"
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "401",
                        description = "Unauthorized - Valid JWT token required"
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Forbidden - Admin role required"
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Not Found - Template does not exist"
                )
        })
        @PostMapping("/templates/{id}/reject")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<ApiResponse<WorkshopTemplateResponse>> rejectWorkshopTemplate(
                        @Parameter(description = "Workshop Template ID", required = true)
                        @PathVariable UUID id,
                        @Valid @RequestBody RejectWorkshopTemplateRequest request,
                        Principal principal) {
                WorkshopTemplateResponse response = workshopTemplateService.rejectWorkshopTemplate(
                                principal.getName(),
                                id,
                                request.getRejectReason());
                return ResponseEntity
                                .ok(ApiResponse.success(HttpStatus.OK,
                                                "Workshop template rejected",
                                                response));
        }

        // ==================== DELETE WORKSHOP TEMPLATE ====================

        @Operation(
                summary = "Delete a workshop template (Vendor only)",
                description = """
                        Permanently deletes a workshop template. Only templates that are not ACTIVE can be deleted.
                        
                        **Requirements:**
                        - Vendor role required
                        - Must be the template owner
                        - Template must NOT be in ACTIVE status
                        
                        **Deletable Statuses:**
                        - DRAFT: Not yet submitted
                        - PENDING: Under review (can be withdrawn)
                        - REJECTED: After rejection
                        
                        **Cannot Delete:**
                        - ACTIVE: Template is live and may have associated workshop sessions
                        
                        **What Gets Deleted:**
                        - Workshop template record
                        - All associated images (cascading delete)
                        - All tag associations (cascading delete)
                        
                        **Warning:**
                        - This action is permanent and cannot be undone
                        - All template data will be lost
                        - If you want to deactivate an ACTIVE template, use a different endpoint
                        
                        **Use Cases:**
                        - Remove unwanted draft templates
                        - Delete templates that won't be used
                        - Clean up after rejections (alternative to editing and resubmitting)
                        """
        )
        @ApiResponses(value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "Template deleted successfully"
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "400",
                        description = "Bad Request - Cannot delete ACTIVE template"
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "401",
                        description = "Unauthorized - Valid JWT token required"
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "403",
                        description = "Forbidden - Vendor role required or not template owner"
                ),
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "404",
                        description = "Not Found - Template does not exist"
                ) 
        })
        @DeleteMapping("/templates/{id}")
        @PreAuthorize("hasRole('VENDOR')")
        public ResponseEntity<ApiResponse<Void>> deleteWorkshopTemplate(
                        @Parameter(description = "Workshop Template ID to delete", required = true)
                        @PathVariable UUID id,
                        Principal principal) {
                workshopTemplateService.deleteWorkshopTemplate(principal.getName(), id);
                return ResponseEntity
                                .ok(ApiResponse.success(HttpStatus.OK, "Workshop template deleted successfully", null));
        }
}
