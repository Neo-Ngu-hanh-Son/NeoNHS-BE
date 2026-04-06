package fpt.project.NeoNHS.controller.admin;

import fpt.project.NeoNHS.dto.request.event.EventTimelineRequest;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.event.EventTimelineResponse;
import fpt.project.NeoNHS.service.EventTimelineService;
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
@RequestMapping("/api/admin/event-timelines")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Event Timelines", description = "Admin APIs for managing event timelines")
public class AdminEventTimelineController {

    private final EventTimelineService timelineService;

    @PostMapping
    @Operation(summary = "Create a new event timeline entry")
    public ResponseEntity<ApiResponse<EventTimelineResponse>> createTimeline(@Valid @RequestBody EventTimelineRequest request) {
        EventTimelineResponse response = timelineService.createTimeline(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "Timeline created successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing event timeline entry")
    public ResponseEntity<ApiResponse<EventTimelineResponse>> updateTimeline(@PathVariable UUID id, @Valid @RequestBody EventTimelineRequest request) {
        EventTimelineResponse response = timelineService.updateTimeline(id, request);
        return ResponseEntity.ok(ApiResponse.success("Timeline updated successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get timeline by ID")
    public ResponseEntity<ApiResponse<EventTimelineResponse>> getTimelineById(@PathVariable UUID id) {
        EventTimelineResponse response = timelineService.getTimelineById(id);
        return ResponseEntity.ok(ApiResponse.success("Timeline retrieved successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all timelines")
    public ResponseEntity<ApiResponse<List<EventTimelineResponse>>> getAllTimelines() {
        List<EventTimelineResponse> response = timelineService.getAllTimelines();
        return ResponseEntity.ok(ApiResponse.success("Timelines retrieved successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete timeline")
    public ResponseEntity<ApiResponse<Void>> deleteTimeline(@PathVariable UUID id) {
        timelineService.deleteTimeline(id);
        return ResponseEntity.ok(ApiResponse.success("Timeline deleted successfully", null));
    }
}
