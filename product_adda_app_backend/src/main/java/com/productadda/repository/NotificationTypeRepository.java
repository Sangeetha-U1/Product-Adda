package com.productadda.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.NotificationType;

public interface NotificationTypeRepository extends JpaRepository<NotificationType, UUID> {

    Optional<NotificationType> findByNotificationTypeName(String notificationTypeName);
}
