package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.request.ticketcatalog.CreateTicketCatalogRequest;
import fpt.project.NeoNHS.dto.request.ticketcatalog.UpdateTicketCatalogRequest;
import fpt.project.NeoNHS.dto.response.ticketcatalog.TicketCatalogResponse;

import java.util.List;
import java.util.UUID;

public interface TicketCatalogService {

    TicketCatalogResponse createTicketCatalog(UUID eventId, CreateTicketCatalogRequest request);

    TicketCatalogResponse updateTicketCatalog(UUID eventId, UUID ticketCatalogId, UpdateTicketCatalogRequest request);

    List<TicketCatalogResponse> getTicketCatalogsByEvent(UUID eventId);

    List<TicketCatalogResponse> getTicketCatalogsByEventForPublic(UUID eventId);

    TicketCatalogResponse getTicketCatalogById(UUID eventId, UUID ticketCatalogId);

    void softDeleteTicketCatalog(UUID eventId, UUID ticketCatalogId, UUID deletedBy);

    TicketCatalogResponse restoreTicketCatalog(UUID eventId, UUID ticketCatalogId);

    /**
     * Permanently delete a ticket catalog from the database.
     * Only allowed if no orders have ever been placed for this ticket catalog.
     * @param eventId Event ID the ticket catalog belongs to
     * @param ticketCatalogId Ticket Catalog ID to delete permanently
     */
    void hardDeleteTicketCatalog(UUID eventId, UUID ticketCatalogId);
}
