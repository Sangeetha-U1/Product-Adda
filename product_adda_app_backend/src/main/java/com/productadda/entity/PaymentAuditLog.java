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
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "payment_audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentAuditLog {

    // TODO: Consider adding fk_reconciliation_log_id to payment_audit_logs
    // instead of storing reconciliationLogId only inside JSON details.

    /*
     * =========================================================
     * PRIMARY KEY
     * =========================================================
     */
    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "pk_audit_log_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID pkAuditLogId;

    /*
     * =========================================================
     * RELATIONSHIPS
     * =========================================================
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_payment_id")
    private Payment fkPayment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_refund_id")
    private Refund fkRefund;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_actor_id")
    private User fkActor;

    /*
     * =========================================================
     * AUDIT DETAILS
     * =========================================================
     */
    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @Column(name = "actor_role", length = 30)
    private String actorRole;

    @Column(name = "old_status", length = 50)
    private String oldStatus;

    @Column(name = "new_status", length = 50)
    private String newStatus;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata")
    private Map<String, Object> metadata;

    /*
     * ===========================================================================
     * AUDIT
     * ===========================================================================
     */
    @Column(name = "created_at_utc", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAtUtc;
}