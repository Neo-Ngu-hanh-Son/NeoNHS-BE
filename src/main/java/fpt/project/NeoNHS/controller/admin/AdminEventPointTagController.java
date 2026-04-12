package fpt.project.NeoNHS.controller.admin;

import fpt.project.NeoNHS.dto.request.event.EventPointTagRequest;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.event.EventPointTagResponse;
import fpt.project.NeoNHS.service.EventPointTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/event-point-tags")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Event Point Tags", description = "Admin APIs for managing event point tags")
public class AdminEventPointTagController {

    private final EventPointTagService tagService;

    @PostMapping
    @Operation(summary = "Create a new event point tag")
    public ResponseEntity<ApiResponse<EventPointTagResponse>> createTag(
            @Valid @RequestBody EventPointTagRequest request) {
        EventPointTagResponse response = tagService.createTag(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "Tag created successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing event point tag")
    public ResponseEntity<ApiResponse<EventPointTagResponse>> updateTag(
            @PathVariable UUID id, @Valid @RequestBody EventPointTagRequest request) {
        EventPointTagResponse response = tagService.updateTag(id, request);
        return ResponseEntity.ok(ApiResponse.success("Tag updated successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get tag by ID")
    public ResponseEntity<ApiResponse<EventPointTagResponse>> getTagById(@PathVariable UUID id) {
        EventPointTagResponse response = tagService.getTagById(id);
        return ResponseEntity.ok(ApiResponse.success("Tag retrieved successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all tags")
    public ResponseEntity<ApiResponse<List<EventPointTagResponse>>> getAllTags() {
        List<EventPointTagResponse> response = tagService.getAllTags();
        return ResponseEntity.ok(ApiResponse.success("Tags retrieved successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete tag")
    public ResponseEntity<ApiResponse<Void>> deleteTag(@PathVariable UUID id) {
        tagService.deleteTag(id);
        return ResponseEntity.ok(ApiResponse.success("Tag deleted successfully", null));
    }
}
