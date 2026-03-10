package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.constants.PaginationConstants;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.workshop.WorkshopSessionResponse;
import fpt.project.NeoNHS.dto.response.workshop.WorkshopTemplateResponse;
import fpt.project.NeoNHS.service.WorkshopSessionService;
import fpt.project.NeoNHS.service.WorkshopTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/workshops")
@RequiredArgsConstructor
@Tag(name = "Workshop (Public)", description = "Public APIs for tourists to browse workshop templates and sessions")
public class WorkshopTouristController {

    private final WorkshopTemplateService workshopTemplateService;
    private final WorkshopSessionService workshopSessionService;

    // ==================== WORKSHOP TEMPLATES ====================

    @GetMapping("/templates")
    @Operation(
            summary = "Get all active workshop templates",
            description = """
                    Returns a paginated list of all workshop templates with status **ACTIVE**.
                    
                    Only templates approved by admin are visible here.
                    Supports sorting by: `createdAt`, `name`, `defaultPrice`, `averageRating`, `estimatedDuration`.
                    
                    **No authentication required.**
                    """
    )
    public ResponseEntity<ApiResponse<Page<WorkshopTemplateResponse>>> getActiveTemplates(
            @Parameter(description = "Page number (starts from 1)", example = "1")
            @RequestParam(defaultValue = "1") int page,
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

        Page<WorkshopTemplateResponse> response = workshopTemplateService.getActiveWorkshopTemplates(pageable);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Active workshop templates retrieved successfully", response));
    }

    @GetMapping("/templates/{id}")
    @Operation(
            summary = "Get workshop template detail",
            description = """
                    Returns the full detail of a single workshop template.
                    
                    Only templates with status **ACTIVE** are accessible via this endpoint.
                    Returns **404** if the template does not exist or is not active.
                    
                    **No authentication required.**
                    """
    )
    public ResponseEntity<ApiResponse<WorkshopTemplateResponse>> getTemplateDetail(
            @Parameter(description = "Workshop Template ID", required = true)
            @PathVariable UUID id) {

        WorkshopTemplateResponse response = workshopTemplateService.getActiveWorkshopTemplateById(id);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Workshop template retrieved successfully", response));
    }

    @GetMapping("/templates/search")
    @Operation(
            summary = "Search and filter active workshop templates",
            description = """
                    Search and filter active workshop templates with multiple criteria.
                    All parameters are **optional** and can be combined freely.
                    
                    **Filter options:**
                    - `keyword` — Search by name, short description, or full description
                    - `tagId` — Filter by a specific WTag UUID
                    - `minPrice` / `maxPrice` — Filter by price range (defaultPrice)
                    - `minDuration` / `maxDuration` — Filter by duration in minutes
                    - `minRating` — Filter by minimum average rating (0.0 – 5.0)
                    
                    **No authentication required.**
                    """
    )
    public ResponseEntity<ApiResponse<Page<WorkshopTemplateResponse>>> searchTemplates(
            @Parameter(description = "Keyword to search name/description", example = "bánh")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "Filter by WTag ID")
            @RequestParam(required = false) UUID tagId,
            @Parameter(description = "Minimum price", example = "100000")
            @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Maximum price", example = "1000000")
            @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "Minimum duration (minutes)", example = "60")
            @RequestParam(required = false) Integer minDuration,
            @Parameter(description = "Maximum duration (minutes)", example = "240")
            @RequestParam(required = false) Integer maxDuration,
            @Parameter(description = "Minimum average rating (0.0 - 5.0)", example = "4.0")
            @RequestParam(required = false) BigDecimal minRating,
            @Parameter(description = "Page number (starts from 1)", example = "1")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SIZE) int size,
            @Parameter(description = "Field to sort by", example = "averageRating")
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_BY) String sortBy,
            @Parameter(description = "Sort direction: asc or desc", example = "desc")
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_DIR) String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase(PaginationConstants.SORT_ASC)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        Page<WorkshopTemplateResponse> response = workshopTemplateService.searchAndFilterActiveTemplates(
                keyword, tagId, minPrice, maxPrice, minDuration, maxDuration, minRating, pageable);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Workshop templates retrieved successfully", response));
    }

    // ==================== WORKSHOP SESSIONS ====================

    @GetMapping("/templates/{id}/sessions")
    @Operation(
            summary = "Get upcoming sessions of a workshop template",
            description = """
                    Returns a paginated list of **upcoming (SCHEDULED)** sessions for a specific active workshop template.
                    
                    Only sessions with:
                    - Status = `SCHEDULED`
                    - `startTime` in the **future**
                    
                    are returned.
                    
                    Returns **404** if the template does not exist or is not active.
                    
                    **No authentication required.**
                    """
    )
    public ResponseEntity<ApiResponse<Page<WorkshopSessionResponse>>> getSessionsByTemplate(
            @Parameter(description = "Workshop Template ID", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Page number (starts from 1)", example = "1")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SIZE) int size,
            @Parameter(description = "Field to sort by", example = "startTime")
            @RequestParam(defaultValue = "startTime") String sortBy,
            @Parameter(description = "Sort direction: asc or desc", example = "asc")
            @RequestParam(defaultValue = PaginationConstants.SORT_ASC) String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase(PaginationConstants.SORT_ASC)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        Page<WorkshopSessionResponse> response = workshopSessionService.getUpcomingSessionsByTemplateId(id, pageable);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Upcoming sessions retrieved successfully", response));
    }
}
