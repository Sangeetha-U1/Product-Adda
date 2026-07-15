package com.productadda.service.notifications;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.notifications.NotificationHealthLiveResponseDto;

import com.productadda.entity.NotificationWorkerStatus;

import com.productadda.exception.ApiException;

import com.productadda.repository.NotificationWorkerStatusRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationHealthCheckLiveService {

    private static final String WORKER_NAME = "notification_queue_processor";

    // If the worker hasn't reported a heartbeat in over 2 minutes
    // (roughly 8x its 15-second run interval), something is wrong.
    private static final long UNHEALTHY_THRESHOLD_SECONDS = 120L;

    private final NotificationWorkerStatusRepository notificationWorkerStatusRepository;

    /*
     * ================================================================
     * CHECK LIVENESS
     * Description: Reports whether QueueProcessorWorker has run
     * recently, by reading its heartbeat row. isHealthy is false both
     * when the worker has never run (lastProcessedAtUtc is null) and
     * when its last run is older than UNHEALTHY_THRESHOLD_SECONDS.
     * ================================================================
     */
    @Transactional(readOnly = true)
    public NotificationHealthLiveResponseDto checkLiveness() {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // Reason: No input parameters to validate.
        // ==========================================

        // ==========================================
        // 1.2 CONTEXT AUTHENTICATION
        // Reason: N/A - public liveness probe, matching the existing
        // /api/health/app and /api/health/db convention.
        // ==========================================

        // ==========================================
        // 1.3 DATABASE LOOKUP VALIDATION
        // ==========================================
        NotificationWorkerStatus workerStatus = notificationWorkerStatusRepository.findByWorkerName(WORKER_NAME)
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        WORKER_NAME + " worker status row not found"));

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        LocalDateTime lastProcessedAtUtc = workerStatus.getLastProcessedAtUtc();

        Long secondsSinceLastRun = lastProcessedAtUtc != null
                ? Duration.between(lastProcessedAtUtc, now).getSeconds()
                : null;

        boolean isHealthy = secondsSinceLastRun != null && secondsSinceLastRun <= UNHEALTHY_THRESHOLD_SECONDS;

        /*
         * ================================================================
         * 3. DB SAVING SECTION (Skip if Read-Only GET)
         * Reason: Read-only lookup, no mutations performed.
         * ================================================================
         */

        /*
         * ================================================================
         * 4. POST-SAVING DATA SANITIZATION & MASKING
         * Reason: N/A - see section 3.
         * ================================================================
         */

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * ================================================================
         */
        return NotificationHealthLiveResponseDto.builder()
                .workerName(workerStatus.getWorkerName())
                .isHealthy(isHealthy)
                .lastProcessedAtUtc(lastProcessedAtUtc)
                .processedCount(workerStatus.getProcessedCount())
                .secondsSinceLastRun(secondsSinceLastRun)
                .build();
    }
}
