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

        /*
         * Day 4: general-purpose analytics fetch, used by
         * AnalyticsAdminService and HealthCheckService. eventType/channel
         * are optional narrowing filters (null = no filter on that
         * dimension). Aggregation itself happens in-memory via
         * NotificationAnalyzer, not in SQL, per Dheeraj's "notifications
         * table is the primary analytics source" decision.
         */
        @Query("SELECT n FROM Notification n WHERE n.createdAtUtc BETWEEN :fromUtc AND :toUtc "
                        + "AND (:eventType IS NULL OR n.fkType.notificationTypeName = :eventType) "
                        + "AND (:channel IS NULL OR n.fkChannel.channelName = :channel)")
        List<Notification> findForAnalytics(
                        @Param("fromUtc") LocalDateTime fromUtc,
                        @Param("toUtc") LocalDateTime toUtc,
                        @Param("eventType") String eventType,
                        @Param("channel") String channel);

        /*
         * Day 4: vendor-scoped analytics fetch. Simple scoping -- every
         * notification where this vendor's own user is the recipient AND
         * the recipient role is VENDOR (not a broader join through
         * order_items to every notification touching the vendor's orders,
         * e.g. the customer's own notifications for that order). See
         * CHANGES_DAY4.md for the reasoning.
         */
        @Query("SELECT n FROM Notification n WHERE n.fkUser = :vendorUser "
                        + "AND n.fkRecipientRole.roleName = 'VENDOR' "
                        + "AND n.createdAtUtc BETWEEN :fromUtc AND :toUtc")
        List<Notification> findForVendorAnalytics(
                        @Param("vendorUser") User vendorUser,
                        @Param("fromUtc") LocalDateTime fromUtc,
                        @Param("toUtc") LocalDateTime toUtc);

        /*
         * Day 4: notification history for a user, across ALL channels
         * (unlike the IN_APP-only my-notifications endpoint). Every filter
         * parameter is optional (null = no filter on that dimension).
         */
        @Query("SELECT n FROM Notification n WHERE n.fkUser = :user "
                        + "AND (:eventType IS NULL OR n.fkType.notificationTypeName = :eventType) "
                        + "AND (:channel IS NULL OR n.fkChannel.channelName = :channel) "
                        + "AND (:fromUtc IS NULL OR n.createdAtUtc >= :fromUtc) "
                        + "AND (:toUtc IS NULL OR n.createdAtUtc <= :toUtc) "
                        + "ORDER BY n.createdAtUtc DESC")
        Page<Notification> findHistoryForUser(
                        @Param("user") User user,
                        @Param("eventType") String eventType,
                        @Param("channel") String channel,
                        @Param("fromUtc") LocalDateTime fromUtc,
                        @Param("toUtc") LocalDateTime toUtc,
                        Pageable pageable);
}
