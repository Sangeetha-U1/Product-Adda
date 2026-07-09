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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "coupons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {

    /*
     * =========================================================
     * PRIMARY KEY
     * =========================================================
     */
    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "pk_coupon_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID pkCouponId;

    /*
     * =========================================================
     * RELATIONSHIPS
     * =========================================================
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_discount_type_id", nullable = false)
    private CouponDiscountType fkDiscountType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_coupon_status_id", nullable = false)
    private CouponStatus fkCouponStatus;

    /*
     * =========================================================
     * COUPON DETAILS
     * =========================================================
     */
    @Column(name = "coupon_code", nullable = false, unique = true, length = 50)
    private String couponCode;

    @Lob
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "maximum_discount_amount", precision = 10, scale = 2)
    private BigDecimal maximumDiscountAmount;

    @Column(name = "minimum_purchase_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal minimumPurchaseAmount;

    @Column(name = "maximum_global_usage")
    private Integer maximumGlobalUsage;

    @Column(name = "maximum_user_usage")
    private Integer maximumUserUsage;

    @Column(name = "usage_count", nullable = false)
    private Integer usageCount;

    @Column(name = "is_one_time", nullable = false)
    private Boolean isOneTime;

    @Column(name = "starts_at_utc", nullable = false)
    private LocalDateTime startsAtUtc;

    @Column(name = "expires_at_utc", nullable = false)
    private LocalDateTime expiresAtUtc;

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