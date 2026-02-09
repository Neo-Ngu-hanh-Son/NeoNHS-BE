package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.constants.PaginationConstants;
import fpt.project.NeoNHS.dto.request.event.CreateEventRequest;
import fpt.project.NeoNHS.dto.request.event.EventFilterRequest;
import fpt.project.NeoNHS.dto.request.event.UpdateEventRequest;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.event.EventResponse;
import fpt.project.NeoNHS.enums.EventStatus;
import fpt.project.NeoNHS.service.EventService;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(
            @Valid @RequestBody CreateEventRequest request) {
        EventResponse event = eventService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "Event created successfully", event));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEventRequest request) {
        EventResponse event = eventService.updateEvent(id, request);
        return ResponseEntity.ok(ApiResponse.success("Event updated successfully", event));
    }

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
                .build();

        Sort sort = sortDir.equalsIgnoreCase(PaginationConstants.SORT_ASC)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<EventResponse> events = eventService.getAllEvents(filter, pageable);
        return ResponseEntity.ok(ApiResponse.success("Events retrieved successfully", events));
    }

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
                .build();

        List<EventResponse> events = eventService.getAllEvents(filter);
        return ResponseEntity.ok(ApiResponse.success("Events retrieved successfully", events));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> getEventById(@PathVariable UUID id) {
        EventResponse event = eventService.getEventById(id);
        return ResponseEntity.ok(ApiResponse.success("Event retrieved successfully", event));
    }
}
