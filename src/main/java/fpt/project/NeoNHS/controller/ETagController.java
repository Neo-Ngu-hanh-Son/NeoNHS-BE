package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.event.TagResponse;
import fpt.project.NeoNHS.service.ETagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Public controller for Event Tags.
 * Accessible without authentication.
 */
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@Tag(name = "Event Tags", description = "Public APIs for browsing event tags")
public class ETagController {

    private final ETagService eTagService;

    @Operation(
            summary = "Get all active event tags",
            description = "Retrieve all active (non-deleted) event tags for dropdown filter. Returns a flat list without pagination."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<TagResponse>>> getAllActiveTags() {
        List<TagResponse> tags = eTagService.getAllActiveTags();
        return ResponseEntity.ok(ApiResponse.success("Tags retrieved successfully", tags));
    }
}
