package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.constants.PaginationConstants;
import fpt.project.NeoNHS.dto.request.event.EventFilterRequest;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.event.EventResponse;
import fpt.project.NeoNHS.enums.EventStatus;
import fpt.project.NeoNHS.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Public controller for Event.
 * All endpoints are accessible without authentication.
 * Only shows non-deleted events.
 */
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "Events", description = "Public APIs for browsing events")
public class EventController {

    private final EventService eventService;

    @Operation(
            summary = "Get all events",
            description = "Retrieve a paginated list of events with optional filters. Only returns active (non-deleted) events."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<Page<EventResponse>>> getAllEvents(
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SIZE) int size,
            @RequestParam(required = false) EventStatus status,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) List<UUID> tagIds,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = PaginationConstants.DEFAULT_SORT_DIR) String sortDir) {

        EventFilterRequest filter = EventFilterRequest.builder()
                .status(status)
                .name(name)
                .location(location)
                .startDate(startDate)
                .endDate(endDate)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .tagIds(tagIds)
                .deleted(false)
                .includeDeleted(false)
                .build();

        Sort sort = sortDir.equalsIgnoreCase(PaginationConstants.SORT_ASC)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<EventResponse> events = eventService.getAllEvents(filter, pageable);
        return ResponseEntity.ok(ApiResponse.success("Events retrieved successfully", events));
    }

    @Operation(
            summary = "Get all events without pagination",
            description = "Retrieve all events without pagination. Only returns active (non-deleted) events."
    )
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getAllEventsWithoutPagination(
            @RequestParam(required = false) EventStatus status,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) List<UUID> tagIds) {

        EventFilterRequest filter = EventFilterRequest.builder()
                .status(status)
                .name(name)
                .location(location)
                .startDate(startDate)
                .endDate(endDate)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .tagIds(tagIds)
                .deleted(false)
                .includeDeleted(false)
                .build();

        List<EventResponse> events = eventService.getAllEvents(filter);
        return ResponseEntity.ok(ApiResponse.success("Events retrieved successfully", events));
    }

    @Operation(
            summary = "Get event by ID",
            description = "Retrieve detailed information of a specific event. Only returns if the event is not deleted."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> getEventById(
            @Parameter(description = "Event ID") @PathVariable UUID id) {
        EventResponse event = eventService.getEventById(id);
        return ResponseEntity.ok(ApiResponse.success("Event retrieved successfully", event));
    }
}
