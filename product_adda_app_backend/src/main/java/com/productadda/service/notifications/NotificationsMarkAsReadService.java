package com.productadda.service.notifications;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.notifications.NotificationListItemDto;

import com.productadda.entity.Notification;
import com.productadda.entity.User;

import com.productadda.exception.ApiException;

import com.productadda.repository.NotificationRepository;
import com.productadda.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationsMarkAsReadService {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    /*
     * ================================================================
     * MARK AS READ
     * Description: Marks a single IN_APP notification owned by the
     * authenticated user as read. Scoped to IN_APP channel only --
     * marking an EMAIL/SMS row "read" has no meaningful semantics in
     * this system, so a non-IN_APP notificationId is rejected the same
     * way a not-owned one is (404-shaped, no existence leak).
     * ================================================================
     */
    @Transactional
    public NotificationListItemDto markAsRead(UUID notificationId) {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        if (notificationId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "notificationId is required");
        }

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

        Notification notification = notificationRepository
                .findByPkNotificationIdAndFkUser(notificationId, currentUser)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Notification not found"));

        if (notification.getFkChannel() == null || !"IN_APP".equals(notification.getFkChannel().getChannelName())) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Notification not found");
        }

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        notification.setIsRead(true);
        notification.setReadAtUtc(now);

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * ================================================================
         */
        Notification savedNotification = notificationRepository.save(notification);

        /*
         * ================================================================
         * 4. POST-SAVING DATA SANITIZATION & MASKING
         * Reason: No sensitive fields to strip.
         * ================================================================
         */

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * ================================================================
         */
        return NotificationListItemDto.builder()
                .notificationId(savedNotification.getPkNotificationId())
                .notificationTypeName(savedNotification.getFkType() != null
                        ? savedNotification.getFkType().getNotificationTypeName()
                        : null)
                .title(savedNotification.getTitle())
                .message(savedNotification.getMessage())
                .isRead(savedNotification.getIsRead())
                .dispatchStatusName(savedNotification.getFkDispatchStatus() != null
                        ? savedNotification.getFkDispatchStatus().getStatusName()
                        : null)
                .sentAtUtc(savedNotification.getSentAtUtc())
                .readAtUtc(savedNotification.getReadAtUtc())
                .createdAtUtc(savedNotification.getCreatedAtUtc())
                .build();
    }
}
