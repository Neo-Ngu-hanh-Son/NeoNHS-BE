package fpt.project.NeoNHS.controller.admin;

import fpt.project.NeoNHS.constants.PaginationConstants;
import fpt.project.NeoNHS.dto.request.event.CreateEventRequest;
import fpt.project.NeoNHS.dto.request.event.EventFilterRequest;
import fpt.project.NeoNHS.dto.request.event.UpdateEventRequest;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.event.EventResponse;
import fpt.project.NeoNHS.enums.EventStatus;
import fpt.project.NeoNHS.security.UserPrincipal;
import fpt.project.NeoNHS.service.EventService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Admin controller for Event management.
 * All endpoints require ADMIN role.
 */
@RestController
@RequestMapping("/api/admin/events")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Events", description = "Admin APIs for managing events (requires ADMIN role)")
public class AdminEventController {

    private final EventService eventService;

    @Operation(
            summary = "Create event",
            description = "Create a new event with the provided details"
    )
    @PostMapping
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(
            @Valid @RequestBody CreateEventRequest request) {
        EventResponse event = eventService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "Event created successfully", event));
    }

    @Operation(
            summary = "Update event",
            description = "Update an existing event by ID"
    )
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(
            @Parameter(description = "Event ID") @PathVariable UUID id,
            @Valid @RequestBody UpdateEventRequest request) {
        EventResponse event = eventService.updateEvent(id, request);
        return ResponseEntity.ok(ApiResponse.success("Event updated successfully", event));
    }

    @Operation(
            summary = "Get all events (Admin)",
            description = "Retrieve a paginated list of events with optional filters. Admin can filter by deleted status using 'deleted' and 'includeDeleted' parameters."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<Page<EventResponse>>> getAllEventsForAdmin(
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
            @RequestParam(required = false) Boolean deleted,
            @RequestParam(defaultValue = "false") Boolean includeDeleted,
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
                .deleted(deleted)
                .includeDeleted(includeDeleted)
                .build();

        Sort sort = sortDir.equalsIgnoreCase(PaginationConstants.SORT_ASC)
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<EventResponse> events = eventService.getAllEvents(filter, pageable);
        return ResponseEntity.ok(ApiResponse.success("Events retrieved successfully", events));
    }

    @Operation(
            summary = "Get event by ID (Admin)",
            description = "Retrieve detailed information of a specific event, including deleted events"
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> getEventByIdForAdmin(
            @Parameter(description = "Event ID") @PathVariable UUID id) {
        EventResponse event = eventService.getEventByIdForAdmin(id);
        return ResponseEntity.ok(ApiResponse.success("Event retrieved successfully", event));
    }

    @Operation(
            summary = "Soft delete event",
            description = "Soft delete an event by setting deletedAt and deletedBy fields. The event can be restored later."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(
            @Parameter(description = "Event ID") @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        eventService.softDeleteEvent(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Event deleted successfully", null));
    }

    @Operation(
            summary = "Restore event",
            description = "Restore a soft-deleted event by clearing deletedAt and deletedBy fields"
    )
    @PatchMapping("/{id}/restore")
    public ResponseEntity<ApiResponse<EventResponse>> restoreEvent(
            @Parameter(description = "Event ID") @PathVariable UUID id) {
        EventResponse event = eventService.restoreEvent(id);
        return ResponseEntity.ok(ApiResponse.success("Event restored successfully", event));
    }
}
