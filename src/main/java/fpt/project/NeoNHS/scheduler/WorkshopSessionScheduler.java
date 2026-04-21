package fpt.project.NeoNHS.scheduler;

import fpt.project.NeoNHS.entity.WorkshopSession;
import fpt.project.NeoNHS.enums.SessionStatus;
import fpt.project.NeoNHS.repository.WorkshopSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkshopSessionScheduler {

    private final WorkshopSessionRepository workshopSessionRepository;

    /**
     * Note on performance:
     * Running a job every 5 minutes (or 15 minutes) is actually very lightweight and standard practice.
     * The database query only looks for a small subset of records (e.g., status = SCHEDULED/ONGOING and time < NOW).
     * If you add indexes on `status`, `start_time`, and `end_time`, this query takes less than 1 millisecond.
     * This is much safer than scheduling in-memory tasks (which get lost if the server restarts).
     */
    //Change to every 15 minutes to reduce load, since this is not time-sensitive and can run less frequently
    @Scheduled(cron = "0 */15 * * * *") // Every 15 minutes at 0 seconds
    @Transactional
    public void handleExpiredAndUnattendedSessions() {
        LocalDateTime now = LocalDateTime.now();
        log.info("[Scheduler] Running checking for expired or unattended workshop sessions at {}", now);

        // 1. Cancel un-enrolled sessions that have already started
        List<WorkshopSession> emptyExpired = workshopSessionRepository.findByStatusAndCurrentEnrolledAndStartTimeBefore(
                SessionStatus.SCHEDULED, 0, now
        );
        for (WorkshopSession s : emptyExpired) {
            s.setStatus(SessionStatus.CANCELLED);
            log.info("[Scheduler] Auto-cancelled session {} because no one enrolled", s.getId());
        }

        // 2. Automatically transition scheduled but enrolled sessions to ONGOING
        List<WorkshopSession> autoStart = workshopSessionRepository.findByStatusAndCurrentEnrolledGreaterThanAndStartTimeBefore(
                SessionStatus.SCHEDULED, 0, now.plusMinutes(15)
        );
        for (WorkshopSession s : autoStart) {
            s.setStatus(SessionStatus.ONGOING);
            log.info("[Scheduler] Auto-started session {} because vendor forgot to start", s.getId());
        }

        // 3. Automatically transition ONGOING sessions to COMPLETED if end time passed
        List<WorkshopSession> autoComplete = workshopSessionRepository.findByStatusAndEndTimeBefore(
                SessionStatus.ONGOING, now
        );
        for (WorkshopSession s : autoComplete) {
            s.setStatus(SessionStatus.COMPLETED);
            log.info("[Scheduler] Auto-completed session {} because time ended", s.getId());
        }

        if (!emptyExpired.isEmpty()) workshopSessionRepository.saveAll(emptyExpired);
        if (!autoStart.isEmpty()) workshopSessionRepository.saveAll(autoStart);
        if (!autoComplete.isEmpty()) workshopSessionRepository.saveAll(autoComplete);
    }
}
