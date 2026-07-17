package com.productadda.service.notifications;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.analyzer.notifications.NotificationAnalyzer;

import com.productadda.dto.notifications.VendorAnalyticsDto;

import com.productadda.entity.Notification;
import com.productadda.entity.User;
import com.productadda.entity.UserRole;
import com.productadda.entity.Vendor;

import com.productadda.exception.ApiException;

import com.productadda.repository.NotificationRepository;
import com.productadda.repository.UserRepository;
import com.productadda.repository.UserRoleRepository;
import com.productadda.repository.VendorRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnalyticsVendorService {

    private static final int DEFAULT_RANGE_DAYS = 30;

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final VendorRepository vendorRepository;
    private final NotificationRepository notificationRepository;

    /*
     * ================================================================
     * GET VENDOR ANALYTICS
     * Description: Vendor-only. Returns the authenticated vendor's own
     * notification delivery breakdown by event type, for a date range
     * (defaults to the last 30 days). Scoping is simple: every
     * `notifications` row where fk_user is this vendor's own user AND
     * fk_recipient_role = VENDOR -- not a broader join through
     * order_items to every notification touching the vendor's orders
     * (which would also pull in the customer's own notifications for
     * those orders). for the reasoning; vendor is
     * always resolved server-side from the authenticated user, never
     * from a client-supplied vendorId.
     * ================================================================
     */
    @Transactional(readOnly = true)
    public VendorAnalyticsDto getVendorAnalytics(String dateFrom, String dateTo) {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        LocalDateTime toUtc = parseOptionalDateEnd(dateTo, "dateTo", now);
        LocalDateTime fromUtc = parseOptionalDateStart(dateFrom, "dateFrom", toUtc.minusDays(DEFAULT_RANGE_DAYS));

        if (fromUtc.isAfter(toUtc)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "dateFrom must not be after dateTo");
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

        List<UserRole> userRoles = userRoleRepository.findByFkUser(currentUser);

        boolean hasVendorRole = userRoles.stream()
                .map(userRole -> userRole.getFkRole().getRoleName())
                .anyMatch(roleName -> "VENDOR".equals(roleName));

        if (!hasVendorRole) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Vendor role required to access this endpoint");
        }

        Vendor vendor = vendorRepository.findByFkUser(currentUser)
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN,
                        "Vendor role required to access this endpoint"));

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        List<Notification> notifications = notificationRepository.findForVendorAnalytics(
                currentUser, fromUtc, toUtc);

        /*
         * ================================================================
         * 3. DB SAVING SECTION (Skip if Read-Only GET)
         * Reason: Read-only aggregation, no mutations performed.
         * ================================================================
         */

        /*
         * ================================================================
         * 4. POST-SAVING DATA SANITIZATION & MASKING
         * Reason: No sensitive fields to strip -- vendor sees only its
         * own aggregate counts, not other vendors' or customers' data.
         * ================================================================
         */

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * ================================================================
         */
        return VendorAnalyticsDto.builder()
                .vendorId(vendor.getPkVendorId())
                .dateFrom(fromUtc)
                .dateTo(toUtc)
                .byEvent(NotificationAnalyzer.groupByEvent(notifications))
                .build();
    }

    /*
     * ================================================================
     * PARSE OPTIONAL DATE START/END (private helpers)
     * ================================================================
     */
    private LocalDateTime parseOptionalDateStart(String dateStr, String fieldName, LocalDateTime defaultValue) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return LocalDate.parse(dateStr.trim()).atStartOfDay();
        } catch (DateTimeParseException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, fieldName + " must be a valid ISO date (yyyy-MM-dd)");
        }
    }

    private LocalDateTime parseOptionalDateEnd(String dateStr, String fieldName, LocalDateTime defaultValue) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return LocalDate.parse(dateStr.trim()).atTime(23, 59, 59);
        } catch (DateTimeParseException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, fieldName + " must be a valid ISO date (yyyy-MM-dd)");
        }
    }
}
