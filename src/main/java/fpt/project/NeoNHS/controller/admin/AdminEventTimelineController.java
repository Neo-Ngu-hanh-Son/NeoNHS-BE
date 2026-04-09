package fpt.project.NeoNHS.controller.admin;

import fpt.project.NeoNHS.dto.request.event.EventTimelineRequest;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.event.EventPointResponse;
import fpt.project.NeoNHS.dto.response.event.EventPointTagResponse;
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
@RequestMapping("/api/admin/events/{eventId}/timelines")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Event Timelines", description = "Admin APIs for managing event timelines")
public class AdminEventTimelineController {

    private final EventTimelineService timelineService;

    @PostMapping
    @Operation(summary = "Create a new event timeline entry")
    public ResponseEntity<ApiResponse<EventTimelineResponse>> createTimeline(@PathVariable UUID eventId,
            @Valid @RequestBody EventTimelineRequest request) {
        EventTimelineResponse response = timelineService.createTimeline(eventId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "Timeline created successfully", response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing event timeline entry")
    public ResponseEntity<ApiResponse<EventTimelineResponse>> updateTimeline(@PathVariable UUID eventId,
            @PathVariable UUID id, @Valid @RequestBody EventTimelineRequest request) {
        EventTimelineResponse response = timelineService.updateTimeline(eventId, id, request);
        return ResponseEntity.ok(ApiResponse.success("Timeline updated successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get timeline by ID")
    public ResponseEntity<ApiResponse<EventTimelineResponse>> getTimelineById(@PathVariable UUID eventId,
            @PathVariable UUID id) {
        // Technically eventId could be verified against the timeline, but for
        // simplicity we rely on the timeline's ID.
        EventTimelineResponse response = timelineService.getTimelineById(id);
        return ResponseEntity.ok(ApiResponse.success("Timeline retrieved successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all timelines for a specific event")
    public ResponseEntity<ApiResponse<List<EventTimelineResponse>>> getTimelinesByEvent(@PathVariable UUID eventId) {
        List<EventTimelineResponse> response = timelineService.getTimelinesByEventId(eventId);
        return ResponseEntity.ok(ApiResponse.success("Timelines retrieved successfully", response));
    }

    @GetMapping("/event-points")
    @Operation(summary = "Get all event-points based on the current event ID")
    public ResponseEntity<ApiResponse<List<EventPointResponse>>> getAllEventPoints(@PathVariable UUID eventId) {
        List<EventPointResponse> response = timelineService.getAllEventPointByEventId(eventId);
        return ResponseEntity.ok(ApiResponse.success("Event-points retrieved successfully", response));
    }

    @GetMapping("/point-tags")
    @Operation(summary = "Get all event-points based on the current event ID")
    public ResponseEntity<ApiResponse<List<EventPointTagResponse>>> getAllEventPointTags(@PathVariable UUID eventId) {
        List<EventPointTagResponse> response = timelineService.getAllEventPointTagByEventId(eventId);
        return ResponseEntity.ok(ApiResponse.success("Event-points retrieved successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete timeline")
    public ResponseEntity<ApiResponse<Void>> deleteTimeline(@PathVariable UUID eventId, @PathVariable UUID id) {
        timelineService.deleteTimeline(id);
        return ResponseEntity.ok(ApiResponse.success("Timeline deleted successfully", null));
    }
}
