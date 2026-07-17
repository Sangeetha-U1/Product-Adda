package com.productadda.worker.notifications;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.productadda.entity.NotificationWorkerStatus;

import com.productadda.repository.NotificationRepository;
import com.productadda.repository.NotificationWorkerStatusRepository;

import lombok.RequiredArgsConstructor;

/*
 * ================================================================
 * HealthMonitoringWorker
 * Follows the same pattern as ScheduledReconciliationTask and
 * QueueProcessorWorker: a thin @Component wrapping a try/catch around
 * the actual check, so an exception in one run can never kill the
 * scheduler thread pool. Requires @EnableScheduling, already provided
 * by config/SchedulingConfig.java -- no change needed there.
 *
 * Alerting is log-based only (log.warn/log.error),
 * decision -- no Slack/PagerDuty/email integration exists in this
 * project, and none is added here.
 * ================================================================
 */
@Component
@RequiredArgsConstructor
public class HealthMonitoringWorker {

    private static final Logger log = LoggerFactory.getLogger(HealthMonitoringWorker.class);

    private static final String WORKER_NAME = "notification_queue_processor";
    private static final List<String> IN_FLIGHT_STATUSES = List.of("PENDING", "RETRYING");

    private static final long QUEUE_BACKLOG_THRESHOLD = 10_000L;
    private static final long WORKER_STUCK_THRESHOLD_SECONDS = 60L;

    private final NotificationRepository notificationRepository;
    private final NotificationWorkerStatusRepository notificationWorkerStatusRepository;

    /*
     * Runs every 1 minute, per the execution plan.
     */
    @Scheduled(fixedDelay = 60000)
    public void monitorHealth() {
        try {
            checkQueueBacklog();
            checkWorkerStuck();
        } catch (Exception exception) {
            log.error("Health monitoring run failed: {}", exception.getMessage(), exception);
        }
    }

    /*
     * ================================================================
     * CHECK QUEUE BACKLOG (private helper)
     * ================================================================
     */
    private void checkQueueBacklog() {
        long queueDepth = notificationRepository.countByFkDispatchStatus_StatusNameIn(IN_FLIGHT_STATUSES);

        if (queueDepth > QUEUE_BACKLOG_THRESHOLD) {
            log.error("Notification queue backlog detected: depth={}, threshold={}. "
                    + "Recommendation: scale queue workers.", queueDepth, QUEUE_BACKLOG_THRESHOLD);
        }
    }

    /*
     * ================================================================
     * CHECK WORKER STUCK (private helper)
     * ================================================================
     */
    private void checkWorkerStuck() {
        NotificationWorkerStatus workerStatus = notificationWorkerStatusRepository
                .findByWorkerName(WORKER_NAME)
                .orElse(null);

        if (workerStatus == null) {
            log.warn("Notification queue worker status row not found -- cannot verify liveness.");
            return;
        }

        if (workerStatus.getLastProcessedAtUtc() == null) {
            log.warn("Notification queue worker has never run since startup.");
            return;
        }

        long secondsSinceLastRun = Duration.between(
                workerStatus.getLastProcessedAtUtc(), LocalDateTime.now(ZoneOffset.UTC)).getSeconds();

        if (secondsSinceLastRun > WORKER_STUCK_THRESHOLD_SECONDS) {
            log.error("Notification queue worker appears stuck: {} seconds since last run "
                    + "(threshold {}s). Recommendation: manual intervention / restart.",
                    secondsSinceLastRun, WORKER_STUCK_THRESHOLD_SECONDS);
        }
    }
}
