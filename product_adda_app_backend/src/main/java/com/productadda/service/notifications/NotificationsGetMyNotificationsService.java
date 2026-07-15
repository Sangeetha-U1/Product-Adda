package com.productadda.service.notifications;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.notifications.NotificationListItemDto;
import com.productadda.dto.notifications.PaginatedNotificationResponseDto;

import com.productadda.entity.Notification;
import com.productadda.entity.User;

import com.productadda.exception.ApiException;

import com.productadda.repository.NotificationRepository;
import com.productadda.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationsGetMyNotificationsService {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    /*
     * ================================================================
     * GET MY NOTIFICATIONS
     * Description: Paginated in-app notification center for the
     * authenticated user, regardless of which role(s) they hold --
     * vendors/delivery-partners/admins all read this via their same
     * underlying users row. Filters to IN_APP channel only.
     * ================================================================
     */
    @Transactional(readOnly = true)
    public PaginatedNotificationResponseDto getMyNotifications(int page, int pageSize) {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        int safePage = page < 0 ? 0 : page;
        int safePageSize = (pageSize <= 0 || pageSize > 100) ? 20 : pageSize;

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
        PageRequest pageRequest = PageRequest.of(safePage, safePageSize,
                Sort.by(Sort.Direction.DESC, "createdAtUtc"));

        Page<Notification> notificationPage = notificationRepository
                .findByFkUserAndFkChannel_ChannelName(currentUser, "IN_APP", pageRequest);

        /*
         * ================================================================
         * 3. DB SAVING SECTION (Skip if Read-Only GET)
         * Reason: Read-only paginated lookup, no mutations performed.
         * ================================================================
         */

        /*
         * ================================================================
         * 4. POST-SAVING DATA SANITIZATION & MASKING
         * ================================================================
         */
        List<NotificationListItemDto> items = notificationPage.getContent().stream()
                .map(notification -> NotificationListItemDto.builder()
                        .notificationId(notification.getPkNotificationId())
                        .notificationTypeName(notification.getFkType() != null
                                ? notification.getFkType().getNotificationTypeName()
                                : null)
                        .title(notification.getTitle())
                        .message(notification.getMessage())
                        .isRead(notification.getIsRead())
                        .dispatchStatusName(notification.getFkDispatchStatus() != null
                                ? notification.getFkDispatchStatus().getStatusName()
                                : null)
                        .sentAtUtc(notification.getSentAtUtc())
                        .readAtUtc(notification.getReadAtUtc())
                        .createdAtUtc(notification.getCreatedAtUtc())
                        .build())
                .collect(Collectors.toList());

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * ================================================================
         */
        return PaginatedNotificationResponseDto.builder()
                .notifications(items)
                .totalCount(notificationPage.getTotalElements())
                .page(safePage)
                .pageSize(safePageSize)
                .build();
    }
}
