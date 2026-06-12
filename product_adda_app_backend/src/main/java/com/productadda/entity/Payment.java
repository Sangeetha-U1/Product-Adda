package com.productadda.entity;

import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

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

    @Column(name = "payment_status", nullable = false, length = 50)
    private String paymentStatus;

    @Column(name = "transaction_ref", length = 255)
    private String transactionRef;

    @Column(name = "amount_paid", nullable = false, precision = 10, scale = 2)
    private BigDecimal amountPaid;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;
}