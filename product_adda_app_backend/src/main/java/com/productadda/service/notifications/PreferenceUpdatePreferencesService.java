package com.productadda.service.notifications;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.notifications.NotificationPreferenceResponseDto;
import com.productadda.dto.notifications.PreferenceUpdateRequestDto;
import com.productadda.dto.notifications.RecipientTypeAndIdDto;

import com.productadda.entity.AuditActionType;
import com.productadda.entity.NotificationAuditLog;
import com.productadda.entity.NotificationPreference;
import com.productadda.entity.User;

import com.productadda.exception.ApiException;

import com.productadda.repository.AuditActionTypeRepository;
import com.productadda.repository.NotificationAuditLogRepository;
import com.productadda.repository.NotificationPreferenceRepository;
import com.productadda.repository.NotificationTypeRepository;
import com.productadda.repository.UserRepository;
import com.productadda.repository.UserRoleRepository;

import com.productadda.util.NotificationPreferenceDefaultsUtil;
import com.productadda.util.UuidUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PreferenceUpdatePreferencesService {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final NotificationTypeRepository notificationTypeRepository;
    private final AuditActionTypeRepository auditActionTypeRepository;
    private final NotificationAuditLogRepository notificationAuditLogRepository;
    private final RecipientTypeAndIdResolverService recipientTypeAndIdResolverService;
    private final UuidUtil uuidUtil;

    /*
     * ================================================================
     * UPDATE PREFERENCES
     * Description: Validates and applies a full preference update
     * (every field required -- PATCH semantics here mean "full
     * replacement", not partial merge) for the authenticated user's own
     * recipient identity. Logs the change to notification_audit_logs
     * with fk_admin_user_id = null, since this is self-service, not an
     * admin-initiated change.
     * ================================================================
     */
    @Transactional
    public NotificationPreferenceResponseDto updatePreferences(PreferenceUpdateRequestDto requestDto) {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        if (requestDto == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Request body must not be null");
        }
        if (requestDto.getNotificationsEnabled() == null || requestDto.getEmailEnabled() == null
                || requestDto.getSmsEnabled() == null || requestDto.getPushEnabled() == null
                || requestDto.getInAppEnabled() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "notificationsEnabled, emailEnabled, smsEnabled, pushEnabled and inAppEnabled are all required booleans");
        }

        LocalTime quietHoursStart = parseTimeOrThrow(requestDto.getQuietHoursStart(), "quietHoursStart");
        LocalTime quietHoursEnd = parseTimeOrThrow(requestDto.getQuietHoursEnd(), "quietHoursEnd");

        if (quietHoursStart.equals(quietHoursEnd)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "quietHoursStart and quietHoursEnd must not be equal");
        }

        List<String> eventOptOuts = requestDto.getEventOptOuts() == null ? List.of() : requestDto.getEventOptOuts();

        for (String eventTypeName : eventOptOuts) {
            notificationTypeRepository.findByNotificationTypeName(eventTypeName)
                    .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST,
                            "eventOptOuts contains an unknown notification type: " + eventTypeName));
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

        String callerRoleName = resolveCallerRoleName(currentUser);

        RecipientTypeAndIdDto recipientTypeAndId = recipientTypeAndIdResolverService.resolve(currentUser,
                callerRoleName);

        AuditActionType preferenceUpdatedActionType = auditActionTypeRepository
                .findByActionTypeName("PREFERENCE_UPDATED")
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "PREFERENCE_UPDATED audit action type lookup row not found"));

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        NotificationPreference preference = notificationPreferenceRepository
                .findByFkRecipientTypeAndRecipientId(recipientTypeAndId.getRecipientType(),
                        recipientTypeAndId.getRecipientId())
                .orElseGet(() -> NotificationPreferenceDefaultsUtil.buildDefault(
                        recipientTypeAndId.getRecipientType(), recipientTypeAndId.getRecipientId(),
                        uuidUtil.generateUuidV7()));

        Map<String, Object> oldValue = buildAuditSnapshot(preference);

        preference.setNotificationsEnabled(requestDto.getNotificationsEnabled());
        preference.setEmailEnabled(requestDto.getEmailEnabled());
        preference.setSmsEnabled(requestDto.getSmsEnabled());
        preference.setPushEnabled(requestDto.getPushEnabled());
        preference.setInAppEnabled(requestDto.getInAppEnabled());
        preference.setQuietHoursStart(quietHoursStart);
        preference.setQuietHoursEnd(quietHoursEnd);
        preference.setEventOptOuts(eventOptOuts);

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * ================================================================
         */
        NotificationPreference savedPreference = notificationPreferenceRepository.save(preference);

        NotificationPreference reloadedPreference = notificationPreferenceRepository
                .findById(savedPreference.getPkPreferenceId())
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Preference persisted but could not be reloaded"));

        Map<String, Object> newValue = buildAuditSnapshot(reloadedPreference);

        NotificationAuditLog auditLog = NotificationAuditLog.builder()
                .pkAuditLogId(uuidUtil.generateUuidV7())
                .fkAdminUser(null)
                .fkAuditActionType(preferenceUpdatedActionType)
                .targetTable("notification_preferences")
                .targetId(reloadedPreference.getPkPreferenceId())
                .oldValue(oldValue)
                .newValue(newValue)
                .build();

        notificationAuditLogRepository.save(auditLog);

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
        return NotificationPreferenceResponseDto.builder()
                .preferenceId(reloadedPreference.getPkPreferenceId())
                .notificationsEnabled(reloadedPreference.getNotificationsEnabled())
                .emailEnabled(reloadedPreference.getEmailEnabled())
                .smsEnabled(reloadedPreference.getSmsEnabled())
                .pushEnabled(reloadedPreference.getPushEnabled())
                .inAppEnabled(reloadedPreference.getInAppEnabled())
                .quietHoursStart(reloadedPreference.getQuietHoursStart().format(TIME_FORMAT))
                .quietHoursEnd(reloadedPreference.getQuietHoursEnd().format(TIME_FORMAT))
                .eventOptOuts(reloadedPreference.getEventOptOuts())
                .updatedAtUtc(reloadedPreference.getUpdatedAtUtc())
                .build();
    }

    /*
     * ================================================================
     * PARSE TIME OR THROW (private helper)
     * ================================================================
     */
    private LocalTime parseTimeOrThrow(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, fieldName + " is required in HH:mm format");
        }
        try {
            return LocalTime.parse(value.trim(), TIME_FORMAT);
        } catch (DateTimeParseException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    fieldName + " must be valid HH:mm format (00:00 - 23:59)");
        }
    }

    /*
     * ================================================================
     * RESOLVE CALLER ROLE NAME (private helper)
     * ================================================================
     */
    private String resolveCallerRoleName(User user) {
        List<String> roleNames = userRoleRepository.findByFkUser(user).stream()
                .map(userRole -> userRole.getFkRole().getRoleName())
                .toList();

        if (roleNames.contains("VENDOR")) {
            return "VENDOR";
        }
        if (roleNames.contains("DELIVERY_PARTNER")) {
            return "DELIVERY_PARTNER";
        }
        return "CUSTOMER";
    }

    /*
     * ================================================================
     * BUILD AUDIT SNAPSHOT (private helper)
     * ================================================================
     */
    private Map<String, Object> buildAuditSnapshot(NotificationPreference preference) {
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("notificationsEnabled", preference.getNotificationsEnabled());
        snapshot.put("emailEnabled", preference.getEmailEnabled());
        snapshot.put("smsEnabled", preference.getSmsEnabled());
        snapshot.put("pushEnabled", preference.getPushEnabled());
        snapshot.put("inAppEnabled", preference.getInAppEnabled());
        snapshot.put("quietHoursStart", preference.getQuietHoursStart() != null
                ? preference.getQuietHoursStart().format(TIME_FORMAT)
                : null);
        snapshot.put("quietHoursEnd", preference.getQuietHoursEnd() != null
                ? preference.getQuietHoursEnd().format(TIME_FORMAT)
                : null);
        snapshot.put("eventOptOuts", preference.getEventOptOuts());
        return snapshot;
    }
}
