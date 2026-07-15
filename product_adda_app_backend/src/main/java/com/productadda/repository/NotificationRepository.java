package com.productadda.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.productadda.entity.Notification;
import com.productadda.entity.User;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    /*
     * Used by NotificationsGetMyNotificationsService (in-app notification
     * center) -- filters to a single channel by name via the fk_channel
     * relationship, so the caller passes "IN_APP" without needing to
     * resolve a NotificationChannel entity first.
     */
    Page<Notification> findByFkUserAndFkChannel_ChannelName(User user, String channelName, Pageable pageable);

    /*
     * Used by NotificationQueueProcessorService to fetch the next batch
     * ready for dispatch: PENDING rows (never attempted) and RETRYING
     * rows whose backoff window has elapsed (or was never set).
     */
    @Query("SELECT n FROM Notification n "
            + "WHERE n.fkDispatchStatus.statusName IN :statuses "
            + "AND (n.nextRetryAtUtc IS NULL OR n.nextRetryAtUtc <= :now) "
            + "ORDER BY n.createdAtUtc ASC")
    List<Notification> findBatchForDispatch(
            @Param("statuses") List<String> statuses,
            @Param("now") LocalDateTime now,
            Pageable pageable);
}
