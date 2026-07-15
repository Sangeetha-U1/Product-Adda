package com.productadda.service.notifications;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.entity.DispatchStatus;
import com.productadda.entity.Notification;
import com.productadda.entity.NotificationLog;
import com.productadda.entity.NotificationWorkerStatus;

import com.productadda.exception.ApiException;

import com.productadda.repository.DispatchStatusRepository;
import com.productadda.repository.NotificationLogRepository;
import com.productadda.repository.NotificationRepository;
import com.productadda.repository.NotificationWorkerStatusRepository;

import com.productadda.util.UuidUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationQueueProcessorService {

    private static final Logger log = LoggerFactory.getLogger(NotificationQueueProcessorService.class);

    private static final int BATCH_SIZE = 100;
    private static final String WORKER_NAME = "notification_queue_processor";

    // Exponential backoff, per week_8_execution_plan_v2.md (retry 1 = 5
    // min, retry 2 = 15 min, retry 3 = 60 min, then MAX_RETRIES_FAILED).
    private static final long RETRY_1_DELAY_MINUTES = 5L;
    private static final long RETRY_2_DELAY_MINUTES = 15L;
    private static final long RETRY_3_DELAY_MINUTES = 60L;
    private static final int MAX_RETRY_COUNT = 3;

    private final NotificationRepository notificationRepository;
    private final DispatchStatusRepository dispatchStatusRepository;
    private final NotificationLogRepository notificationLogRepository;
    private final NotificationWorkerStatusRepository notificationWorkerStatusRepository;

    private final NotificationDispatchServiceEmail notificationDispatchServiceEmail;
    private final NotificationDispatchServiceInApp notificationDispatchServiceInApp;
    private final NotificationDispatchServiceSms notificationDispatchServiceSms;

    private final UuidUtil uuidUtil;

    /*
     * ================================================================
     * PROCESS PENDING BATCH
     * Description: Fetches up to BATCH_SIZE notifications in PENDING or
     * RETRYING status whose next_retry_at_utc has elapsed (or is null),
     * dispatches each via its channel, and records the outcome. One
     * notification's failure is isolated and never halts the batch.
     * Invoked on a fixed schedule by QueueProcessorWorker.
     * ================================================================
     */
    @Transactional
    public void processPendingBatch() {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * Reason: No caller-supplied input -- this is a scheduled,
         * parameterless batch job. Lookup rows are resolved instead.
         * ================================================================
         */
        DispatchStatus queuedStatus = dispatchStatusRepository.findByStatusName("QUEUED")
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "QUEUED dispatch status lookup row not found"));
        DispatchStatus sentStatus = dispatchStatusRepository.findByStatusName("SENT")
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "SENT dispatch status lookup row not found"));
        DispatchStatus failedStatus = dispatchStatusRepository.findByStatusName("FAILED")
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "FAILED dispatch status lookup row not found"));
        DispatchStatus retryingStatus = dispatchStatusRepository.findByStatusName("RETRYING")
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "RETRYING dispatch status lookup row not found"));
        DispatchStatus maxRetriesFailedStatus = dispatchStatusRepository.findByStatusName("MAX_RETRIES_FAILED")
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "MAX_RETRIES_FAILED dispatch status lookup row not found"));

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        List<Notification> batch = notificationRepository.findBatchForDispatch(
                List.of("PENDING", "RETRYING"), now, PageRequest.of(0, BATCH_SIZE));

        int sentCount = 0;
        int failedCount = 0;

        for (Notification notification : batch) {

            notification.setFkDispatchStatus(queuedStatus);
            notificationRepository.save(notification);

            String channelName = notification.getFkChannel() != null
                    ? notification.getFkChannel().getChannelName()
                    : null;

            int attemptNumber = notification.getRetryCount() + 1;

            try {
                dispatchByChannel(channelName, notification);

                notification.setFkDispatchStatus(sentStatus);
                notification.setSentAtUtc(now);
                notificationRepository.save(notification);

                writeLog(notification, attemptNumber, sentStatus, null, now);

                sentCount++;

            } catch (Exception dispatchException) {

                int newRetryCount = notification.getRetryCount() + 1;
                notification.setRetryCount(newRetryCount);

                if (newRetryCount >= MAX_RETRY_COUNT) {
                    notification.setFkDispatchStatus(maxRetriesFailedStatus);
                    notification.setNextRetryAtUtc(null);
                } else {
                    long delayMinutes = resolveBackoffMinutes(newRetryCount);
                    notification.setFkDispatchStatus(retryingStatus);
                    notification.setNextRetryAtUtc(now.plusMinutes(delayMinutes));
                }

                notificationRepository.save(notification);

                writeLog(notification, attemptNumber, failedStatus, dispatchException.getMessage(), now);

                failedCount++;

                log.warn("Notification dispatch failed: notificationId={}, channel={}, attempt={}, reason={}",
                        notification.getPkNotificationId(), channelName, attemptNumber,
                        dispatchException.getMessage());
            }
        }

        updateWorkerHeartbeat(now, batch.size());

        log.info("Notification queue batch processed: fetched={}, sent={}, failed={}",
                batch.size(), sentCount, failedCount);

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * Reason: Per-notification saves and log writes happen inline
         * above, inside the batch loop, since each notification's
         * outcome must be isolated from the others rather than
         * committed as one all-or-nothing unit.
         * ================================================================
         */

        /*
         * ================================================================
         * 4. POST-SAVING DATA SANITIZATION & MASKING
         * Reason: N/A - void method, no response payload.
         * ================================================================
         */

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * Reason: N/A - internal scheduled job, not bound to a REST
         * endpoint.
         * ================================================================
         */
    }

    /*
     * ================================================================
     * DISPATCH BY CHANNEL (private helper)
     * Description: Routes to the correct per-channel dispatch service.
     * PUSH has no dispatch service yet (unlike SMS, which has a
     * structural stub per Dheeraj's explicit Day 1 request); a PUSH
     * row reaching this switch throws until FCM wiring is added in a
     * future week.
     * ================================================================
     */
    private void dispatchByChannel(String channelName, Notification notification) {
        if ("EMAIL".equals(channelName)) {
            notificationDispatchServiceEmail.dispatchEmail(notification);
        } else if ("IN_APP".equals(channelName)) {
            notificationDispatchServiceInApp.dispatchInApp();
        } else if ("SMS".equals(channelName)) {
            notificationDispatchServiceSms.dispatchSms(notification);
        } else {
            // TODO: Wire up a PUSH dispatch service once FCM
            // credentials/setup are available.
            throw new ApiException(HttpStatus.NOT_IMPLEMENTED,
                    "No dispatch handler configured for channel: " + channelName);
        }
    }

    /*
     * ================================================================
     * RESOLVE BACKOFF MINUTES (private helper)
     * ================================================================
     */
    private long resolveBackoffMinutes(int retryCount) {
        if (retryCount == 1) {
            return RETRY_1_DELAY_MINUTES;
        }
        if (retryCount == 2) {
            return RETRY_2_DELAY_MINUTES;
        }
        return RETRY_3_DELAY_MINUTES;
    }

    /*
     * ================================================================
     * WRITE LOG (private helper)
     * Description: Appends one immutable notification_logs row per
     * dispatch attempt, success or failure.
     * ================================================================
     */
    private void writeLog(Notification notification, int attemptNumber, DispatchStatus outcomeStatus,
            String errorMessage, LocalDateTime dispatchedAtUtc) {

        NotificationLog notificationLog = NotificationLog.builder()
                .pkLogId(uuidUtil.generateUuidV7())
                .fkNotification(notification)
                .attemptNumber(attemptNumber)
                .fkDispatchStatus(outcomeStatus)
                .errorMessage(errorMessage)
                .dispatchedAtUtc(dispatchedAtUtc)
                .build();

        notificationLogRepository.save(notificationLog);
    }

    /*
     * ================================================================
     * UPDATE WORKER HEARTBEAT (private helper)
     * ================================================================
     */
    private void updateWorkerHeartbeat(LocalDateTime now, int processedInThisRun) {
        NotificationWorkerStatus workerStatus = notificationWorkerStatusRepository.findByWorkerName(WORKER_NAME)
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        WORKER_NAME + " worker status row not found"));

        workerStatus.setLastProcessedAtUtc(now);
        workerStatus.setProcessedCount(workerStatus.getProcessedCount() + processedInThisRun);

        notificationWorkerStatusRepository.save(workerStatus);
    }
}
