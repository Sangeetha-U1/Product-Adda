package com.productadda.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.JoinColumn;

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
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    // Define your sorting constants here
    public static final String SORT_BY_CREATED_AT_UTC = "createdAtUtc";

    /*
     * =========================================================
     * PRIMARY KEY
     * =========================================================
     */
    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "pk_order_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID pkOrderId;

    /*
     * =========================================================
     * RELATIONSHIPS
     * =========================================================
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_user_id", nullable = false)
    private User fkUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_cart_id")
    private Cart fkCart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_status_id", nullable = false)
    private OrderStatus fkStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_address_id", nullable = false)
    private Address fkAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_coupon_id")
    private Coupon fkCoupon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_delivery_partner_id")
    private DeliveryPartner fkDeliveryPartner;

    @OneToOne(mappedBy = "fkOrder", fetch = FetchType.LAZY)
    private Payment payment;

    /*
     * =========================================================
     * PAYMENT TRACKING
     * =========================================================
     */
    @Column(name = "payment_initiated_at_utc")
    private LocalDateTime paymentInitiatedAtUtc;

    @Column(name = "payment_confirmed_at_utc")
    private LocalDateTime paymentConfirmedAtUtc;

    @Column(name = "total_refunded_amount_in_paise", nullable = false)
    @Builder.Default
    private Long totalRefundedAmountInPaise = 0L;

    /*
     * =========================================================
     * ORDER INFO
     * =========================================================
     */
    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "coupon_discount", nullable = false, precision = 10, scale = 2)
    private BigDecimal couponDiscount;

    @Column(name = "shipping_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal shippingCost;

    @Column(name = "tax_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal taxAmount;

    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "idempotency_key", columnDefinition = "BINARY(16)", nullable = false, unique = true)
    private UUID idempotencyKey;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    // ==========================================
    // CANCELLATION TRACKING
    // Description: cancelled_by is a plain string ("CUSTOMER"/"ADMIN")
    // capturing the acting role at cancellation time -- an audit label,
    // not a domain workflow status, so it is intentionally NOT a
    // lookup-table FK (Zero-Enum Rule targets state-machine values,
    // not free-text initiator labels).
    // ==========================================
    @Column(name = "cancelled_by", length = 50)
    private String cancelledBy;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @Column(name = "cancelled_at_utc")
    private LocalDateTime cancelledAtUtc;

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