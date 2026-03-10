package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.constants.PaginationConstants;
import fpt.project.NeoNHS.dto.request.workshop.CreateWorkshopSessionRequest;
import fpt.project.NeoNHS.dto.request.workshop.UpdateWorkshopSessionRequest;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.workshop.WorkshopSessionResponse;
import fpt.project.NeoNHS.enums.SessionStatus;
import fpt.project.NeoNHS.service.WorkshopSessionService;
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
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/workshops")
@RequiredArgsConstructor
@Tag(name = "Vendor - Workshop Session Management", description = "APIs for managing workshop sessions - the bookable instances that users see")
public class WorkshopSessionController {

    private final WorkshopSessionService workshopSessionService;

    // ==================== CREATE WORKSHOP SESSION ====================

    @Operation(
            summary = "Create a new workshop session (Vendor only)",
            description = """
                    Creates a new scheduled workshop session from an ACTIVE template. No admin approval required.
                    
                    **Requirements:**
                    - Vendor role required
                    - Must own the template
                    - Template must be ACTIVE status
                    - Start time must be in the future
                    - End time must be after start time
                    
                    **Required Fields:**
                    - workshopTemplateId: ID of an ACTIVE template
                    - startTime: When the session starts
                    - endTime: When the session ends
                    
                    **Optional Fields:**
                    - price: Session price (defaults to template's defaultPrice)
                    - maxParticipants: Max attendees (defaults to template's maxParticipants)
                    
                    **After Creation:**
                    - Status is set to SCHEDULED
                    - Session is immediately available for users to book
                    - Vendor can update/cancel/delete the session
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Workshop session created successfully",
                    content = @Content(mediaType = "application/json",
                                     schema = @Schema(implementation = WorkshopSessionResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Validation failed or template not ACTIVE"
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
    @PostMapping("/sessions")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<WorkshopSessionResponse>> createWorkshopSession(
                    @Valid @RequestBody CreateWorkshopSessionRequest request,
                    Principal principal) {
            WorkshopSessionResponse response = workshopSessionService.createWorkshopSession(
                            principal.getName(),
                            request);
            return ResponseEntity.status(HttpStatus.CREATED)
                            .body(ApiResponse.success(HttpStatus.CREATED, "Workshop session created successfully",
                                            response));
    }

    // ==================== READ WORKSHOP SESSION ====================

    @Operation(
            summary = "Get workshop session by ID",
            description = """
                    Retrieves detailed information about a specific workshop session.
                    
                    **Returns:**
                    - Complete session information including:
                      * Session details (time, price, participants, status)
                      * Template details (name, description, images, tags, ratings)
                      * Vendor information
                      * Available slots for booking
                    
                    **Access:**
                    - Public endpoint (no authentication required)
                    - This is what users see when browsing workshops
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Session retrieved successfully",
                    content = @Content(mediaType = "application/json",
                                     schema = @Schema(implementation = WorkshopSessionResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Not Found - Session does not exist"
            )
    })
    @GetMapping("/sessions/{id}")
    public ResponseEntity<ApiResponse<WorkshopSessionResponse>> getWorkshopSessionById(
                    @Parameter(description = "Workshop Session ID", required = true)
                    @PathVariable UUID id) {
            WorkshopSessionResponse response = workshopSessionService.getWorkshopSessionById(id);
            return ResponseEntity
                            .ok(ApiResponse.success(HttpStatus.OK, "Workshop session retrieved successfully",
                                            response));
    }

