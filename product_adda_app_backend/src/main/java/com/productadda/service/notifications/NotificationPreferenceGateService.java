package com.productadda.service.notifications;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.notifications.NotificationGateDecisionDto;
import com.productadda.dto.notifications.RecipientTypeAndIdDto;

import com.productadda.entity.Notification;
import com.productadda.entity.NotificationPreference;

import com.productadda.repository.NotificationPreferenceRepository;
import com.productadda.repository.NotificationRepository;

import com.productadda.util.NotificationPreferenceDefaultsUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationPreferenceGateService {

    private static final int RATE_LIMIT_MAX_PER_HOUR = 5;
    private static final long RATE_LIMIT_RETRY_DELAY_MINUTES = 15L;

    private final RecipientTypeAndIdResolverService recipientTypeAndIdResolverService;
    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final NotificationRepository notificationRepository;

    /*
     * ================================================================
     * EVALUATE
     * Description: Decides whether a PENDING/RETRYING notification
     * should actually be dispatched right now, based on
     * notification_preferences (master toggle, per-channel toggle,
     * event opt-out), quiet hours, and rate limiting. Missing
     * preferences are treated as defaults (no row is written here) --
     * per Dheeraj's "lazy creation" decision, only the preferences API
     * persists a row.
     *
     * Quiet hours are compared against server UTC time-of-day; this
     * schema has no per-user timezone column, so "quiet hours" is
     * effectively "quiet hours in UTC" today.
     * ================================================================
     */
    @Transactional(readOnly = true)
    public NotificationGateDecisionDto evaluate(Notification notification) {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * Reason: Caller (NotificationQueueProcessorService) only ever
         * passes an already-persisted, fully-loaded Notification.
         * ================================================================
         */

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        String recipientRoleName = notification.getFkRecipientRole() != null
                ? notification.getFkRecipientRole().getRoleName()
                : null;

        RecipientTypeAndIdDto recipientTypeAndId = recipientTypeAndIdResolverService.resolve(
                notification.getFkUser(), recipientRoleName);

        NotificationPreference preference = notificationPreferenceRepository
                .findByFkRecipientTypeAndRecipientId(recipientTypeAndId.getRecipientType(),
                        recipientTypeAndId.getRecipientId())
                .orElseGet(() -> NotificationPreferenceDefaultsUtil.buildDefault(
                        recipientTypeAndId.getRecipientType(), recipientTypeAndId.getRecipientId(), null));

        if (!Boolean.TRUE.equals(preference.getNotificationsEnabled())) {
            return NotificationGateDecisionDto.builder()
                    .decisionType("SKIP")
                    .reason("notifications_enabled is false for this recipient")
                    .build();
        }

        String channelName = notification.getFkChannel() != null
                ? notification.getFkChannel().getChannelName()
                : null;

        if (!isChannelEnabled(preference, channelName)) {
            return NotificationGateDecisionDto.builder()
                    .decisionType("SKIP")
                    .reason(channelName + "_enabled is false for this recipient")
                    .build();
        }

        String notificationTypeName = notification.getFkType() != null
                ? notification.getFkType().getNotificationTypeName()
                : null;

        List<String> optOuts = preference.getEventOptOuts();
        if (optOuts != null && notificationTypeName != null && optOuts.contains(notificationTypeName)) {
            return NotificationGateDecisionDto.builder()
                    .decisionType("SKIP")
                    .reason("Recipient opted out of " + notificationTypeName)
                    .build();
        }

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        LocalDateTime quietHoursDeferUntil = resolveQuietHoursDeferral(preference, now);
        if (quietHoursDeferUntil != null) {
            return NotificationGateDecisionDto.builder()
                    .decisionType("DEFER")
                    .deferUntilUtc(quietHoursDeferUntil)
                    .reason("Quiet hours active until " + preference.getQuietHoursEnd())
                    .build();
        }

        long sentInLastHour = notificationRepository.countByFkUserAndFkDispatchStatus_StatusNameAndSentAtUtcAfter(
                notification.getFkUser(), "SENT", now.minusHours(1));

        if (sentInLastHour >= RATE_LIMIT_MAX_PER_HOUR) {
            return NotificationGateDecisionDto.builder()
                    .decisionType("DEFER")
                    .deferUntilUtc(now.plusMinutes(RATE_LIMIT_RETRY_DELAY_MINUTES))
                    .reason("Rate limit reached: " + sentInLastHour + " sent in the last hour")
                    .build();
        }

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * Reason: Read-only evaluation, no mutations performed.
         * ================================================================
         */

        /*
         * ================================================================
         * 4. POST-SAVING DATA SANITIZATION & MASKING
         * Reason: N/A - see section 3.
         * ================================================================
         */

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * ================================================================
         */
        return NotificationGateDecisionDto.builder()
                .decisionType("SEND")
                .build();
    }

    /*
     * ================================================================
     * IS CHANNEL ENABLED (private helper)
     * ================================================================
     */
    private boolean isChannelEnabled(NotificationPreference preference, String channelName) {
        if ("EMAIL".equals(channelName)) {
            return Boolean.TRUE.equals(preference.getEmailEnabled());
        }
        if ("SMS".equals(channelName)) {
            return Boolean.TRUE.equals(preference.getSmsEnabled());
        }
        if ("PUSH".equals(channelName)) {
            return Boolean.TRUE.equals(preference.getPushEnabled());
        }
        if ("IN_APP".equals(channelName)) {
            return Boolean.TRUE.equals(preference.getInAppEnabled());
        }
        return false;
    }

    /*
     * ================================================================
     * RESOLVE QUIET HOURS DEFERRAL (private helper)
     * Description: Returns the UTC timestamp to defer until, if `now`
     * falls inside the recipient's quiet hours window; null if not in
     * quiet hours. Handles the overnight-wraparound case (e.g.
     * 22:00-08:00) as well as a same-day window (e.g. 08:00-22:00).
     * ================================================================
     */
    private LocalDateTime resolveQuietHoursDeferral(NotificationPreference preference, LocalDateTime now) {
        LocalTime start = preference.getQuietHoursStart();
        LocalTime end = preference.getQuietHoursEnd();

        if (start == null || end == null) {
            return null;
        }

        LocalTime currentTime = now.toLocalTime();
        boolean inQuietHours;

        if (start.isBefore(end)) {
            // Same-day window, e.g. 08:00 - 22:00
            inQuietHours = !currentTime.isBefore(start) && currentTime.isBefore(end);
        } else {
            // Overnight window, e.g. 22:00 - 08:00
            inQuietHours = !currentTime.isBefore(start) || currentTime.isBefore(end);
        }

        if (!inQuietHours) {
            return null;
        }

        LocalDateTime deferUntil = now.toLocalDate().atTime(end);
        if (!deferUntil.isAfter(now)) {
            deferUntil = deferUntil.plusDays(1);
        }
        return deferUntil;
    }
}
