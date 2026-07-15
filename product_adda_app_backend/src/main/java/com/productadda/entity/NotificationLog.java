package com.productadda.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;

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
@Table(name = "notification_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationLog {

    /*
     * =========================================================
     * PRIMARY KEY
     * =========================================================
     */
    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "pk_log_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID pkLogId;

    /*
     * =========================================================
     * RELATIONSHIPS
     * =========================================================
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_notification_id", nullable = false)
    private Notification fkNotification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_dispatch_status_id", nullable = false)
    private DispatchStatus fkDispatchStatus;

    /*
     * =========================================================
     * ATTEMPT DETAILS
     * =========================================================
     */
    @Column(name = "attempt_number", nullable = false)
    private Integer attemptNumber;

    @Lob
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "dispatched_at_utc")
    private LocalDateTime dispatchedAtUtc;

    /*
     * =========================================================
     * AUDIT
     * =========================================================
     */
    @Column(name = "created_at_utc", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAtUtc;
}
