package com.productadda.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    /*
     * =========================================================
     * PRIMARY KEY
     * =========================================================
     */
    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "pk_payment_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID pkPaymentId;

    /*
     * =========================================================
     * RELATIONSHIPS
     * =========================================================
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_order_id", nullable = false, unique = true)
    private Order fkOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_status_id", nullable = false)
    private PaymentStatus fkStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_gateway_id", nullable = false)
    private PaymentGateway fkGateway;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_user_id", nullable = false)
    private User fkUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_payment_source_id")
    private PaymentMethod fkPaymentSource;

    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "idempotency_key", columnDefinition = "BINARY(16)", nullable = false, unique = true)
    private UUID idempotencyKey;

    /*
     * =========================================================
     * PAYMENT DETAILS
     * =========================================================
     */
    @Column(name = "payment_method", nullable = false, length = 50)
    private String paymentMethod;

    @Column(name = "gateway_transaction_id", length = 255)
    private String gatewayTransactionId;

    @Column(name = "gateway_order_id", length = 255)
    private String gatewayOrderId;

    @Column(name = "gateway_payment_link_id", length = 255)
    private String gatewayPaymentLinkId;

    @Column(name = "gateway_signature", length = 500)
    private String gatewaySignature;

    @Lob
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "amount_in_paise", nullable = false)
    private Long amountInPaise;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "paid_at_utc")
    private LocalDateTime paidAtUtc;

    @Column(name = "captured_at_utc")
    private LocalDateTime capturedAtUtc;

    @Column(name = "failed_at_utc")
    private LocalDateTime failedAtUtc;

    @Column(name = "webhook_received_at_utc")
    private LocalDateTime webhookReceivedAtUtc;

    @Lob
    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata")
    private Map<String, Object> metadata;

    /*
     * =========================================================
     * STATUS
     * =========================================================
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

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