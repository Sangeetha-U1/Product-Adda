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

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    @Column(name = "amount_paid", nullable = false, precision = 10, scale = 2)
    private BigDecimal amountPaid;

    @Column(name = "paid_at_utc")
    private LocalDateTime paidAtUtc;

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