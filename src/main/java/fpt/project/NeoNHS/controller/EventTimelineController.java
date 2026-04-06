package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.event.EventPointResponse;
import fpt.project.NeoNHS.dto.response.event.EventPointTagResponse;
import fpt.project.NeoNHS.dto.response.event.EventTimelineResponse;
import fpt.project.NeoNHS.dto.response.event.TimelineGroupedResponse;
import fpt.project.NeoNHS.service.EventPointService;
import fpt.project.NeoNHS.service.EventPointTagService;
import fpt.project.NeoNHS.service.EventTimelineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Event Timelines & Map", description = "Public APIs for browsing event timelines and map points")
public class EventTimelineController {

    private final EventTimelineService timelineService;
    private final EventPointService pointService;
    private final EventPointTagService tagService;

    @GetMapping("/events/{eventId}/timelines")
    @Operation(summary = "Get all timelines for a specific event, optionally filtered by date")
    public ResponseEntity<ApiResponse<List<EventTimelineResponse>>> getTimelinesByEvent(
            @Parameter(description = "Event ID") @PathVariable UUID eventId,
            @Parameter(description = "Filter by date (yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<EventTimelineResponse> response;
        if (date != null) {
            response = timelineService.getTimelinesByEventAndDate(eventId, date);
        } else {
            response = timelineService.getTimelinesByEventId(eventId);
        }
        return ResponseEntity.ok(ApiResponse.success("Timelines retrieved successfully", response));
    }

    @GetMapping("/events/{eventId}/timelines/grouped")
    @Operation(summary = "Get timelines grouped by date for a specific event")
    public ResponseEntity<ApiResponse<List<TimelineGroupedResponse>>> getTimelinesGroupedByDate(
            @Parameter(description = "Event ID") @PathVariable UUID eventId) {
        List<TimelineGroupedResponse> response = timelineService.getTimelinesByEventGroupedByDate(eventId);
        return ResponseEntity.ok(ApiResponse.success("Timelines grouped by date retrieved successfully", response));
    }

    @GetMapping("/event-timelines/{id}")
    @Operation(summary = "Get timeline entry by ID")
    public ResponseEntity<ApiResponse<EventTimelineResponse>> getTimelineById(
            @Parameter(description = "Timeline ID") @PathVariable UUID id) {
        EventTimelineResponse response = timelineService.getTimelineById(id);
        return ResponseEntity.ok(ApiResponse.success("Timeline retrieved successfully", response));
    }

    @GetMapping("/events/{eventId}/points")
    @Operation(summary = "Get all event points (map markers) for a specific event")
    public ResponseEntity<ApiResponse<List<EventPointResponse>>> getPointsByEvent(
            @Parameter(description = "Event ID") @PathVariable UUID eventId) {
        // TODO: Implement filtering by eventId when EventPoint has a direct relationship to Event
        List<EventPointResponse> response = pointService.getAllPoints();
        return ResponseEntity.ok(ApiResponse.success("Points retrieved successfully", response));
    }

    @GetMapping("/event-point-tags")
    @Operation(summary = "Get all event point tags (for filter chips UI)")
    public ResponseEntity<ApiResponse<List<EventPointTagResponse>>> getAllTags() {
        List<EventPointTagResponse> response = tagService.getAllTags();
        return ResponseEntity.ok(ApiResponse.success("Tags retrieved successfully", response));
    }
}
