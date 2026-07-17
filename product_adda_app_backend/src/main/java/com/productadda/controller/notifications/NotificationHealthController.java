package com.productadda.controller.notifications;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.notifications.NotificationHealthLiveResponseDto;
import com.productadda.dto.notifications.ReadinessResponseDto;

import com.productadda.service.notifications.NotificationHealthCheckLiveService;
import com.productadda.service.notifications.ReadinessCheckService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications/health")
@RequiredArgsConstructor
public class NotificationHealthController {

    private final NotificationHealthCheckLiveService notificationHealthCheckLiveService;
    private final ReadinessCheckService readinessCheckService;

    /*
     * ================================================================
     * LIVENESS CHECK
     * GET /api/notifications/health/live
     * No Auth - public liveness probe, mirrors the existing
     * /api/health/app and /api/health/db convention.
     * now returns a real 503 when the worker is
     * unhealthy, instead of always 200 with a body flag -- correct for
     * orchestrators that only check the HTTP status code.
     * ================================================================
     */
    @GetMapping("/live")
    public ResponseEntity<ApiSuccessResponseDto<NotificationHealthLiveResponseDto>> checkLiveness() {

        NotificationHealthLiveResponseDto response = notificationHealthCheckLiveService.checkLiveness();

        boolean isHealthy = Boolean.TRUE.equals(response.getIsHealthy());
        HttpStatus httpStatus = isHealthy ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;

        return ResponseEntity.status(httpStatus).body(
                ApiSuccessResponseDto.<NotificationHealthLiveResponseDto>builder()
                        .success(isHealthy)
                        .message(isHealthy
                                ? "Notification queue processor is alive"
                                : "Notification queue processor appears stuck")
                        .data(response)
                        .build());
    }

    /*
     * ================================================================
     * READINESS CHECK
     * GET /api/notifications/health/readiness
     * No Auth - public readiness probe for load balancers. Requires
     * adding this path to PublicRoutes.java, same as /live already was.
     * ================================================================
     */
    @GetMapping("/readiness")
    public ResponseEntity<ApiSuccessResponseDto<ReadinessResponseDto>> checkReadiness() {

        ReadinessResponseDto response = readinessCheckService.checkReadiness();

        boolean isReady = "ready".equals(response.getStatus());
        HttpStatus httpStatus = isReady ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;

        return ResponseEntity.status(httpStatus).body(
                ApiSuccessResponseDto.<ReadinessResponseDto>builder()
                        .success(isReady)
                        .message(isReady
                                ? "System ready to accept notification events"
                                : "System not ready - one or more dependencies unhealthy")
                        .data(response)
                        .build());
    }
}
