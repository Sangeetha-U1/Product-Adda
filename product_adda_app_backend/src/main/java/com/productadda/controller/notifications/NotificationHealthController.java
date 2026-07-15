package com.productadda.controller.notifications;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.notifications.NotificationHealthLiveResponseDto;

import com.productadda.service.notifications.NotificationHealthCheckLiveService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications/health")
@RequiredArgsConstructor
public class NotificationHealthController {

    private final NotificationHealthCheckLiveService notificationHealthCheckLiveService;

    /*
     * ================================================================
     * LIVENESS CHECK
     * GET /api/notifications/health/live
     * No Auth - public liveness probe, mirrors the existing
     * /api/health/app and /api/health/db convention. Requires adding
     * this path to PublicRoutes.java -- see CHANGES_DAY1.md, since
     * that file was not reviewed this week.
     * ================================================================
     */
    @GetMapping("/live")
    public ResponseEntity<ApiSuccessResponseDto<NotificationHealthLiveResponseDto>> checkLiveness() {

        NotificationHealthLiveResponseDto response = notificationHealthCheckLiveService.checkLiveness();

        return ResponseEntity.status(HttpStatus.OK).body(
                ApiSuccessResponseDto.<NotificationHealthLiveResponseDto>builder()
                        .success(true)
                        .message("Notification queue processor liveness check")
                        .data(response)
                        .build());
    }
}
