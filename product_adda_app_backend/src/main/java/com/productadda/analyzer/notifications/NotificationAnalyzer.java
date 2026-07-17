package com.productadda.analyzer.notifications;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.productadda.dto.notifications.DeliveryCountsDto;
import com.productadda.dto.notifications.RetryStatsDto;

import com.productadda.entity.Notification;

/*
 * ================================================================
 * NotificationAnalyzer
 * Plain stateless computation utility, not a Spring @Service -- matches
 * the execution plan's own explicit file structure, which separates an
 * "analyzer" package (pure computation) from "service" (orchestration,
 * auth, DB access). Every method here operates only on already-fetched
 * in-memory data; none of them touch the database.
 * ================================================================
 */
public final class NotificationAnalyzer {

    private static final String STATUS_SENT = "SENT";
    private static final String STATUS_FAILED = "FAILED";
    private static final String STATUS_MAX_RETRIES_FAILED = "MAX_RETRIES_FAILED";

    private NotificationAnalyzer() {
    }

    /*
     * Aggregate counts across a list of notifications. See
     * DeliveryCountsDto for the sent/delivered/failed semantic mapping.
     */
    public static DeliveryCountsDto calculateCounts(List<Notification> notifications) {
        long delivered = 0;
        long failed = 0;

        for (Notification notification : notifications) {
            String statusName = statusNameOf(notification);
            if (STATUS_SENT.equals(statusName)) {
                delivered++;
            } else if (STATUS_FAILED.equals(statusName) || STATUS_MAX_RETRIES_FAILED.equals(statusName)) {
                failed++;
            }
        }

        return DeliveryCountsDto.builder()
                .sent(delivered + failed)
                .delivered(delivered)
                .failed(failed)
                .build();
    }

    /*
     * Groups notifications by channel name, computing DeliveryCountsDto
     * per group.
     */
    public static Map<String, DeliveryCountsDto> groupByChannel(List<Notification> notifications) {
        Map<String, List<Notification>> grouped = new HashMap<>();
        for (Notification notification : notifications) {
            String channelName = notification.getFkChannel() != null
                    ? notification.getFkChannel().getChannelName()
                    : "UNKNOWN";
            grouped.computeIfAbsent(channelName, key -> new java.util.ArrayList<>()).add(notification);
        }

        Map<String, DeliveryCountsDto> result = new HashMap<>();
        grouped.forEach((channelName, group) -> result.put(channelName, calculateCounts(group)));
        return result;
    }

    /*
     * Groups notifications by notification type name, computing
     * DeliveryCountsDto per group.
     */
    public static Map<String, DeliveryCountsDto> groupByEvent(List<Notification> notifications) {
        Map<String, List<Notification>> grouped = new HashMap<>();
        for (Notification notification : notifications) {
            String typeName = notification.getFkType() != null
                    ? notification.getFkType().getNotificationTypeName()
                    : "UNKNOWN";
            grouped.computeIfAbsent(typeName, key -> new java.util.ArrayList<>()).add(notification);
        }

        Map<String, DeliveryCountsDto> result = new HashMap<>();
        grouped.forEach((typeName, group) -> result.put(typeName, calculateCounts(group)));
        return result;
    }

    /*
     * Retry statistics across a list of notifications.
     */
    public static RetryStatsDto calculateRetryStats(List<Notification> notifications) {
        long totalRetried = 0;
        long successfulAfterRetry = 0;
        long maxRetriesFailed = 0;

        for (Notification notification : notifications) {
            int retryCount = notification.getRetryCount() != null ? notification.getRetryCount() : 0;
            String statusName = statusNameOf(notification);

            if (retryCount > 0) {
                totalRetried++;
                if (STATUS_SENT.equals(statusName)) {
                    successfulAfterRetry++;
                }
            }
            if (STATUS_MAX_RETRIES_FAILED.equals(statusName)) {
                maxRetriesFailed++;
            }
        }

        return RetryStatsDto.builder()
                .totalRetried(totalRetried)
                .successfulAfterRetry(successfulAfterRetry)
                .maxRetriesFailed(maxRetriesFailed)
                .build();
    }

    /*
     * Best-effort error classification from raw error message text.
     * There is no structured error-code field anywhere in this schema
     * (no HTTP status codes, since this project uses SMTP directly, not
     * a REST-based provider like SendGrid) -- this is deliberately
     * simple substring matching, not a guarantee of accuracy.
     */
    public static String classifyError(String errorMessage) {
        if (errorMessage == null || errorMessage.isBlank()) {
            return "unknown";
        }
        String lower = errorMessage.toLowerCase();
        if (lower.contains("auth")) {
            return "authentication_error";
        }
        if (lower.contains("timeout") || lower.contains("timed out")) {
            return "timeout";
        }
        if (lower.contains("connect")) {
            return "connection_error";
        }
        if (lower.contains("not implemented") || lower.contains("not configured")) {
            return "provider_not_configured";
        }
        return "other";
    }

    /*
     * Rate calculation as a percentage, rounded to 1 decimal place.
     * Returns 0.0 rather than NaN/Infinity when the denominator is 0.
     */
    public static double calculateRate(long numerator, long denominator) {
        if (denominator == 0) {
            return 0.0;
        }
        return Math.round((numerator * 1000.0) / denominator) / 10.0;
    }

    private static String statusNameOf(Notification notification) {
        return notification.getFkDispatchStatus() != null
                ? notification.getFkDispatchStatus().getStatusName()
                : null;
    }
}
