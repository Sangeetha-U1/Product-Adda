package com.productadda.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "carts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {

    /*
     * =========================================================
     * PRIMARY KEY
     * =========================================================
     */
    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "pk_cart_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID pkCartId;

    /*
     * =========================================================
     * RELATIONSHIPS
     * =========================================================
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_user_id", nullable = false, unique = true)
    private User fkUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_cart_status_id", nullable = false)
    private CartStatus fkCartStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_coupon_id")
    private Coupon fkCoupon;

    /*
     * =========================================================
     * STATUS & EXPIRATION
     * =========================================================
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "expires_at_utc")
    private LocalDateTime expiresAtUtc;

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