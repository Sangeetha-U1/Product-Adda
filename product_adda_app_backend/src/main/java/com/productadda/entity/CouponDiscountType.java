package com.productadda.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "coupon_discount_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponDiscountType {

    /*
     * =========================================================
     * PRIMARY KEY
     * =========================================================
     */
    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "pk_discount_type_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID pkDiscountTypeId;

    /*
     * =========================================================
     * DISCOUNT TYPE DETAILS
     * =========================================================
     */
    @Column(name = "discount_type_code", nullable = false, unique = true, length = 50)
    private String discountTypeCode;

    @Column(name = "discount_type_label", nullable = false, length = 100)
    private String discountTypeLabel;

    @Column(name = "display_order", nullable = false, unique = true)
    private Integer displayOrder;

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
