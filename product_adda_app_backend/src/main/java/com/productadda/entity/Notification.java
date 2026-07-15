package com.productadda.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    // Define your sorting constants here
    public static final String SORT_BY_CREATED_AT_UTC = "createdAtUtc";

    /*
     * =========================================================
     * PRIMARY KEY
     * =========================================================
     */
    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "pk_notification_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID pkNotificationId;

    // ==========================================
    // IDEMPOTENCY
    // Description: Generated fresh per EventListenerService*
    // invocation (Week 8 Day 1 decision, Q3) -- not a stable key
    // derived from business data. Combined with fk_user_id and
    // fk_channel_id via a composite unique key, it currently only
    // guards against the exact same insert executing twice within
    // one invocation (e.g. a retried transaction), not against the
    // same business event firing via two separate calls. See
    // WEEK_8_EXECUTION_PLAN_REVISED.md item 3 for the full caveat.
    // ==========================================
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "event_id", columnDefinition = "BINARY(16)")
    private UUID eventId;

    /*
     * =========================================================
     * RELATIONSHIPS
     * =========================================================
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_user_id", nullable = false)
    private User fkUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_type_id", nullable = false)
    private NotificationType fkType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_channel_id", nullable = false)
    private NotificationChannel fkChannel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_order_id")
    private Order fkOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_payment_id")
    private Payment fkPayment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_recipient_role_id")
    private RecipientRole fkRecipientRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_dispatch_status_id")
    private DispatchStatus fkDispatchStatus;

    /*
     * =========================================================
     * DISPATCH / RETRY BOOKKEEPING
     * =========================================================
     */
    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "next_retry_at_utc")
    private LocalDateTime nextRetryAtUtc;

    /*
     * =========================================================
     * RENDERED CONTENT
     * Description: title/message are the rendered subject/body for
     * this recipient+channel -- Day 1 populates these via
     * NotificationContentBuilder; Day 3 swaps the source to
     * notification_templates without changing this entity.
     * =========================================================
     */
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "sent_at_utc")
    private LocalDateTime sentAtUtc;

    @Column(name = "read_at_utc")
    private LocalDateTime readAtUtc;

    /*
     * =========================================================
     * STATUS
     * =========================================================
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /*
     * ===========================================================================
     * AUDIT
     * ===========================================================================
     */
    @Column(name = "created_at_utc", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAtUtc;

    @Column(name = "updated_at_utc", nullable = false, insertable = false)
    private LocalDateTime updatedAtUtc;
}
