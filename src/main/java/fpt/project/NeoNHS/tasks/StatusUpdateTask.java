package fpt.project.NeoNHS.tasks;

import fpt.project.NeoNHS.entity.Event;
import fpt.project.NeoNHS.entity.Voucher;
import fpt.project.NeoNHS.enums.EventStatus;
import fpt.project.NeoNHS.enums.VoucherStatus;
import fpt.project.NeoNHS.repository.EventRepository;
import fpt.project.NeoNHS.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class StatusUpdateTask {

    private final VoucherRepository voucherRepository;
    private final EventRepository eventRepository;

    @Scheduled(cron = "0 0 0 * * *") // Every day at midnight
    @Transactional
    public void updateStatuses() {
        log.info("Starting StatusUpdateTask at {}", LocalDateTime.now());
        updateVoucherStatuses();
        updateEventStatuses();
        log.info("Finished StatusUpdateTask");
    }

    private void updateVoucherStatuses() {
        List<Voucher> activeVouchers = voucherRepository.findAllByStatusAndDeletedAtIsNull(VoucherStatus.ACTIVE);
        LocalDateTime now = LocalDateTime.now();
        int count = 0;

        for (Voucher voucher : activeVouchers) {
            if (voucher.getEndDate() != null && voucher.getEndDate().isBefore(now)) {
                voucher.setStatus(VoucherStatus.EXPIRED);
                count++;
            }
        }

        if (count > 0) {
            voucherRepository.saveAll(activeVouchers);
            log.info("Updated {} vouchers to EXPIRED", count);
        }
    }

    private void updateEventStatuses() {
        List<EventStatus> targets = List.of(EventStatus.UPCOMING, EventStatus.ONGOING);
        List<Event> events = eventRepository.findAllByStatusInAndDeletedAtIsNull(targets);
        LocalDateTime now = LocalDateTime.now();
        int count = 0;

        for (Event event : events) {
            EventStatus oldStatus = event.getStatus();
            
            if (event.getEndTime().isBefore(now)) {
                event.setStatus(EventStatus.COMPLETED);
            } else if (event.getStartTime().isBefore(now) || event.getStartTime().isEqual(now)) {
                event.setStatus(EventStatus.ONGOING);
            }

            if (event.getStatus() != oldStatus) {
                count++;
            }
        }

        if (count > 0) {
            eventRepository.saveAll(events);
            log.info("Updated {} events status", count);
        }
    }
}
