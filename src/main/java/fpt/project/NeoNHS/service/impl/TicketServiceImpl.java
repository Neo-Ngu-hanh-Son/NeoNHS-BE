package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.dto.response.TicketDetailResponse;
import fpt.project.NeoNHS.entity.Ticket;
import fpt.project.NeoNHS.enums.TicketStatus;
import fpt.project.NeoNHS.enums.TicketType;
import fpt.project.NeoNHS.exception.BadRequestException;
import fpt.project.NeoNHS.exception.ForbiddenException;
import fpt.project.NeoNHS.exception.ResourceNotFoundException;
import fpt.project.NeoNHS.exception.UnauthorizedException;
import fpt.project.NeoNHS.repository.TicketRepository;
import fpt.project.NeoNHS.security.UserPrincipal;
import fpt.project.NeoNHS.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;

    @Override
    public TicketDetailResponse verifyTicketViaCode(String code) {
        UserPrincipal currentUser = getCurrentUserPrincipal();

        Ticket ticket = ticketRepository.findByTicketCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with code: " + code));

        // Role-based Access Control
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isVendor = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_VENDOR"));

        if (isAdmin) {
            // ADMIN only verifies EVENT and ENTRANCE
            if (ticket.getTicketType() != TicketType.EVENT && ticket.getTicketType() != TicketType.ENTRANCE) {
                throw new ForbiddenException("ADMIN is only allowed to verify EVENT and ENTRANCE tickets");
            }
        } else if (isVendor) {
            // VENDOR only verifies WORKSHOP
            if (ticket.getTicketType() != TicketType.WORKSHOP) {
                throw new ForbiddenException("VENDOR is only allowed to verify WORKSHOP tickets");
            }
            // Check if this workshop belongs to the current vendor
            if (ticket.getWorkshopSession() == null ||
                    ticket.getWorkshopSession().getWorkshopTemplate() == null ||
                    ticket.getWorkshopSession().getWorkshopTemplate().getVendor() == null ||
                    ticket.getWorkshopSession().getWorkshopTemplate().getVendor().getUser() == null ||
                    !ticket.getWorkshopSession().getWorkshopTemplate().getVendor().getUser().getId().equals(currentUser.getId())) {
                throw new ForbiddenException("You can only verify tickets for your own workshops");
            }
        } else {
            throw new ForbiddenException("You do not have permission to verify tickets");
        }

        if (ticket.getStatus() == TicketStatus.USED) {
            throw new BadRequestException("Ticket is already used");
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

    private UserPrincipal getCurrentUserPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("User is not authenticated");
        }
        if (!(auth.getPrincipal() instanceof UserPrincipal)) {
            throw new UnauthorizedException("Invalid authentication principal");
        }
        return (UserPrincipal) auth.getPrincipal();
    }
}
