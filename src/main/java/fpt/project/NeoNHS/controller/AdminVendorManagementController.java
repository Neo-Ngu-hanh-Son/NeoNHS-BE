package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.constants.PaginationConstants;
import fpt.project.NeoNHS.dto.request.admin.BanVendorRequest;
import fpt.project.NeoNHS.dto.request.admin.CreateVendorByAdminRequest;
import fpt.project.NeoNHS.dto.request.admin.UpdateVendorByAdminRequest;
import fpt.project.NeoNHS.dto.request.workshop.RejectWorkshopTemplateRequest;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.auth.VendorProfileResponse;
import fpt.project.NeoNHS.dto.response.workshop.WorkshopTemplateResponse;
import fpt.project.NeoNHS.service.AdminVendorManagementService;
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

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/vendors")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Vendor Management", description = "APIs for Admin to manage vendors and workshop templates")
public class AdminVendorManagementController {

    private final AdminVendorManagementService adminVendorManagementService;
    private final WorkshopTemplateService workshopTemplateService;

    @PostMapping
    @Operation(summary = "Create a new vendor account", description = "Admin creates a new vendor account with profile")
    public ResponseEntity<ApiResponse<VendorProfileResponse>> createVendor(
            @Valid @RequestBody CreateVendorByAdminRequest request) {

        VendorProfileResponse vendor = adminVendorManagementService.createVendorByAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "Vendor created successfully", vendor));
    }

    @GetMapping
    @Operation(summary = "Get all vendors", description = "Get all vendors with pagination and sorting")
    public ResponseEntity<ApiResponse<Page<VendorProfileResponse>>> getAllVendors(
            @Parameter(description = "Page number (1-based)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (ASC/DESC)") @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort sort = sortDirection.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        Page<VendorProfileResponse> vendors = adminVendorManagementService.getAllVendors(pageable);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Vendors retrieved successfully", vendors));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get vendor by ID", description = "Get a specific vendor by their ID")
    public ResponseEntity<ApiResponse<VendorProfileResponse>> getVendorById(@PathVariable UUID id) {
        VendorProfileResponse vendor = adminVendorManagementService.getVendorById(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Vendor retrieved successfully", vendor));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update vendor profile", description = "Admin updates a vendor's profile information")
    public ResponseEntity<ApiResponse<VendorProfileResponse>> updateVendor(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateVendorByAdminRequest request) {

        VendorProfileResponse vendor = adminVendorManagementService.updateVendorByAdmin(id, request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Vendor updated successfully", vendor));
    }

    @PostMapping("/{id}/ban")
    @Operation(summary = "Ban a vendor", description = "Ban a vendor account with optional reason")
    public ResponseEntity<ApiResponse<VendorProfileResponse>> banVendor(
            @PathVariable UUID id,
            @RequestBody(required = false) BanVendorRequest request) {

        if (request == null) {
            request = new BanVendorRequest();
        }

        VendorProfileResponse vendor = adminVendorManagementService.banVendor(id, request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Vendor banned successfully", vendor));
    }

    @PostMapping("/{id}/unban")
    @Operation(summary = "Unban a vendor", description = "Unban a previously banned vendor account")
    public ResponseEntity<ApiResponse<VendorProfileResponse>> unbanVendor(@PathVariable UUID id) {
        VendorProfileResponse vendor = adminVendorManagementService.unbanVendor(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Vendor unbanned successfully", vendor));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete vendor", description = "Soft delete a vendor account (deactivate)")
    public ResponseEntity<ApiResponse<Void>> deleteVendor(@PathVariable UUID id) {
        adminVendorManagementService.deleteVendor(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Vendor deleted successfully", null));
    }

    @GetMapping("/search")
    @Operation(summary = "Search vendors", description = "Search vendors by keyword (name, email, business name)")
    public ResponseEntity<ApiResponse<Page<VendorProfileResponse>>> searchVendors(
            @Parameter(description = "Search keyword") @RequestParam String keyword,
            @Parameter(description = "Page number (1-based)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (ASC/DESC)") @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort sort = sortDirection.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        Page<VendorProfileResponse> vendors = adminVendorManagementService.searchVendors(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Search results retrieved successfully", vendors));
    }

    @GetMapping("/filter")
    @Operation(summary = "Advanced filter", description = "Filter vendors with multiple criteria")
    public ResponseEntity<ApiResponse<Page<VendorProfileResponse>>> advancedFilter(
            @Parameter(description = "Search keyword") @RequestParam(required = false) String keyword,
            @Parameter(description = "Verification status") @RequestParam(required = false) Boolean isVerified,
            @Parameter(description = "Banned status") @RequestParam(required = false) Boolean isBanned,
            @Parameter(description = "Active status") @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Page number (1-based)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (ASC/DESC)") @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort sort = sortDirection.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        Page<VendorProfileResponse> vendors = adminVendorManagementService.advancedSearchAndFilter(
                keyword, isVerified, isBanned, isActive, pageable
        );
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Filtered results retrieved successfully", vendors));
    }

    @GetMapping("/filter/verified")
    @Operation(summary = "Filter by verification status", description = "Get vendors filtered by verification status")
    public ResponseEntity<ApiResponse<Page<VendorProfileResponse>>> filterByVerification(
            @Parameter(description = "Verification status") @RequestParam Boolean isVerified,
            @Parameter(description = "Page number (1-based)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (ASC/DESC)") @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort sort = sortDirection.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        Page<VendorProfileResponse> vendors = adminVendorManagementService.filterVendorsByVerification(isVerified, pageable);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Filtered vendors retrieved successfully", vendors));
    }

    @GetMapping("/filter/banned")
    @Operation(summary = "Filter by banned status", description = "Get vendors filtered by banned status")
    public ResponseEntity<ApiResponse<Page<VendorProfileResponse>>> filterByBannedStatus(
            @Parameter(description = "Banned status") @RequestParam Boolean isBanned,
            @Parameter(description = "Page number (1-based)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (ASC/DESC)") @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort sort = sortDirection.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        Page<VendorProfileResponse> vendors = adminVendorManagementService.filterVendorsByBannedStatus(isBanned, pageable);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Filtered vendors retrieved successfully", vendors));
    }

    @GetMapping("/filter/active")
    @Operation(summary = "Filter by active status", description = "Get vendors filtered by active status")
    public ResponseEntity<ApiResponse<Page<VendorProfileResponse>>> filterByActiveStatus(
            @Parameter(description = "Active status") @RequestParam Boolean isActive,
            @Parameter(description = "Page number (1-based)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (ASC/DESC)") @RequestParam(defaultValue = "DESC") String sortDirection) {

        Sort sort = sortDirection.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        Page<VendorProfileResponse> vendors = adminVendorManagementService.filterVendorsByActiveStatus(isActive, pageable);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Filtered vendors retrieved successfully", vendors));
    }

    // ==================== WORKSHOP TEMPLATE MANAGEMENT ====================

    @GetMapping("/{id}/workshop-templates")
    @Operation(
            summary = "Get all workshop templates for a specific vendor (Admin only)",
            description = """
                    Retrieves all workshop templates created by a specific vendor with pagination and sorting.
                    
                    **Access:**
                    - Admin role required
                    - Returns templates in all statuses (DRAFT, PENDING, ACTIVE, REJECTED)
                    
                    **Pagination Parameters:**
                    - page: Page number (default: 1)
                    - size: Items per page (default: 10)
                    - sortBy: Field to sort by (default: "createdAt")
                    - sortDir: Sort direction - "ASC" or "DESC" (default: "DESC")
                    
                    **Common Sort Fields:**
                    - createdAt: Creation date
                    - updatedAt: Last update date
                    - name: Template name
                    - defaultPrice: Price
                    - status: Template status
                    
                    **Use Cases:**
                    - View all templates from vendor detail modal
                    - Monitor vendor's template submission activity
                    - Review vendor's workshop portfolio
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
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Not Found - Vendor does not exist"
            )
    })
    public ResponseEntity<ApiResponse<Page<WorkshopTemplateResponse>>> getVendorWorkshopTemplates(
            @Parameter(description = "Vendor ID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Page number (1-based)", example = "1")
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE) int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SIZE) int size,
            @Parameter(description = "Field to sort by", example = "createdAt")
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_BY) String sortBy,
            @Parameter(description = "Sort direction: ASC or DESC", example = "DESC")
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_DIR) String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase(PaginationConstants.SORT_ASC)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<WorkshopTemplateResponse> response = workshopTemplateService.getWorkshopTemplatesByVendorId(id, pageable);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Vendor's workshop templates retrieved successfully", response));
    }

    @GetMapping("/workshop-templates")
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

        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<WorkshopTemplateResponse> response = workshopTemplateService.getAllWorkshopTemplates(pageable);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Workshop templates retrieved successfully", response));
    }

    @PostMapping("/workshop-templates/{id}/approve")
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
    public ResponseEntity<ApiResponse<WorkshopTemplateResponse>> approveWorkshopTemplate(
            @Parameter(description = "Workshop Template ID", required = true)
            @PathVariable UUID id,
            Principal principal) {
        WorkshopTemplateResponse response = workshopTemplateService.approveWorkshopTemplate(
                principal.getName(),
                id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Workshop template approved successfully", response));
    }

    @PostMapping("/workshop-templates/{id}/reject")
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
    public ResponseEntity<ApiResponse<WorkshopTemplateResponse>> rejectWorkshopTemplate(
            @Parameter(description = "Workshop Template ID", required = true)
            @PathVariable UUID id,
            @Valid @RequestBody RejectWorkshopTemplateRequest request,
            Principal principal) {
        WorkshopTemplateResponse response = workshopTemplateService.rejectWorkshopTemplate(
                principal.getName(),
                id,
                request.getRejectReason());
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Workshop template rejected", response));
    }
}
