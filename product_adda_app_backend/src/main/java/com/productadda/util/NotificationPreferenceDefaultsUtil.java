package com.productadda.util;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import com.productadda.entity.NotificationPreference;
import com.productadda.entity.RecipientType;

/*
 * ================================================================
 * NotificationPreferenceDefaultsUtil
 * Plain static utility, not a Spring bean -- shared default-value
 * source for two call sites that must never drift apart:
 * PreferenceGetPreferencesService/PreferenceUpdatePreferencesService
 * (which persist this on first API access) and
 * NotificationPreferenceGateService (which uses it in-memory only,
 * never persisted, per the "lazy creation, queue processor treats
 * missing preferences as defaults").
 * ================================================================
 */
public final class NotificationPreferenceDefaultsUtil {

    public static final boolean DEFAULT_NOTIFICATIONS_ENABLED = true;
    public static final boolean DEFAULT_EMAIL_ENABLED = true;
    public static final boolean DEFAULT_SMS_ENABLED = false;
    public static final boolean DEFAULT_PUSH_ENABLED = false;
    public static final boolean DEFAULT_IN_APP_ENABLED = true;
    public static final LocalTime DEFAULT_QUIET_HOURS_START = LocalTime.of(22, 0);
    public static final LocalTime DEFAULT_QUIET_HOURS_END = LocalTime.of(8, 0);

    private NotificationPreferenceDefaultsUtil() {
    }

    /*
     * Builds a NotificationPreference with default values. Not
     * persisted by this method -- the caller decides whether to save
     * it (API lazy-create path) or just use it transiently (queue
     * processor gate path).
     */
    public static NotificationPreference buildDefault(RecipientType recipientType, UUID recipientId,
            UUID newPreferenceId) {
        return NotificationPreference.builder()
                .pkPreferenceId(newPreferenceId)
                .fkRecipientType(recipientType)
                .recipientId(recipientId)
                .notificationsEnabled(DEFAULT_NOTIFICATIONS_ENABLED)
                .emailEnabled(DEFAULT_EMAIL_ENABLED)
                .smsEnabled(DEFAULT_SMS_ENABLED)
                .pushEnabled(DEFAULT_PUSH_ENABLED)
                .inAppEnabled(DEFAULT_IN_APP_ENABLED)
                .quietHoursStart(DEFAULT_QUIET_HOURS_START)
                .quietHoursEnd(DEFAULT_QUIET_HOURS_END)
                .eventOptOuts(List.of())
                .isActive(true)
                .build();
    }
}
