package com.productadda.controller.notifications;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.notifications.MarkAllReadResponseDto;
import com.productadda.dto.notifications.NotificationListItemDto;
import com.productadda.dto.notifications.PaginatedNotificationResponseDto;

import com.productadda.service.notifications.NotificationsGetMyNotificationsService;
import com.productadda.service.notifications.NotificationsMarkAllAsReadService;
import com.productadda.service.notifications.NotificationsMarkAsReadService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationsGetMyNotificationsService notificationsGetMyNotificationsService;
    private final NotificationsMarkAsReadService notificationsMarkAsReadService;
    private final NotificationsMarkAllAsReadService notificationsMarkAllAsReadService;

    /*
     * ================================================================
     * GET MY NOTIFICATIONS
     * GET /api/notifications/my-notifications
     * Auth Required - any authenticated role. unreadOnly consolidates the plan's separate GET
     * /api/notifications/center into this same endpoint.
     * ================================================================
     */
    @GetMapping("/my-notifications")
    public ResponseEntity<ApiSuccessResponseDto<PaginatedNotificationResponseDto>> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "false") boolean unreadOnly) {

        PaginatedNotificationResponseDto response = notificationsGetMyNotificationsService
                .getMyNotifications(page, pageSize, unreadOnly);

        return ResponseEntity.status(HttpStatus.OK).body(
                ApiSuccessResponseDto.<PaginatedNotificationResponseDto>builder()
                        .success(true)
                        .message("In-app notifications retrieved successfully")
                        .data(response)
                        .build());
    }

    /*
     * ================================================================
     * MARK NOTIFICATION AS READ
     * PATCH /api/notifications/{notificationId}/read
     * Auth Required - own notifications only, IN_APP channel only.
     * ================================================================
     */
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiSuccessResponseDto<NotificationListItemDto>> markAsRead(
            @PathVariable UUID notificationId) {

        NotificationListItemDto response = notificationsMarkAsReadService.markAsRead(notificationId);

        return ResponseEntity.status(HttpStatus.OK).body(
                ApiSuccessResponseDto.<NotificationListItemDto>builder()
                        .success(true)
                        .message("Notification marked as read")
                        .data(response)
                        .build());
    }

    /*
     * ================================================================
     * MARK ALL NOTIFICATIONS AS READ
     * PATCH /api/notifications/mark-all-read
     * Auth Required - own notifications only, IN_APP channel only.
     * ================================================================
     */
    @PatchMapping("/mark-all-read")
    public ResponseEntity<ApiSuccessResponseDto<MarkAllReadResponseDto>> markAllAsRead() {

        MarkAllReadResponseDto response = notificationsMarkAllAsReadService.markAllAsRead();

        return ResponseEntity.status(HttpStatus.OK).body(
                ApiSuccessResponseDto.<MarkAllReadResponseDto>builder()
                        .success(true)
                        .message("All notifications marked as read")
                        .data(response)
                        .build());
    }
}
