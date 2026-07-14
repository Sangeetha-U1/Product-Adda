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

/*
 * ================================================================
 * PaymentReconciliationLog
 * One row per reconciliation run (scheduled or manual). Created as
 * RUNNING, then updated in place once the comparison completes -
 * no separate "final" row is inserted.
 * ================================================================
 */
@Entity
@Table(name = "payment_reconciliation_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentReconciliationLog {

    /*
     * =========================================================
     * PRIMARY KEY
     * =========================================================
     */
    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "pk_reconciliation_log_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID pkReconciliationLogId;

    /*
     * =========================================================
     * RELATIONSHIPS
     * =========================================================
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_gateway_id", nullable = false)
    private PaymentGateway fkGateway;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_status_id", nullable = false)
    private PaymentStatus fkStatus;

    // Nullable: a scheduled run has no human trigger
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_triggered_by_user_id")
    private User fkTriggeredByUser;

    /*
     * =========================================================
     * RECONCILIATION DETAILS
     * =========================================================
     */
    // "daily_scheduled" / "on_demand" / "manual"
    @Column(name = "reconciliation_type", nullable = false, length = 30)
    private String reconciliationType;

    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "reconciliation_run_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID reconciliationRunId;

    @Column(name = "total_gateway_transactions", nullable = false)
    private Integer totalGatewayTransactions;

    @Column(name = "total_db_records", nullable = false)
    private Integer totalDbRecords;

    @Column(name = "matched_records", nullable = false)
    private Integer matchedRecords;

    @Column(name = "missing_in_db", nullable = false)
    private Integer missingInDb;

    @Column(name = "orphaned_in_db", nullable = false)
    private Integer orphanedInDb;

    @Lob
    @Column(name = "discrepancy_details", columnDefinition = "json")
    private String discrepancyDetails;

    @Column(name = "reconciled_at_utc")
    private LocalDateTime reconciledAtUtc;

    /*
     * ===========================================================================
     * AUDIT
     * ===========================================================================
     */
    @Column(name = "created_at_utc", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAtUtc;
}
