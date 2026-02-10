package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.response.ticket.TicketCatalogResponse;

import java.util.List;
import java.util.UUID;

public interface TicketCatalogService {
    List<TicketCatalogResponse> getTicketCatalogsByEvent(UUID eventId);

    List<TicketCatalogResponse> getTicketCatalogsByAttraction(UUID attractionId);
}
