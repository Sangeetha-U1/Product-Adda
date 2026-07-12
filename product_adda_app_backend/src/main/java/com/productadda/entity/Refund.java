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
@Table(name = "refunds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Refund {

    /*
     * =========================================================
     * PRIMARY KEY
     * =========================================================
     */
    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "pk_refund_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID pkRefundId;

    /*
     * =========================================================
     * RELATIONSHIPS
     * =========================================================
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_payment_id", nullable = false)
    private Payment fkPayment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_order_id", nullable = false)
    private Order fkOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_status_id", nullable = false)
    private PaymentStatus fkStatus;

    /*
     * =========================================================
     * REFUND DETAILS
     * =========================================================
     */
    @Column(name = "refund_type", nullable = false, length = 20)
    private String refundType;

    @Column(name = "refund_amount_in_paise", nullable = false)
    private Long refundAmountInPaise;

    @Column(name = "gateway_refund_id", length = 255)
    private String gatewayRefundId;

    @Column(name = "initiated_by", nullable = false, length = 30)
    private String initiatedBy;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "refund_triggered_by_order_event", length = 30)
    private String refundTriggeredByOrderEvent;

    @Column(name = "processed_at_utc")
    private LocalDateTime processedAtUtc;

    @Column(name = "failed_at_utc")
    private LocalDateTime failedAtUtc;

    @Lob
    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    /*
     * =========================================================
     * STATUS / AUDIT
     * =========================================================
     */
    @Column(name = "created_at_utc", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAtUtc;

    @Column(name = "updated_at_utc", nullable = false, insertable = false)
    private LocalDateTime updatedAtUtc;
}