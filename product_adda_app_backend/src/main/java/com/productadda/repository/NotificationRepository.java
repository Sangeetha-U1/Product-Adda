package com.productadda.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
     * Used by NotificationsGetMyNotificationsService. isReadValues is
     * List.of(false) for unreadOnly=true, or List.of(true, false) for
     * the full list -- one method covers both plain list and
     * unreadOnly filter without a second query.
     */
    Page<Notification> findByFkUserAndFkChannel_ChannelNameAndIsReadIn(
            User user, String channelName, List<Boolean> isReadValues, Pageable pageable);

    /*
     * Used by NotificationsMarkAllAsReadService.
     */
    List<Notification> findByFkUserAndFkChannel_ChannelNameAndIsReadFalse(User user, String channelName);

    /*
     * Used by NotificationsMarkAsReadService for the Zero-Trust
     * ownership check before marking a single notification read.
     */
    Optional<Notification> findByPkNotificationIdAndFkUser(UUID pkNotificationId, User user);

    /*
     * Used by NotificationPreferenceGateService for rate-limit
     * evaluation (how many notifications this user has actually had
     * SENT in the last hour, across all channels).
     */
    long countByFkUserAndFkDispatchStatus_StatusNameAndSentAtUtcAfter(
            User user, String statusName, LocalDateTime sentAfter);

    /*
     * Used by NotificationQueueProcessorService to fetch the next batch
     * ready for dispatch: PENDING rows (never attempted) and RETRYING
     * rows whose backoff window has elapsed (or was never set). Also
     * picks up rows deferred by NotificationPreferenceGateService for
     * quiet-hours/rate-limit reasons, since those stay PENDING/RETRYING
     * with next_retry_at_utc pushed forward rather than a new status.
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
