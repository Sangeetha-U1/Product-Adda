package com.productadda.service.notifications;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.notifications.MarkAllReadResponseDto;

import com.productadda.entity.Notification;
import com.productadda.entity.User;

import com.productadda.exception.ApiException;

import com.productadda.repository.NotificationRepository;
import com.productadda.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationsMarkAllAsReadService {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    /*
     * ================================================================
     * MARK ALL AS READ
     * Description: Marks every unread IN_APP notification owned by the
     * authenticated user as read in one call.
     * ================================================================
     */
    @Transactional
    public MarkAllReadResponseDto markAllAsRead() {

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

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        List<Notification> unreadNotifications = notificationRepository
                .findByFkUserAndFkChannel_ChannelNameAndIsReadFalse(currentUser, "IN_APP");

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        for (Notification notification : unreadNotifications) {
            notification.setIsRead(true);
            notification.setReadAtUtc(now);
        }

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * ================================================================
         */
        notificationRepository.saveAll(unreadNotifications);

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
        return MarkAllReadResponseDto.builder()
                .markedReadCount(unreadNotifications.size())
                .build();
    }
}
