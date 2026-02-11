package fpt.project.NeoNHS.controller.admin;

import fpt.project.NeoNHS.dto.request.ticketcatalog.CreateTicketCatalogRequest;
import fpt.project.NeoNHS.dto.request.ticketcatalog.UpdateTicketCatalogRequest;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.ticketcatalog.TicketCatalogResponse;
import fpt.project.NeoNHS.security.UserPrincipal;
import fpt.project.NeoNHS.service.TicketCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Admin controller for Ticket Catalog management.
 * All endpoints require ADMIN role.
 */
@RestController
@RequestMapping("/api/admin/events/{eventId}/ticket-catalogs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Ticket Catalogs", description = "Admin APIs for managing ticket catalogs per event (requires ADMIN role)")
public class AdminTicketCatalogController {

    private final TicketCatalogService ticketCatalogService;

    @Operation(
            summary = "Create ticket catalog",
            description = "Create a new ticket catalog for an event. Used to define ticket types (e.g. Adult, Child, VIP, Early Bird) with pricing rules and quota."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<TicketCatalogResponse>> createTicketCatalog(
            @Parameter(description = "Event ID") @PathVariable UUID eventId,
            @Valid @RequestBody CreateTicketCatalogRequest request) {
        TicketCatalogResponse ticketCatalog = ticketCatalogService.createTicketCatalog(eventId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "Ticket catalog created successfully", ticketCatalog));
    }

    @Operation(
            summary = "Update ticket catalog",
            description = "Update an existing ticket catalog by ID. Supports partial updates."
    )
    @PutMapping("/{ticketCatalogId}")
    public ResponseEntity<ApiResponse<TicketCatalogResponse>> updateTicketCatalog(
            @Parameter(description = "Event ID") @PathVariable UUID eventId,
            @Parameter(description = "Ticket Catalog ID") @PathVariable UUID ticketCatalogId,
            @Valid @RequestBody UpdateTicketCatalogRequest request) {
        TicketCatalogResponse ticketCatalog = ticketCatalogService.updateTicketCatalog(eventId, ticketCatalogId, request);
        return ResponseEntity.ok(ApiResponse.success("Ticket catalog updated successfully", ticketCatalog));
    }

    @Operation(
            summary = "Get all ticket catalogs for event (Admin)",
            description = "Retrieve all ticket catalogs for an event, including soft-deleted ones"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<TicketCatalogResponse>>> getTicketCatalogsByEvent(
            @Parameter(description = "Event ID") @PathVariable UUID eventId) {
        List<TicketCatalogResponse> ticketCatalogs = ticketCatalogService.getTicketCatalogsByEvent(eventId);
        return ResponseEntity.ok(ApiResponse.success("Ticket catalogs retrieved successfully", ticketCatalogs));
    }

    @Operation(
            summary = "Get ticket catalog by ID (Admin)",
            description = "Retrieve detailed information of a specific ticket catalog, including deleted ones"
    )
    @GetMapping("/{ticketCatalogId}")
    public ResponseEntity<ApiResponse<TicketCatalogResponse>> getTicketCatalogById(
            @Parameter(description = "Event ID") @PathVariable UUID eventId,
            @Parameter(description = "Ticket Catalog ID") @PathVariable UUID ticketCatalogId) {
        TicketCatalogResponse ticketCatalog = ticketCatalogService.getTicketCatalogById(eventId, ticketCatalogId);
        return ResponseEntity.ok(ApiResponse.success("Ticket catalog retrieved successfully", ticketCatalog));
    }

    @Operation(
            summary = "Soft delete ticket catalog",
            description = "Soft delete a ticket catalog by setting deletedAt and deletedBy fields. The ticket catalog can be restored later."
    )
    @DeleteMapping("/{ticketCatalogId}")
    public ResponseEntity<ApiResponse<Void>> deleteTicketCatalog(
            @Parameter(description = "Event ID") @PathVariable UUID eventId,
            @Parameter(description = "Ticket Catalog ID") @PathVariable UUID ticketCatalogId,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        ticketCatalogService.softDeleteTicketCatalog(eventId, ticketCatalogId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Ticket catalog deleted successfully", null));
    }

    @Operation(
            summary = "Restore ticket catalog",
            description = "Restore a soft-deleted ticket catalog by clearing deletedAt and deletedBy fields"
    )
    @PatchMapping("/{ticketCatalogId}/restore")
    public ResponseEntity<ApiResponse<TicketCatalogResponse>> restoreTicketCatalog(
            @Parameter(description = "Event ID") @PathVariable UUID eventId,
            @Parameter(description = "Ticket Catalog ID") @PathVariable UUID ticketCatalogId) {
        TicketCatalogResponse ticketCatalog = ticketCatalogService.restoreTicketCatalog(eventId, ticketCatalogId);
        return ResponseEntity.ok(ApiResponse.success("Ticket catalog restored successfully", ticketCatalog));
    }
}
