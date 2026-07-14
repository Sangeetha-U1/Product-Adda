package com.productadda.service.payment;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/*
 * ================================================================
 * ScheduledReconciliationTask
 * First @Scheduled job in this codebase - no existing pattern to
 * mirror, so this follows plain Spring Boot convention. Requires
 * @EnableScheduling somewhere in the application context; see
 * config/SchedulingConfig.java (also added this day) rather than
 * modifying ProductaddaApplication.java directly.
 * ================================================================
 */
@Component
@RequiredArgsConstructor
public class ScheduledReconciliationTask {

    private static final Logger log = LoggerFactory.getLogger(ScheduledReconciliationTask.class);

    private final ReconciliationServiceDailyRun reconciliationServiceDailyRun;

    /*
     * Runs daily at 2:00 AM UTC. Cron expression fields are
     * second-minute-hour-day-month-weekday; zone is pinned explicitly
     * to UTC regardless of server timezone configuration.
     */
    @Scheduled(cron = "0 0 2 * * *", zone = "UTC")
    public void runScheduledReconciliation() {
        LocalDateTime toUtc = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime fromUtc = toUtc.minusHours(24);

        try {
            reconciliationServiceDailyRun.runReconciliation(fromUtc, toUtc, "daily_scheduled", null);
        } catch (Exception exception) {
            // A scheduled job must never let an exception propagate and kill
            // the scheduler thread pool - log and let tomorrow's run proceed.
            log.error("Scheduled daily reconciliation run failed: {}", exception.getMessage(), exception);
        }
    }
}
