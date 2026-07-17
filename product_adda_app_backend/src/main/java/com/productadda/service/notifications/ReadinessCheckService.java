package com.productadda.service.notifications;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.notifications.ReadinessResponseDto;

import com.productadda.entity.NotificationWorkerStatus;

import com.productadda.repository.NotificationWorkerStatusRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReadinessCheckService {

    private static final String WORKER_NAME = "notification_queue_processor";

    // Matches the same reconciled threshold used by
    // NotificationHealthCheckLiveService -- see that class for why 60s,
    // not the plan's original (incompatible-with-our-own-architecture) 5s.
    private static final long WORKER_STUCK_THRESHOLD_SECONDS = 60L;

    private final com.productadda.service.HealthService healthService;
    private final NotificationWorkerStatusRepository notificationWorkerStatusRepository;

    // Injected as the concrete JavaMailSenderImpl, not the JavaMailSender
    // interface -- testConnection() is only exposed on the impl class.
    // Spring Boot's auto-configured mail sender bean is a
    // JavaMailSenderImpl by default, so this resolves to the same
    // singleton EmailService already uses.
    private final JavaMailSenderImpl javaMailSender;

    @Value("${app.mail.smtp-enabled}")
    private boolean smtpEnabled;

    /*
     * ================================================================
     * CHECK READINESS
     * Description: Public system readiness probe for load balancers /
     * orchestrators. Checks database connectivity (reusing the existing
     * global HealthService.getDatabaseHealth(), the same check backing
     * GET /api/health/db, rather than a second parallel implementation),
     * EMAIL SMTP connectivity (a real JavaMailSenderImpl.testConnection()
     * call -- opens and closes a connection without sending anything),
     * and the notification queue worker's heartbeat. SMS/PUSH always
     * report "inactive" (no provider configured, an expected passing
     * state, not a failure -- per the plan's own Test Scenario 9).
     * IN_APP always reports "healthy" -- it has no external dependency
     * beyond the database, which is checked separately.
     * ================================================================
     */
    @Transactional(readOnly = true)
    public ReadinessResponseDto checkReadiness() {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * Reason: No input parameters to validate; public probe.
         * ================================================================
         */

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        Map<String, String> componentStatus = new HashMap<>();
        List<String> failingComponents = new ArrayList<>();

        // Database
        try {
            healthService.getDatabaseHealth();
            componentStatus.put("database", "healthy");
        } catch (Exception ex) {
            componentStatus.put("database", "unhealthy");
            failingComponents.add("database");
        }

        // Email (real SMTP connectivity test)
        if (!smtpEnabled) {
            componentStatus.put("email", "inactive");
        } else {
            try {
                javaMailSender.testConnection();
                componentStatus.put("email", "healthy");
            } catch (Exception ex) {
                componentStatus.put("email", "unhealthy");
                failingComponents.add("email");
            }
        }

        // SMS / Push -- no provider configured yet, always inactive.
        componentStatus.put("sms", "inactive");
        componentStatus.put("push", "inactive");

        // In-app -- no external dependency beyond the DB, already checked.
        componentStatus.put("in_app", "healthy");

        // Queue worker heartbeat
        NotificationWorkerStatus workerStatus = notificationWorkerStatusRepository
                .findByWorkerName(WORKER_NAME)
                .orElse(null);

        boolean workerAlive = false;
        if (workerStatus != null && workerStatus.getLastProcessedAtUtc() != null) {
            long secondsSinceLastRun = Duration.between(
                    workerStatus.getLastProcessedAtUtc(), LocalDateTime.now(ZoneOffset.UTC)).getSeconds();
            workerAlive = secondsSinceLastRun <= WORKER_STUCK_THRESHOLD_SECONDS;
        }

        componentStatus.put("queue_worker", workerAlive ? "alive" : "stuck");
        if (!workerAlive) {
            failingComponents.add("queue_worker");
        }

        boolean isReady = failingComponents.isEmpty();

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * Reason: N/A - read-only probe, no mutations performed.
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
        return ReadinessResponseDto.builder()
                .status(isReady ? "ready" : "not_ready")
                .componentStatus(componentStatus)
                .failingComponents(failingComponents)
                .build();
    }
}
