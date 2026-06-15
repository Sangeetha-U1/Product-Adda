package com.productadda.entity;

import jakarta.persistence.*;
import lombok.*;

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

    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "pk_payment_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID pkPaymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_order_id", nullable = false)
    private Order fkOrder;

    @Column(name = "payment_method", nullable = false, length = 50)
    private String paymentMethod;

    /*
     * =========================================================
     * FOREIGN KEY → PAYMENT STATUS LOOKUP
     * =========================================================
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_status_id", nullable = false)
    private PaymentStatus fkStatus;

    @Column(name = "amount_paid", nullable = false, precision = 10, scale = 2)
    private BigDecimal amountPaid;

    @Column(name = "razorpay_order_id", nullable = true, length = 255)
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id", length = 255)
    private String razorpayPaymentId;

    @Column(name = "razorpay_payment_link_id", length = 255)
    private String razorpayPaymentLinkId;

    @Column(name = "razorpay_signature", length = 500)
    private String razorpaySignature;

    @Column(name = "paid_at_utc")
    private LocalDateTime paidAtUtc;

    /*
     * ===========================================================================
     * AUDIT
     * ===========================================================================
     */

    @Column(name = "created_at_utc", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAtUtc;

}
