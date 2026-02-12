package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.dto.request.ticketcatalog.CreateTicketCatalogRequest;
import fpt.project.NeoNHS.dto.request.ticketcatalog.UpdateTicketCatalogRequest;
import fpt.project.NeoNHS.dto.response.ticketcatalog.TicketCatalogResponse;
import fpt.project.NeoNHS.entity.Event;
import fpt.project.NeoNHS.entity.TicketCatalog;
import fpt.project.NeoNHS.enums.TicketCatalogStatus;
import fpt.project.NeoNHS.exception.BadRequestException;
import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.repository.EventRepository;
import fpt.project.NeoNHS.repository.OrderDetailRepository;
import fpt.project.NeoNHS.repository.TicketCatalogRepository;
import fpt.project.NeoNHS.service.TicketCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketCatalogServiceImpl implements TicketCatalogService {

    private final TicketCatalogRepository ticketCatalogRepository;
    private final EventRepository eventRepository;
    private final OrderDetailRepository orderDetailRepository;

    @Override
    @Transactional
    public TicketCatalogResponse createTicketCatalog(UUID eventId, CreateTicketCatalogRequest request) {
        Event event = getActiveEvent(eventId);

        // Validate business rules
        validateCreateRequest(request);

        TicketCatalog ticketCatalog = TicketCatalog.builder()
                .name(request.getName())
                .description(request.getDescription())
                .customerType(request.getCustomerType())
                .price(request.getPrice())
                .originalPrice(request.getOriginalPrice())
                .applyOnDays(request.getApplyOnDays())
                .validFromDate(request.getValidFromDate())
                .validToDate(request.getValidToDate())
                .totalQuota(request.getTotalQuota())
                .soldQuantity(0)
                .status(request.getStatus() != null ? request.getStatus() : TicketCatalogStatus.ACTIVE)
                .event(event)
                .build();

        TicketCatalog saved = ticketCatalogRepository.save(ticketCatalog);
        return TicketCatalogResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public TicketCatalogResponse updateTicketCatalog(UUID eventId, UUID ticketCatalogId, UpdateTicketCatalogRequest request) {
        eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        TicketCatalog ticketCatalog = getTicketCatalogBelongingToEvent(eventId, ticketCatalogId);

        // Update fields (partial update pattern like EventServiceImpl.updateEvent)
        if (request.getName() != null) {
            ticketCatalog.setName(request.getName());
        }
        if (request.getDescription() != null) {
            ticketCatalog.setDescription(request.getDescription());
        }
        if (request.getCustomerType() != null) {
            ticketCatalog.setCustomerType(request.getCustomerType());
        }
        if (request.getPrice() != null) {
            ticketCatalog.setPrice(request.getPrice());
        }
        if (request.getOriginalPrice() != null) {
            ticketCatalog.setOriginalPrice(request.getOriginalPrice());
        }
        if (request.getApplyOnDays() != null) {
            ticketCatalog.setApplyOnDays(request.getApplyOnDays());
        }
        if (request.getValidFromDate() != null) {
            ticketCatalog.setValidFromDate(request.getValidFromDate());
        }
        if (request.getValidToDate() != null) {
            ticketCatalog.setValidToDate(request.getValidToDate());
        }
        if (request.getTotalQuota() != null) {
            ticketCatalog.setTotalQuota(request.getTotalQuota());
        }
        if (request.getStatus() != null) {
            ticketCatalog.setStatus(request.getStatus());
        }

        // Validate after updates
        validateAfterUpdate(ticketCatalog);

        // Auto-update status to SOLD_OUT if soldQuantity >= totalQuota
        autoUpdateSoldOutStatus(ticketCatalog);

        TicketCatalog updated = ticketCatalogRepository.save(ticketCatalog);
        return TicketCatalogResponse.fromEntity(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketCatalogResponse> getTicketCatalogsByEvent(UUID eventId) {
        eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        return ticketCatalogRepository.findByEventId(eventId).stream()
                .map(TicketCatalogResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketCatalogResponse> getTicketCatalogsByEventForPublic(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        if (event.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Event not found with id: " + eventId);
        }

        return ticketCatalogRepository.findByEventIdAndDeletedAtIsNull(eventId).stream()
                .map(TicketCatalogResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TicketCatalogResponse getTicketCatalogById(UUID eventId, UUID ticketCatalogId) {
        eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        TicketCatalog ticketCatalog = getTicketCatalogBelongingToEvent(eventId, ticketCatalogId);
        return TicketCatalogResponse.fromEntity(ticketCatalog);
    }

    @Override
    @Transactional
    public void softDeleteTicketCatalog(UUID eventId, UUID ticketCatalogId, UUID deletedBy) {
        eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        TicketCatalog ticketCatalog = getTicketCatalogBelongingToEvent(eventId, ticketCatalogId);

        if (ticketCatalog.getDeletedAt() != null) {
            throw new BadRequestException("Ticket catalog is already deleted");
        }

        ticketCatalog.setDeletedAt(LocalDateTime.now());
        ticketCatalog.setDeletedBy(deletedBy);
        ticketCatalogRepository.save(ticketCatalog);
    }

    @Override
    @Transactional
    public TicketCatalogResponse restoreTicketCatalog(UUID eventId, UUID ticketCatalogId) {
        eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        TicketCatalog ticketCatalog = getTicketCatalogBelongingToEvent(eventId, ticketCatalogId);

        if (ticketCatalog.getDeletedAt() == null) {
            throw new BadRequestException("Ticket catalog is not deleted");
        }

        ticketCatalog.setDeletedAt(null);
        ticketCatalog.setDeletedBy(null);
        TicketCatalog restored = ticketCatalogRepository.save(ticketCatalog);
        return TicketCatalogResponse.fromEntity(restored);
    }

    @Override
    @Transactional
    public void hardDeleteTicketCatalog(UUID eventId, UUID ticketCatalogId) {
        eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        TicketCatalog ticketCatalog = getTicketCatalogBelongingToEvent(eventId, ticketCatalogId);

        if (orderDetailRepository.existsByTicketCatalogId(ticketCatalogId)) {
            throw new BadRequestException("Cannot permanently delete ticket catalog that has been ordered");
        }

        ticketCatalogRepository.delete(ticketCatalog);
    }

    // ==================== Private helper methods ====================

    private Event getActiveEvent(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        if (event.getDeletedAt() != null) {
            throw new BadRequestException("Cannot manage ticket catalog for a deleted event");
        }
        return event;
    }

    private TicketCatalog getTicketCatalogBelongingToEvent(UUID eventId, UUID ticketCatalogId) {
        return ticketCatalogRepository.findByEventIdAndId(eventId, ticketCatalogId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ticket catalog not found with id: " + ticketCatalogId + " for event: " + eventId));
    }

    private void validateCreateRequest(CreateTicketCatalogRequest request) {
        if (request.getOriginalPrice() != null && request.getOriginalPrice().compareTo(request.getPrice()) < 0) {
            throw new BadRequestException("Original price must be greater than or equal to price");
        }

        if (request.getValidFromDate() != null && request.getValidToDate() != null
                && request.getValidToDate().isBefore(request.getValidFromDate())) {
            throw new BadRequestException("Valid to date must be after valid from date");
        }
    }

    private void validateAfterUpdate(TicketCatalog ticketCatalog) {
        BigDecimal price = ticketCatalog.getPrice();
        BigDecimal originalPrice = ticketCatalog.getOriginalPrice();

        if (originalPrice != null && price != null && originalPrice.compareTo(price) < 0) {
            throw new BadRequestException("Original price must be greater than or equal to price");
        }

        LocalDateTime validFrom = ticketCatalog.getValidFromDate();
        LocalDateTime validTo = ticketCatalog.getValidToDate();

        if (validFrom != null && validTo != null && validTo.isBefore(validFrom)) {
            throw new BadRequestException("Valid to date must be after valid from date");
        }
    }

    private void autoUpdateSoldOutStatus(TicketCatalog ticketCatalog) {
        if (ticketCatalog.getTotalQuota() != null && ticketCatalog.getSoldQuantity() != null
                && ticketCatalog.getSoldQuantity() >= ticketCatalog.getTotalQuota()) {
            ticketCatalog.setStatus(TicketCatalogStatus.SOLD_OUT);
        }
    }
}
