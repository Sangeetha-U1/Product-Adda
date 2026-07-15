package com.productadda.worker.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.productadda.service.notifications.NotificationQueueProcessorService;

import lombok.RequiredArgsConstructor;

/*
 * ================================================================
 * QueueProcessorWorker
 * Follows the same pattern as ScheduledReconciliationTask: a thin
 * @Component wrapping a try/catch around the actual service call, so
 * an exception in one run can never kill the scheduler thread pool.
 * Requires @EnableScheduling, already provided by
 * config/SchedulingConfig.java -- no change needed there.
 * ================================================================
 */
@Component
@RequiredArgsConstructor
public class QueueProcessorWorker {

    private static final Logger log = LoggerFactory.getLogger(QueueProcessorWorker.class);

    private final NotificationQueueProcessorService notificationQueueProcessorService;

    /*
     * Runs every 15 seconds, per Dheeraj's Day 1 decision (plan
     * originally specified 5 seconds; 15s was chosen as a better fit
     * for a monolith making real synchronous SMTP calls in-loop).
     * fixedDelay (not fixedRate) is used deliberately -- the next run
     * only starts 15 seconds after the previous run FINISHES, so a
     * slow batch can never overlap with itself.
     */
    @Scheduled(fixedDelay = 15000)
    public void runQueueProcessing() {
        try {
            notificationQueueProcessorService.processPendingBatch();
        } catch (Exception exception) {
            log.error("Notification queue processing run failed: {}", exception.getMessage(), exception);
        }
    }
}
