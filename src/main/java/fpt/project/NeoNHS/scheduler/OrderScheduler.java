package fpt.project.NeoNHS.scheduler;

import fpt.project.NeoNHS.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderScheduler {

    private final OrderService orderService;

    // Run every 1 minute
    @Scheduled(fixedRate = 300000)
    public void cancelExpiredOrdersJob() {
        log.info("Running OrderScheduler to cancel expired pending orders...");
        try {
            orderService.cancelExpiredOrders();
            log.info("Completed cancelling expired pending orders.");
        } catch (Exception e) {
            log.error("Error occurred while cancelling expired orders: ", e);
        }
    }
}
