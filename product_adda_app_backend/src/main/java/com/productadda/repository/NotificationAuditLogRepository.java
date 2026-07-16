package com.productadda.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.NotificationAuditLog;

public interface NotificationAuditLogRepository extends JpaRepository<NotificationAuditLog, UUID> {

    // No custom finder methods needed save() only.
}
