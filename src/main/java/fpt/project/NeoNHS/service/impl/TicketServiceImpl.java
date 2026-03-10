package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.dto.response.TicketDetailResponse;
import fpt.project.NeoNHS.entity.Ticket;
import fpt.project.NeoNHS.enums.TicketStatus;
import fpt.project.NeoNHS.repository.TicketRepository;
import fpt.project.NeoNHS.service.TicketService;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;

    @Override
    public TicketDetailResponse verifyTicketViaCode(String code) {
        Ticket ticket = ticketRepository.findByTicketCode(code)
                .orElseThrow(() -> new fpt.project.NeoNHS.exception.ResourceNotFoundException(
                        "Ticket not found with code: " + code));

        if (ticket.getStatus() == TicketStatus.USED) {
            throw new fpt.project.NeoNHS.exception.BadRequestException("Ticket is already used");
        }

        if (ticket.getStatus() == TicketStatus.EXPIRED ||
                (ticket.getExpiryDate() != null && ticket.getExpiryDate().isBefore(LocalDateTime.now()))) {
            if (ticket.getStatus() != TicketStatus.EXPIRED) {
                ticket.setStatus(TicketStatus.EXPIRED);
                ticketRepository.save(ticket);
            }
            throw new fpt.project.NeoNHS.exception.BadRequestException("Ticket is expired");
        }

        ticket.setStatus(TicketStatus.USED);
        ticket.setRedeemedAt(LocalDateTime.now());
        ticketRepository.save(ticket);

        String itemName = "Unknown Type";
        LocalDateTime validFrom = ticket.getIssueDate();

        if (ticket.getTicketCatalog() != null) {
            itemName = ticket.getTicketCatalog().getName();
            if (ticket.getTicketCatalog().getValidFromDate() != null) {
                validFrom = ticket.getTicketCatalog().getValidFromDate();
            }
        } else if (ticket.getWorkshopSession() != null && ticket.getWorkshopSession().getWorkshopTemplate() != null) {
            itemName = ticket.getWorkshopSession().getWorkshopTemplate().getName();
            validFrom = ticket.getWorkshopSession().getStartTime();
        }

        return TicketDetailResponse.builder()
                .id(ticket.getId())
                .ticketCode(ticket.getTicketCode())
                .qrCode(ticket.getQrCode())
                .ticketType(ticket.getTicketType().name())
                .status(ticket.getStatus().name())
                .itemName(itemName)
                .validFrom(validFrom)
                .validTo(ticket.getExpiryDate())
                .build();
    }

}
