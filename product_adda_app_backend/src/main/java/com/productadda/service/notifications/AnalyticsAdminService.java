package com.productadda.service.notifications;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.analyzer.notifications.NotificationAnalyzer;

import com.productadda.dto.notifications.AdminAnalyticsDto;
import com.productadda.dto.notifications.DeliveryCountsDto;
import com.productadda.dto.notifications.RetryStatsDto;

import com.productadda.entity.Notification;
import com.productadda.entity.User;
import com.productadda.entity.UserRole;

import com.productadda.exception.ApiException;

import com.productadda.repository.NotificationRepository;
import com.productadda.repository.UserRepository;
import com.productadda.repository.UserRoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnalyticsAdminService {

    private static final int DEFAULT_RANGE_DAYS = 30;

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final NotificationRepository notificationRepository;

    /*
     * ================================================================
     * GET ADMIN ANALYTICS
     * Description: Admin-only. Platform-wide notification delivery
     * analytics for a date range (defaults to the last 30 days),
     * optionally narrowed by eventType and/or channel. Sourced from the
     * `notifications` table directly (not notification_logs), per
     * every field needed for these
     * aggregations already lives there.
     * ================================================================
     */
    @Transactional(readOnly = true)
    public AdminAnalyticsDto getAdminAnalytics(String dateFrom, String dateTo, String eventType, String channel) {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        LocalDateTime toUtc = parseOptionalDateEnd(dateTo, "dateTo", now);
        LocalDateTime fromUtc = parseOptionalDateStart(dateFrom, "dateFrom", toUtc.minusDays(DEFAULT_RANGE_DAYS));

        if (fromUtc.isAfter(toUtc)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "dateFrom must not be after dateTo");
        }

        String safeEventType = (eventType == null || eventType.trim().isEmpty())
                ? null
                : eventType.trim().toUpperCase();
        String safeChannel = (channel == null || channel.trim().isEmpty())
                ? null
                : channel.trim().toUpperCase();

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
        List<Notification> notifications = notificationRepository.findForAnalytics(
                fromUtc, toUtc, safeEventType, safeChannel);

        DeliveryCountsDto summary = NotificationAnalyzer.calculateCounts(notifications);
        double deliveryRate = NotificationAnalyzer.calculateRate(summary.getDelivered(), summary.getSent());
        double failureRate = NotificationAnalyzer.calculateRate(summary.getFailed(), summary.getSent());
        RetryStatsDto retryStats = NotificationAnalyzer.calculateRetryStats(notifications);

        /*
         * ================================================================
         * 3. DB SAVING SECTION (Skip if Read-Only GET)
         * Reason: Read-only aggregation, no mutations performed.
         * ================================================================
         */

        /*
         * ================================================================
         * 4. POST-SAVING DATA SANITIZATION & MASKING
         * Reason: No sensitive fields to strip -- this is aggregate,
         * platform-wide data, not per-user detail.
         * ================================================================
         */

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * ================================================================
         */
        return AdminAnalyticsDto.builder()
                .dateFrom(fromUtc)
                .dateTo(toUtc)
                .summary(summary)
                .deliveryRate(deliveryRate)
                .failureRate(failureRate)
                .byChannel(NotificationAnalyzer.groupByChannel(notifications))
                .byEvent(NotificationAnalyzer.groupByEvent(notifications))
                .retryStats(retryStats)
                .build();
    }

    /*
     * ================================================================
     * PARSE OPTIONAL DATE START/END (private helpers)
     * ================================================================
     */
    private LocalDateTime parseOptionalDateStart(String dateStr, String fieldName, LocalDateTime defaultValue) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return LocalDate.parse(dateStr.trim()).atStartOfDay();
        } catch (DateTimeParseException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, fieldName + " must be a valid ISO date (yyyy-MM-dd)");
        }
    }

    private LocalDateTime parseOptionalDateEnd(String dateStr, String fieldName, LocalDateTime defaultValue) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return LocalDate.parse(dateStr.trim()).atTime(23, 59, 59);
        } catch (DateTimeParseException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, fieldName + " must be a valid ISO date (yyyy-MM-dd)");
        }
    }
}
