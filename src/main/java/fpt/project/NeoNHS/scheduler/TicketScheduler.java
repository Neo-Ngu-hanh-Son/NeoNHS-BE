package fpt.project.NeoNHS.scheduler;

import fpt.project.NeoNHS.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TicketScheduler {

    private final TicketService ticketService;

    // Chạy mỗi 30 phút (30 * 60 * 1000 = 1800000 ms)
    @Scheduled(fixedRate = 1800000)
    public void checkAndExpireTickets() {
        log.info("Running TicketScheduler to check and update expired tickets...");
        try {
            ticketService.expireOutdatedTickets();
            log.info("Completed checking and updating expired tickets.");
        } catch (Exception e) {
            log.error("Error occurred while checking for expired tickets: ", e);
        }
    }
}
