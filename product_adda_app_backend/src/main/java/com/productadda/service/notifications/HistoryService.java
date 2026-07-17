package com.productadda.service.notifications;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
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

import com.productadda.dto.notifications.NotificationHistoryDto;
import com.productadda.dto.notifications.NotificationHistoryResponseDto;

import com.productadda.entity.Notification;
import com.productadda.entity.NotificationLog;
import com.productadda.entity.User;

import com.productadda.exception.ApiException;

import com.productadda.repository.NotificationLogRepository;
import com.productadda.repository.NotificationRepository;
import com.productadda.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationLogRepository notificationLogRepository;

    /*
     * ================================================================
     * GET HISTORY
     * Description: Paginated notification delivery history for the
     * authenticated user, across ALL channels (unlike the IN_APP-only
     * my-notifications endpoint from Day 1/2). One row per notification
     * (current/final status), not one row per retry attempt -- per
     * Dheeraj's Day 4 decision (option a). All filters optional; no
     * default date range (all-time unless the caller filters).
     * ================================================================
     */
    @Transactional(readOnly = true)
    public NotificationHistoryResponseDto getHistory(
            int page, int pageSize, String eventType, String channel, String dateFrom, String dateTo) {

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

        String safeEventType = (eventType == null || eventType.trim().isEmpty())
                ? null
                : eventType.trim().toUpperCase();
        String safeChannel = (channel == null || channel.trim().isEmpty())
                ? null
                : channel.trim().toUpperCase();

        LocalDateTime fromUtc = parseOptionalDateStart(dateFrom, "dateFrom");
        LocalDateTime toUtc = parseOptionalDateEnd(dateTo, "dateTo");

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

        Page<Notification> notificationPage = notificationRepository.findHistoryForUser(
                currentUser, safeEventType, safeChannel, fromUtc, toUtc, pageRequest);

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
        List<NotificationHistoryDto> items = notificationPage.getContent().stream()
                .map(this::mapToHistoryDto)
                .collect(Collectors.toList());

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * ================================================================
         */
        return NotificationHistoryResponseDto.builder()
                .notifications(items)
                .totalCount(notificationPage.getTotalElements())
                .page(safePage)
                .pageSize(safePageSize)
                .build();
    }

    /*
     * ================================================================
     * MAP TO HISTORY DTO (private helper)
     * Description: Enriches a notification with its most recent
     * dispatch attempt's error message, if it ever failed.
     * ================================================================
     */
    private NotificationHistoryDto mapToHistoryDto(Notification notification) {
        String errorMessage = notificationLogRepository
                .findTopByFkNotificationOrderByCreatedAtUtcDesc(notification)
                .map(NotificationLog::getErrorMessage)
                .orElse(null);

        return NotificationHistoryDto.builder()
                .notificationId(notification.getPkNotificationId())
                .eventType(notification.getFkType() != null
                        ? notification.getFkType().getNotificationTypeName()
                        : null)
                .channel(notification.getFkChannel() != null
                        ? notification.getFkChannel().getChannelName()
                        : null)
                .deliveryStatus(notification.getFkDispatchStatus() != null
                        ? notification.getFkDispatchStatus().getStatusName()
                        : null)
                .sentAtUtc(notification.getSentAtUtc())
                .errorMessage(errorMessage)
                .createdAtUtc(notification.getCreatedAtUtc())
                .build();
    }

    /*
     * ================================================================
     * PARSE OPTIONAL DATE START/END (private helpers)
     * ================================================================
     */
    private LocalDateTime parseOptionalDateStart(String dateStr, String fieldName) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr.trim()).atStartOfDay();
        } catch (DateTimeParseException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, fieldName + " must be a valid ISO date (yyyy-MM-dd)");
        }
    }

    private LocalDateTime parseOptionalDateEnd(String dateStr, String fieldName) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr.trim()).atTime(23, 59, 59);
        } catch (DateTimeParseException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, fieldName + " must be a valid ISO date (yyyy-MM-dd)");
        }
    }
}
