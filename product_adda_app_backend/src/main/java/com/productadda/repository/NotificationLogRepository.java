package com.productadda.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.NotificationLog;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, UUID> {

    // No custom finder methods needed save() only.
    // analytics queries will be added here when that work begins.
}
