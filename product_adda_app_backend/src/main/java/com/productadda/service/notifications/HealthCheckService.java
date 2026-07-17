package com.productadda.service.notifications;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.analyzer.notifications.NotificationAnalyzer;

import com.productadda.dto.notifications.ChannelHealthDto;
import com.productadda.dto.notifications.DeliveryCountsDto;
import com.productadda.dto.notifications.NotificationHealthReportResponseDto;

import com.productadda.entity.Notification;
import com.productadda.entity.NotificationLog;
import com.productadda.entity.User;
import com.productadda.entity.UserRole;

import com.productadda.exception.ApiException;

import com.productadda.repository.NotificationLogRepository;
import com.productadda.repository.NotificationRepository;
import com.productadda.repository.UserRepository;
import com.productadda.repository.UserRoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HealthCheckService {

    private static final List<String> ALL_CHANNEL_NAMES = List.of("EMAIL", "SMS", "PUSH", "IN_APP");
    private static final long WINDOW_HOURS = 24L;

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationLogRepository notificationLogRepository;

    /*
     * ================================================================
     * GET CHANNEL HEALTH
     * Description: Admin-only. Reports dispatch health per actual
     * notification channel (EMAIL, SMS, PUSH, IN_APP) over the last 24
     * hours -- not per named provider (SendGrid/Twilio/FCM), since this
     * project dispatches EMAIL via plain SMTP, not a REST provider API.
     * A channel with zero attempts in the window (SMS, PUSH today,
     * since no rows are ever created for them yet) reports
     * status=inactive rather than a misleading 0%/0% rate.
     * ================================================================
     */
    @Transactional(readOnly = true)
    public NotificationHealthReportResponseDto getChannelHealth() {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        // ==========================================
        // 1.2 CONTEXT AUTHENTICATION
        // ==========================================
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentication missing or invalid");
        }

        String currentUsername = authentication.getName();

        // ==========================================
        // 1.3 DATABASE LOOKUP VALIDATION
        // ==========================================
        User currentUser = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Authenticated user no longer exists"));

        List<UserRole> userRoles = userRoleRepository.findByFkUser(currentUser);

        boolean hasAdminRole = userRoles.stream()
                .map(userRole -> userRole.getFkRole().getRoleName())
                .anyMatch(roleName -> "ADMIN".equals(roleName) || "SUPER_ADMIN".equals(roleName));

        if (!hasAdminRole) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Admin role required to access this endpoint");
        }

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime windowStart = now.minusHours(WINDOW_HOURS);

        Map<String, ChannelHealthDto> channels = new HashMap<>();

        for (String channelName : ALL_CHANNEL_NAMES) {
            List<Notification> channelNotifications = notificationRepository.findForAnalytics(
                    windowStart, now, null, channelName);

            DeliveryCountsDto counts = NotificationAnalyzer.calculateCounts(channelNotifications);

            if (counts.getSent() == 0) {
                channels.put(channelName, ChannelHealthDto.builder()
                        .status("inactive")
                        .build());
                continue;
            }

            double successRate = NotificationAnalyzer.calculateRate(counts.getDelivered(), counts.getSent());
            double failureRate = NotificationAnalyzer.calculateRate(counts.getFailed(), counts.getSent());

            List<NotificationLog> failedLogs = notificationLogRepository
                    .findFailedLogsByChannelSince(channelName, windowStart);

            Map<String, Long> errorBreakdown = new HashMap<>();
            for (NotificationLog log : failedLogs) {
                String category = NotificationAnalyzer.classifyError(log.getErrorMessage());
                errorBreakdown.merge(category, 1L, Long::sum);
            }

            channels.put(channelName, ChannelHealthDto.builder()
                    .status("active")
                    .successRate(successRate)
                    .failureRate(failureRate)
                    .errorBreakdown(errorBreakdown)
                    .build());
        }

        /*
         * ================================================================
         * 3. DB SAVING SECTION (Skip if Read-Only GET)
         * Reason: Read-only aggregation, no mutations performed.
         * ================================================================
         */

        /*
         * ================================================================
         * 4. POST-SAVING DATA SANITIZATION & MASKING
         * Reason: No sensitive fields to strip -- aggregate counts only,
         * no raw recipient data exposed.
         * ================================================================
         */

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * ================================================================
         */
        return NotificationHealthReportResponseDto.builder()
                .channels(channels)
                .build();
    }
}
