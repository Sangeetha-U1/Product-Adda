package com.productadda.service.notifications;

import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.notifications.NotificationPreferenceResponseDto;
import com.productadda.dto.notifications.RecipientTypeAndIdDto;

import com.productadda.entity.NotificationPreference;
import com.productadda.entity.User;

import com.productadda.exception.ApiException;

import com.productadda.repository.NotificationPreferenceRepository;
import com.productadda.repository.UserRepository;
import com.productadda.repository.UserRoleRepository;

import com.productadda.util.NotificationPreferenceDefaultsUtil;
import com.productadda.util.UuidUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PreferenceGetPreferencesService {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final RecipientTypeAndIdResolverService recipientTypeAndIdResolverService;
    private final UuidUtil uuidUtil;

    /*
     * ================================================================
     * GET PREFERENCES
     * Description: Fetches the authenticated user's own notification
     * preferences. Determines recipient type via the caller's role
     * (VENDOR / DELIVERY_PARTNER take priority; everyone else,
     * including plain customers and admins, resolves to USER), since a
     * preferences row is per-role-identity, not per-raw-user. If none
     * exists yet, creates and persists a default row (lazy creation,
     * per decision -- no signup-flow changes).
     * ================================================================
     */
    @Transactional
    public NotificationPreferenceResponseDto getPreferences() {

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

        String callerRoleName = resolveCallerRoleName(currentUser);

        RecipientTypeAndIdDto recipientTypeAndId = recipientTypeAndIdResolverService.resolve(currentUser,
                callerRoleName);

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        NotificationPreference preference = notificationPreferenceRepository
                .findByFkRecipientTypeAndRecipientId(recipientTypeAndId.getRecipientType(),
                        recipientTypeAndId.getRecipientId())
                .orElse(null);

        boolean isNewlyCreated = preference == null;

        if (isNewlyCreated) {
            preference = NotificationPreferenceDefaultsUtil.buildDefault(
                    recipientTypeAndId.getRecipientType(), recipientTypeAndId.getRecipientId(),
                    uuidUtil.generateUuidV7());
        }

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * ================================================================
         */
        if (isNewlyCreated) {
            NotificationPreference savedPreference = notificationPreferenceRepository.save(preference);
            preference = notificationPreferenceRepository.findById(savedPreference.getPkPreferenceId())
                    .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Preference persisted but could not be reloaded"));
        }

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
        return mapToResponseDto(preference);
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
     * MAP TO RESPONSE DTO (private helper)
     * ================================================================
     */
    private NotificationPreferenceResponseDto mapToResponseDto(NotificationPreference preference) {
        return NotificationPreferenceResponseDto.builder()
                .preferenceId(preference.getPkPreferenceId())
                .notificationsEnabled(preference.getNotificationsEnabled())
                .emailEnabled(preference.getEmailEnabled())
                .smsEnabled(preference.getSmsEnabled())
                .pushEnabled(preference.getPushEnabled())
                .inAppEnabled(preference.getInAppEnabled())
                .quietHoursStart(preference.getQuietHoursStart() != null
                        ? preference.getQuietHoursStart().format(TIME_FORMAT)
                        : null)
                .quietHoursEnd(preference.getQuietHoursEnd() != null
                        ? preference.getQuietHoursEnd().format(TIME_FORMAT)
                        : null)
                .eventOptOuts(preference.getEventOptOuts())
                .updatedAtUtc(preference.getUpdatedAtUtc())
                .build();
    }
}
