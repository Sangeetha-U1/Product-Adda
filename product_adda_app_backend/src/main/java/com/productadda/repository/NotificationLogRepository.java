package com.productadda.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.NotificationLog;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, UUID> {

    // No custom finder methods needed for Day 1 -- save() only.
    // Day 4 analytics queries will be added here when that work begins.
}
