package com.productadda.controller.notifications;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.notifications.AdminAnalyticsDto;
import com.productadda.dto.notifications.NotificationHealthReportResponseDto;
import com.productadda.dto.notifications.VendorAnalyticsDto;

import com.productadda.service.notifications.AnalyticsAdminService;
import com.productadda.service.notifications.AnalyticsVendorService;
import com.productadda.service.notifications.HealthCheckService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsAdminService analyticsAdminService;
    private final AnalyticsVendorService analyticsVendorService;
    private final HealthCheckService healthCheckService;

    /*
     * ================================================================
     * GET ADMIN ANALYTICS
     * GET /api/notifications/analytics/admin
     * Auth Required - Admin role. Optional dateFrom/dateTo (default:
     * last 30 days), eventType, channel filters.
     * ================================================================
     */
    @GetMapping("/analytics/admin")
    public ResponseEntity<ApiSuccessResponseDto<AdminAnalyticsDto>> getAdminAnalytics(
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String channel) {

        AdminAnalyticsDto response = analyticsAdminService.getAdminAnalytics(dateFrom, dateTo, eventType, channel);

        return ResponseEntity.status(HttpStatus.OK).body(
                ApiSuccessResponseDto.<AdminAnalyticsDto>builder()
                        .success(true)
                        .message("Admin notification analytics retrieved successfully")
                        .data(response)
                        .build());
    }

    /*
     * ================================================================
     * GET VENDOR ANALYTICS
     * GET /api/notifications/analytics/vendor
     * Auth Required - Vendor role. Own notifications only, resolved
     * server-side from the authenticated user -- no vendorId parameter.
     * Optional dateFrom/dateTo (default: last 30 days).
     * ================================================================
     */
    @GetMapping("/analytics/vendor")
    public ResponseEntity<ApiSuccessResponseDto<VendorAnalyticsDto>> getVendorAnalytics(
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo) {

        VendorAnalyticsDto response = analyticsVendorService.getVendorAnalytics(dateFrom, dateTo);

        return ResponseEntity.status(HttpStatus.OK).body(
                ApiSuccessResponseDto.<VendorAnalyticsDto>builder()
                        .success(true)
                        .message("Vendor notification analytics retrieved successfully")
                        .data(response)
                        .build());
    }

    /*
     * ================================================================
     * GET CHANNEL HEALTH
     * GET /api/notifications/health
     * Auth Required - Admin role. Last 24 hours, per channel
     * (EMAIL/SMS/PUSH/IN_APP), not per named provider.
     * ================================================================
     */
    @GetMapping("/health")
    public ResponseEntity<ApiSuccessResponseDto<NotificationHealthReportResponseDto>> getChannelHealth() {

        NotificationHealthReportResponseDto response = healthCheckService.getChannelHealth();

        return ResponseEntity.status(HttpStatus.OK).body(
                ApiSuccessResponseDto.<NotificationHealthReportResponseDto>builder()
                        .success(true)
                        .message("Notification channel health retrieved successfully")
                        .data(response)
                        .build());
    }
}
