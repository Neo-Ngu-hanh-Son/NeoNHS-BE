package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.event.EventTimelineResponse;
import fpt.project.NeoNHS.service.EventTimelineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/event-timelines")
@RequiredArgsConstructor
@Tag(name = "Event Timelines", description = "Public APIs for browsing event timelines")
public class EventTimelineController {

    private final EventTimelineService timelineService;

    @GetMapping("/event/{eventId}")
    @Operation(summary = "Get all timelines for a specific event")
    public ResponseEntity<ApiResponse<List<EventTimelineResponse>>> getTimelinesByEvent(
            @Parameter(description = "Event ID") @PathVariable UUID eventId) {
        List<EventTimelineResponse> response = timelineService.getTimelinesByEventId(eventId);
        return ResponseEntity.ok(ApiResponse.success("Timelines retrieved successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get timeline entry by ID")
    public ResponseEntity<ApiResponse<EventTimelineResponse>> getTimelineById(
            @Parameter(description = "Timeline ID") @PathVariable UUID id) {
        EventTimelineResponse response = timelineService.getTimelineById(id);
        return ResponseEntity.ok(ApiResponse.success("Timeline retrieved successfully", response));
    }
}
