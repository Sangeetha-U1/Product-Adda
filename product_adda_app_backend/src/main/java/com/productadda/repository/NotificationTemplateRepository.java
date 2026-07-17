package com.productadda.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.NotificationChannel;
import com.productadda.entity.NotificationTemplate;
import com.productadda.entity.NotificationType;
import com.productadda.entity.RecipientRole;

public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, UUID> {

    /*
     * Role-specific lookup, tried first by TemplateContentResolverService.
     */
    Optional<NotificationTemplate> findByFkTypeAndFkChannelAndFkRecipientRole(
            NotificationType type, NotificationChannel channel, RecipientRole role);

    /*
     * Role-agnostic fallback lookup (fk_recipient_role_id IS NULL),
     * tried second by TemplateContentResolverService.
     */
    Optional<NotificationTemplate> findByFkTypeAndFkChannelAndFkRecipientRoleIsNull(
            NotificationType type, NotificationChannel channel);

    /*
     * Used by TemplateGetAllTemplatesService -- fetches everything, with
     * optional eventType/channel filtering done in the service layer
     * given the tiny dataset size (no pagination needed per Dheeraj's
     * Day 3 decision).
     */
    List<NotificationTemplate> findAllByOrderByFkType_NotificationTypeNameAscFkChannel_ChannelNameAsc();
}
