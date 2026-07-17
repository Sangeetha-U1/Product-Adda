package com.productadda.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.productadda.entity.Notification;
import com.productadda.entity.NotificationLog;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, UUID> {

    /*
     * Used by HistoryService to enrich a notification's history row
     * with its most recent attempt's error message (null if that
     * attempt succeeded or no attempt has happened yet).
     */
    Optional<NotificationLog> findTopByFkNotificationOrderByCreatedAtUtcDesc(Notification notification);

    /*
     * Used by HealthCheckService: every FAILED/MAX_RETRIES_FAILED log
     * entry for a given channel within a time window, for error-pattern
     * classification via NotificationAnalyzer.classifyError().
     */
    @Query("SELECT nl FROM NotificationLog nl WHERE nl.fkNotification.fkChannel.channelName = :channel "
            + "AND nl.fkDispatchStatus.statusName IN ('FAILED', 'MAX_RETRIES_FAILED') "
            + "AND nl.createdAtUtc >= :sinceUtc")
    List<NotificationLog> findFailedLogsByChannelSince(
            @Param("channel") String channel,
            @Param("sinceUtc") LocalDateTime sinceUtc);
}
