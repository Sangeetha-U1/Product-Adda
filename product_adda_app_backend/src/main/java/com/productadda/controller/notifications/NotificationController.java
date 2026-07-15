package com.productadda.controller.notifications;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.notifications.PaginatedNotificationResponseDto;

import com.productadda.service.notifications.NotificationsGetMyNotificationsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationsGetMyNotificationsService notificationsGetMyNotificationsService;

    /*
     * ================================================================
     * GET MY NOTIFICATIONS
     * GET /api/notifications/my-notifications
     * Auth Required - any authenticated role (customer/vendor/admin/
     * delivery partner all read their own notifications via this
     * single endpoint).
     * ================================================================
     */
    @GetMapping("/my-notifications")
    public ResponseEntity<ApiSuccessResponseDto<PaginatedNotificationResponseDto>> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {

        PaginatedNotificationResponseDto response = notificationsGetMyNotificationsService
                .getMyNotifications(page, pageSize);

        return ResponseEntity.status(HttpStatus.OK).body(
                ApiSuccessResponseDto.<PaginatedNotificationResponseDto>builder()
                        .success(true)
                        .message("In-app notifications retrieved successfully")
                        .data(response)
                        .build());
    }
}
