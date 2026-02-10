package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.dto.response.ticket.TicketCatalogResponse;
import fpt.project.NeoNHS.entity.TicketCatalog;
import fpt.project.NeoNHS.repository.TicketCatalogRepository;
import fpt.project.NeoNHS.service.TicketCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketCatalogServiceImpl implements TicketCatalogService {

    private final TicketCatalogRepository ticketCatalogRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TicketCatalogResponse> getTicketCatalogsByEvent(UUID eventId) {
        List<TicketCatalog> ticketCatalogs = ticketCatalogRepository.findByEventId(eventId);
        return ticketCatalogs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketCatalogResponse> getTicketCatalogsByAttraction(UUID attractionId) {
        List<TicketCatalog> ticketCatalogs = ticketCatalogRepository.findByAttractionId(attractionId);
        return ticketCatalogs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private TicketCatalogResponse mapToResponse(TicketCatalog ticketCatalog) {
        return TicketCatalogResponse.builder()
                .id(ticketCatalog.getId())
                .name(ticketCatalog.getName())
                .description(ticketCatalog.getDescription())
                .customerType(ticketCatalog.getCustomerType())
                .price(ticketCatalog.getPrice())
                .originalPrice(ticketCatalog.getOriginalPrice())
                .applyOnDays(ticketCatalog.getApplyOnDays())
                .validFromDate(ticketCatalog.getValidFromDate())
                .validToDate(ticketCatalog.getValidToDate())
                .totalQuota(ticketCatalog.getTotalQuota())
                .status(ticketCatalog.getStatus())
                .build();
    }
}
