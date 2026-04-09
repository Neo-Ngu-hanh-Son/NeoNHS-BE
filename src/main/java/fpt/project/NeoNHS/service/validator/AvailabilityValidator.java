package fpt.project.NeoNHS.service.validator;

import fpt.project.NeoNHS.entity.Event;
import fpt.project.NeoNHS.entity.TicketCatalog;
import fpt.project.NeoNHS.entity.WorkshopSession;
import fpt.project.NeoNHS.enums.EventStatus;
import fpt.project.NeoNHS.enums.SessionStatus;
import fpt.project.NeoNHS.enums.TicketCatalogStatus;
import fpt.project.NeoNHS.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AvailabilityValidator {

    public void validateTicketAvailability(TicketCatalog ticketCatalog, int quantity) {
        // 1. Check Ticket Status
        if (ticketCatalog.getStatus() != TicketCatalogStatus.ACTIVE) {
            throw new BadRequestException("Ticket is not active for sale: " + ticketCatalog.getName());
        }

        // 2. Check Date Validity
        LocalDateTime now = LocalDateTime.now();
        if (ticketCatalog.getValidFromDate() != null && now.isBefore(ticketCatalog.getValidFromDate())) {
            throw new BadRequestException("Ticket sale has not started yet: " + ticketCatalog.getName());
        }
        if (ticketCatalog.getValidToDate() != null && now.isAfter(ticketCatalog.getValidToDate())) {
            throw new BadRequestException("Ticket sale has ended: " + ticketCatalog.getName());
        }

        // 3. Check Ticket Quota
        int currentSold = ticketCatalog.getSoldQuantity() != null ? ticketCatalog.getSoldQuantity() : 0;
        if (ticketCatalog.getTotalQuota() != null) {
            if (currentSold + quantity > ticketCatalog.getTotalQuota()) {
                throw new BadRequestException("Exceeds available tickets! Remaining: "
                        + (ticketCatalog.getTotalQuota() - currentSold));
            }
        }

        // 4. Check Event Status and Capacity
        Event event = ticketCatalog.getEvent();
        if (event != null) {
            if (event.getStatus() == EventStatus.CANCELLED || event.getStatus() == EventStatus.COMPLETED) {
                throw new BadRequestException("Event is not available for booking: " + event.getName());
            }

            if (event.getEndTime() != null && LocalDateTime.now().isAfter(event.getEndTime())) {
                throw new BadRequestException("Event has already ended: " + event.getName());
            }

            if (event.getMaxParticipants() != null) {
                int currentEnrolled = event.getCurrentEnrolled() != null ? event.getCurrentEnrolled() : 0;
                if (currentEnrolled + quantity > event.getMaxParticipants()) {
                    throw new BadRequestException("Event is full! Remaining slots: "
                            + (event.getMaxParticipants() - currentEnrolled));
                }
            }
        }
    }

    public void validateWorkshopAvailability(WorkshopSession workshopSession, int quantity) {
        // 1. Check Session Status
        if (workshopSession.getStatus() != SessionStatus.SCHEDULED) {
            throw new BadRequestException(
                    "Workshop is not available for booking: " + workshopSession.getWorkshopTemplate().getName());
        }

        // 2. Check if workshop session has already started (only allow booking before startTime)
        if (workshopSession.getStartTime() != null && !LocalDateTime.now().isBefore(workshopSession.getStartTime())) {
            throw new BadRequestException(
                    "Workshop session has already started: " + workshopSession.getWorkshopTemplate().getName());
        }

        // 3. Check Capacity
        if (workshopSession.getMaxParticipants() != null) {
            int currentEnrolled = workshopSession.getCurrentEnrolled() != null ? workshopSession.getCurrentEnrolled()
                    : 0;
            if (currentEnrolled + quantity > workshopSession.getMaxParticipants()) {
                throw new BadRequestException("Workshop is full! Remaining slots: "
                        + (workshopSession.getMaxParticipants() - currentEnrolled));
            }
        }
    }
}
