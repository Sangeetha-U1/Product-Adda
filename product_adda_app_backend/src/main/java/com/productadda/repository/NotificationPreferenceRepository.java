package com.productadda.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.NotificationPreference;
import com.productadda.entity.RecipientType;

public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, UUID> {

    Optional<NotificationPreference> findByFkRecipientTypeAndRecipientId(RecipientType recipientType, UUID recipientId);
}