    @Operation(
            summary = "Get all upcoming workshop sessions",
            description = """
                    Retrieves all SCHEDULED workshop sessions starting in the future.
                    
                    **Use Case:**
                    - Public marketplace for users to browse available workshops
                    - Shows only bookable sessions (SCHEDULED status)
                    - Excludes past sessions and cancelled sessions
                    
                    **Pagination Parameters:**
                    - page: Page number (default: 0)
                    - size: Items per page (default: 10)
                    - sortBy: Field to sort by (default: "startTime")
                    - sortDir: Sort direction - "asc" or "desc" (default: "asc")
                    
                    **Access:**
                    - Public endpoint (no authentication required)
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Sessions retrieved successfully",
                    content = @Content(mediaType = "application/json")
            )
    })
    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<Page<WorkshopSessionResponse>>> getAllUpcomingSessions(
                    @Parameter(description = "Page number (0-based)", example = "0")
                    @RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE) int page,
                    @Parameter(description = "Number of items per page", example = "10")
                    @RequestParam(defaultValue = PaginationConstants.DEFAULT_SIZE) int size,
                    @Parameter(description = "Field to sort by", example = "startTime")
                    @RequestParam(defaultValue = "startTime") String sortBy,
                    @Parameter(description = "Sort direction: asc or desc", example = "asc")
                    @RequestParam(defaultValue = PaginationConstants.SORT_ASC) String sortDir) {

            Sort sort = sortDir.equalsIgnoreCase(PaginationConstants.SORT_ASC)
                            ? Sort.by(sortBy).ascending()
                            : Sort.by(sortBy).descending();

            Pageable pageable = PageRequest.of(page, size, sort);
            Page<WorkshopSessionResponse> response = workshopSessionService.getAllUpcomingSessions(pageable);
            return ResponseEntity
                            .ok(ApiResponse.success(HttpStatus.OK, "Upcoming workshop sessions retrieved successfully",
                                            response));
    }

    @Operation(
            summary = "Get vendor's own workshop sessions (Vendor only)",
            description = """
                    Retrieves all workshop sessions created by the authenticated vendor.
                    
                    **Access:**
                    - Vendor role required
                    - Returns only sessions owned by the authenticated vendor
                    
                    **Pagination Parameters:**
                    - page: Page number (default: 0)
                    - size: Items per page (default: 10)
                    - sortBy: Field to sort by (default: "startTime")
                    - sortDir: Sort direction - "asc" or "desc" (default: "desc")
                    
                    **Returns:**
                    - Sessions in all statuses (SCHEDULED, ONGOING, COMPLETED, CANCELLED)
                    - Useful for vendor dashboard to manage their sessions
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Sessions retrieved successfully",
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
    @GetMapping("/sessions/my")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<Page<WorkshopSessionResponse>>> getMyWorkshopSessions(
                    Principal principal,
                    @Parameter(description = "Page number (0-based)", example = "0")
                    @RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE) int page,
                    @Parameter(description = "Number of items per page", example = "10")
                    @RequestParam(defaultValue = PaginationConstants.DEFAULT_SIZE) int size,
                    @Parameter(description = "Field to sort by", example = "startTime")
                    @RequestParam(defaultValue = "startTime") String sortBy,
                    @Parameter(description = "Sort direction: asc or desc", example = "desc")
                    @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_DIR) String sortDir) {

            Sort sort = sortDir.equalsIgnoreCase(PaginationConstants.SORT_ASC)
                            ? Sort.by(sortBy).ascending()
                            : Sort.by(sortBy).descending();

            Pageable pageable = PageRequest.of(page, size, sort);
            Page<WorkshopSessionResponse> response = workshopSessionService
                            .getMyWorkshopSessions(principal.getName(), pageable);
            return ResponseEntity
                            .ok(ApiResponse.success(HttpStatus.OK, "Your workshop sessions retrieved successfully",
                                            response));
    }

    @Operation(
            summary = "Get sessions by template ID",
            description = """
                    Retrieves all workshop sessions for a specific template.
                    
                    **Use Case:**
                    - Show all available sessions for a specific workshop type
                    - Users can see different time slots for the same workshop
                    
                    **Pagination Parameters:**
                    - page: Page number (default: 0)
                    - size: Items per page (default: 10)
                    - sortBy: Field to sort by (default: "startTime")
                    - sortDir: Sort direction - "asc" or "desc" (default: "asc")
                    
                    **Access:**
                    - Public endpoint (no authentication required)
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Sessions retrieved successfully",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Not Found - Template does not exist"
            )
    })
    @GetMapping("/sessions/template/{templateId}")
    public ResponseEntity<ApiResponse<Page<WorkshopSessionResponse>>> getSessionsByTemplateId(
                    @Parameter(description = "Workshop Template ID", required = true)
                    @PathVariable UUID templateId,
                    @Parameter(description = "Page number (0-based)", example = "0")
                    @RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE) int page,
                    @Parameter(description = "Number of items per page", example = "10")
                    @RequestParam(defaultValue = PaginationConstants.DEFAULT_SIZE) int size,
                    @Parameter(description = "Field to sort by", example = "startTime")
                    @RequestParam(defaultValue = "startTime") String sortBy,
                    @Parameter(description = "Sort direction: asc or desc", example = "asc")
                    @RequestParam(defaultValue = PaginationConstants.SORT_ASC) String sortDir) {

            Sort sort = sortDir.equalsIgnoreCase(PaginationConstants.SORT_ASC)
                            ? Sort.by(sortBy).ascending()
                            : Sort.by(sortBy).descending();

            Pageable pageable = PageRequest.of(page, size, sort);
            Page<WorkshopSessionResponse> response = workshopSessionService
                            .getSessionsByTemplateId(templateId, pageable);
            return ResponseEntity
                            .ok(ApiResponse.success(HttpStatus.OK, "Workshop sessions retrieved successfully",
                                            response));
    }

    // ==================== SEARCH & FILTER WORKSHOP SESSION ====================

    @Operation(
            summary = "Filter and search workshop sessions",
            description = """
                    Advanced search and filtering for workshop sessions with multiple criteria.
                    
                    **Search Capabilities:**
                    - Keyword search (searches in workshop name and descriptions)
                    - Filter by vendor
                    - Filter by tag/category
                    - Filter by session status
                    - Date range filtering (start date to end date)
                    - Price range filtering
                    - Show only sessions with available slots
                    
                    **Query Parameters (all optional):**
                    - keyword: Search in workshop name and descriptions
                    - vendorId: Filter by specific vendor
                    - tagId: Filter by specific tag/category
                    - status: Filter by session status (SCHEDULED, ONGOING, COMPLETED, CANCELLED)
                    - startDate: Show sessions starting after this date (ISO format: yyyy-MM-dd'T'HH:mm:ss)
                    - endDate: Show sessions starting before this date
                    - minPrice: Minimum price
                    - maxPrice: Maximum price
                    - availableOnly: Show only sessions with available slots (true/false)
                    
                    **Pagination Parameters:**
                    - page: Page number (default: 0)
                    - size: Items per page (default: 10)
                    - sortBy: Field to sort by (default: "startTime")
                    - sortDir: Sort direction - "asc" or "desc" (default: "asc")
                    
                    **Access:**
                    - Public endpoint (no authentication required)
                    
                    **Example Queries:**
                    - Find yoga workshops: ?keyword=yoga
                    - Price range $50-$100: ?minPrice=50&maxPrice=100
                    - Scheduled sessions only: ?status=SCHEDULED
                    - Next 7 days: ?startDate=2026-02-09T00:00:00&endDate=2026-02-16T23:59:59
                    - Available slots only: ?availableOnly=true
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Sessions filtered successfully",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Invalid filter parameters"
            )
    })
    @GetMapping("/sessions/filter")
    public ResponseEntity<ApiResponse<Page<WorkshopSessionResponse>>> filterWorkshopSessions(
                    @Parameter(description = "Keyword to search in workshop name and descriptions", example = "yoga")
                    @RequestParam(required = false) String keyword,
                    @Parameter(description = "Filter by vendor ID")
                    @RequestParam(required = false) UUID vendorId,
                    @Parameter(description = "Filter by tag/category ID")
                    @RequestParam(required = false) UUID tagId,
                    @Parameter(description = "Filter by status", example = "SCHEDULED")
                    @RequestParam(required = false) SessionStatus status,
                    @Parameter(description = "Sessions starting after this date", example = "2026-02-09T00:00:00")
                    @RequestParam(required = false) LocalDateTime startDate,
                    @Parameter(description = "Sessions starting before this date", example = "2026-02-16T23:59:59")
                    @RequestParam(required = false) LocalDateTime endDate,
                    @Parameter(description = "Minimum price", example = "50.00")
                    @RequestParam(required = false) BigDecimal minPrice,
                    @Parameter(description = "Maximum price", example = "100.00")
                    @RequestParam(required = false) BigDecimal maxPrice,
                    @Parameter(description = "Show only sessions with available slots", example = "true")
                    @RequestParam(required = false) Boolean availableOnly,
                    @Parameter(description = "Page number (0-based)", example = "0")
                    @RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE) int page,
                    @Parameter(description = "Number of items per page", example = "10")
                    @RequestParam(defaultValue = PaginationConstants.DEFAULT_SIZE) int size,
                    @Parameter(description = "Field to sort by", example = "startTime")
                    @RequestParam(defaultValue = "startTime") String sortBy,
                    @Parameter(description = "Sort direction: asc or desc", example = "asc")
                    @RequestParam(defaultValue = PaginationConstants.SORT_ASC) String sortDir) {

            Sort sort = sortDir.equalsIgnoreCase(PaginationConstants.SORT_ASC)
                            ? Sort.by(sortBy).ascending()
                            : Sort.by(sortBy).descending();

            Pageable pageable = PageRequest.of(page, size, sort);
            Page<WorkshopSessionResponse> response = workshopSessionService.searchWorkshopSessions(
                            keyword, vendorId, tagId, status, startDate, endDate,
                            minPrice, maxPrice, availableOnly, pageable);
            return ResponseEntity
                            .ok(ApiResponse.success(HttpStatus.OK, "Workshop sessions filtered successfully",
                                            response));
    }

    // ==================== UPDATE WORKSHOP SESSION ====================

    @Operation(
            summary = "Update a workshop session (Vendor only)",
            description = """
                    Updates an existing workshop session. Only SCHEDULED sessions can be updated.
                    
                    **Requirements:**
                    - Vendor role required
                    - Must own the session (via template ownership)
                    - Session must be in SCHEDULED status
                    
                    **Updatable Fields:**
                    - startTime: When the session starts
                    - endTime: When the session ends
                    - price: Session price
                    - maxParticipants: Maximum attendees
                    
                    **Validation:**
                    - Start time must be in the future
                    - End time must be after start time
                    - Cannot reduce maxParticipants below current enrollments
                    - maxParticipants must be >= template's minParticipants
                    
                    **Cannot Update:**
                    - ONGOING: Session is currently in progress
                    - COMPLETED: Session has ended
                    - CANCELLED: Session was cancelled
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Session updated successfully",
                    content = @Content(mediaType = "application/json",
                                     schema = @Schema(implementation = WorkshopSessionResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Validation failed or session cannot be edited (wrong status)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Valid JWT token required"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Vendor role required or not session owner"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Not Found - Session does not exist"
            )
    })
    @PutMapping("/sessions/{id}")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<WorkshopSessionResponse>> updateWorkshopSession(
                    @Parameter(description = "Workshop Session ID", required = true)
                    @PathVariable UUID id,
                    @Valid @RequestBody UpdateWorkshopSessionRequest request,
                    Principal principal) {
            WorkshopSessionResponse response = workshopSessionService.updateWorkshopSession(
                            principal.getName(),
                            id,
                            request);
            return ResponseEntity
                            .ok(ApiResponse.success(HttpStatus.OK, "Workshop session updated successfully",
                                            response));
    }

    // ==================== DELETE WORKSHOP SESSION ====================

    @Operation(
            summary = "Delete a workshop session (Vendor only)",
            description = """
                    Permanently deletes a workshop session. Only SCHEDULED sessions with no enrollments can be deleted.
                    
                    **Requirements:**
                    - Vendor role required
                    - Must own the session (via template ownership)
                    - Session must be in SCHEDULED status
                    - Session must have no enrollments
                    
                    **Cannot Delete:**
                    - Sessions with enrollments (use cancel instead)
                    - ONGOING, COMPLETED, or CANCELLED sessions
                    
                    **Warning:**
                    - This action is permanent and cannot be undone
                    - If the session has enrollments, use the cancel endpoint instead
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Session deleted successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Cannot delete session (has enrollments or wrong status)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Valid JWT token required"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Vendor role required or not session owner"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Not Found - Session does not exist"
            )
    })
    @DeleteMapping("/sessions/{id}")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<Void>> deleteWorkshopSession(
                    @Parameter(description = "Workshop Session ID to delete", required = true)
                    @PathVariable UUID id,
                    Principal principal) {
            workshopSessionService.deleteWorkshopSession(principal.getName(), id);
            return ResponseEntity
                            .ok(ApiResponse.success(HttpStatus.OK, "Workshop session deleted successfully", null));
    }

    @Operation(
            summary = "Cancel a workshop session (Vendor only)",
            description = """
                    Cancels a SCHEDULED workshop session, setting its status to CANCELLED.
                    
                    **Requirements:**
                    - Vendor role required
                    - Must own the session (via template ownership)
                    - Session must be in SCHEDULED status
                    
                    **Use Case:**
                    - Session has enrollments and needs to be cancelled
                    - Session cannot be deleted (hard delete) so it's cancelled instead (soft delete)
                    
                    **After Cancellation:**
                    - Status changes to CANCELLED
                    - Session is no longer bookable
                    - Enrolled users should be notified (out of scope)
                    - Refunds should be processed (out of scope)
                    
                    **Note:**
                    - This is a soft delete - the session record remains in the database
                    - Use this instead of delete when the session has enrollments
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Session cancelled successfully",
                    content = @Content(mediaType = "application/json",
                                     schema = @Schema(implementation = WorkshopSessionResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Session is not in SCHEDULED status"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Valid JWT token required"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - Vendor role required or not session owner"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Not Found - Session does not exist"
            )
    })
    @PostMapping("/sessions/{id}/cancel")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<WorkshopSessionResponse>> cancelWorkshopSession(
                    @Parameter(description = "Workshop Session ID to cancel", required = true)
                    @PathVariable UUID id,
                    Principal principal) {
            WorkshopSessionResponse response = workshopSessionService.cancelWorkshopSession(
                            principal.getName(),
                            id);
            return ResponseEntity
                            .ok(ApiResponse.success(HttpStatus.OK, "Workshop session cancelled successfully",
                                            response));
    }
}
