package fpt.project.NeoNHS.controller.admin;

import fpt.project.NeoNHS.constants.PaginationConstants;
import fpt.project.NeoNHS.dto.request.event.CreateTagRequest;
import fpt.project.NeoNHS.dto.request.event.UpdateTagRequest;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.event.TagResponse;
import fpt.project.NeoNHS.security.UserPrincipal;
import fpt.project.NeoNHS.service.ETagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Admin controller for Event Tag management.
 * All endpoints require ADMIN role.
 */
@RestController
@RequestMapping("/api/admin/tags")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Event Tags", description = "Admin APIs for managing event tags (requires ADMIN role)")
public class AdminETagController {

    private final ETagService eTagService;

    @Operation(
            summary = "Create tag",
            description = "Create a new event tag with name, description, color and icon"
    )
    @PostMapping
    public ResponseEntity<ApiResponse<TagResponse>> createTag(
            @Valid @RequestBody CreateTagRequest request) {
        TagResponse tag = eTagService.createTag(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "Tag created successfully", tag));
    }

    @Operation(
            summary = "Update tag",
            description = "Update an existing event tag by ID. Supports partial updates."
    )
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TagResponse>> updateTag(
            @Parameter(description = "Tag ID") @PathVariable UUID id,
            @Valid @RequestBody UpdateTagRequest request) {
        TagResponse tag = eTagService.updateTag(id, request);
        return ResponseEntity.ok(ApiResponse.success("Tag updated successfully", tag));
    }

    @Operation(
            summary = "Get all tags (Admin)",
            description = "Retrieve a paginated list of all tags, including soft-deleted ones"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<Page<TagResponse>>> getAllTags(
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SIZE) int size,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_DIR) String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase(PaginationConstants.SORT_ASC)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<TagResponse> tags = eTagService.getAllTags(pageable);
        return ResponseEntity.ok(ApiResponse.success("Tags retrieved successfully", tags));
    }

    @Operation(
            summary = "Get tag by ID",
            description = "Retrieve detailed information of a specific tag"
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TagResponse>> getTagById(
            @Parameter(description = "Tag ID") @PathVariable UUID id) {
        TagResponse tag = eTagService.getTagById(id);
        return ResponseEntity.ok(ApiResponse.success("Tag retrieved successfully", tag));
    }

    @Operation(
            summary = "Soft delete tag",
            description = "Soft delete a tag by setting deletedAt and deletedBy fields. The tag can be restored later."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTag(
            @Parameter(description = "Tag ID") @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        eTagService.softDeleteTag(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Tag deleted successfully", null));
    }

    @Operation(
            summary = "Restore tag",
            description = "Restore a soft-deleted tag by clearing deletedAt and deletedBy fields"
    )
    @PatchMapping("/{id}/restore")
    public ResponseEntity<ApiResponse<TagResponse>> restoreTag(
            @Parameter(description = "Tag ID") @PathVariable UUID id) {
        TagResponse tag = eTagService.restoreTag(id);
        return ResponseEntity.ok(ApiResponse.success("Tag restored successfully", tag));
    }
}
