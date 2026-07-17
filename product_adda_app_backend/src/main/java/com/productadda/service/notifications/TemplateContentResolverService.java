package com.productadda.service.notifications;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.productadda.dto.notifications.NotificationContentDto;

import com.productadda.entity.NotificationChannel;
import com.productadda.entity.NotificationTemplate;
import com.productadda.entity.NotificationType;
import com.productadda.entity.RecipientRole;

import com.productadda.repository.NotificationTemplateRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TemplateContentResolverService {

    private final NotificationTemplateRepository notificationTemplateRepository;
    private final TemplateRenderingEngine templateRenderingEngine;
    private final NotificationContentBuilder notificationContentBuilder;

    /*
     * ================================================================
     * RESOLVE CONTENT
     * Description: Looks up a notification_templates row matching
     * (type, channel, recipient role) first; if none, falls back to a
     * role-agnostic template (fk_recipient_role_id IS NULL) for that
     * same (type, channel); if still none, falls back to
     * hardcoded NotificationContentBuilder -- whether because no
     * template was ever seeded for this combination, or a future day's
     * delete feature removed it. Called once per channel, per
     * recipient, from inside NotificationCreationService's loop, so
     * EMAIL and IN_APP can genuinely differ in wording.
     * ================================================================
     */
    public NotificationContentDto resolveContent(NotificationType notificationType, NotificationChannel channel,
            RecipientRole recipientRole, Map<String, Object> context) {

        NotificationTemplate template = recipientRole != null
                ? notificationTemplateRepository
                        .findByFkTypeAndFkChannelAndFkRecipientRole(notificationType, channel, recipientRole)
                        .orElse(null)
                : null;

        if (template == null) {
            template = notificationTemplateRepository
                    .findByFkTypeAndFkChannelAndFkRecipientRoleIsNull(notificationType, channel)
                    .orElse(null);
        }

        if (template == null) {
            return notificationContentBuilder.buildContent(notificationType.getNotificationTypeName(), context);
        }

        String renderedSubject = templateRenderingEngine.render(template.getSubject(), context);
        String renderedBody = templateRenderingEngine.render(template.getBody(), context);

        return NotificationContentDto.builder()
                .subject(renderedSubject)
                .body(renderedBody)
                .build();
    }
}
