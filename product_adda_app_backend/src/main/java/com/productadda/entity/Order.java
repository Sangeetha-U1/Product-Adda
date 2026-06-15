package com.productadda.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "orders")

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    /*
     * =========================================================
     * PRIMARY KEY
     * =========================================================
     */
    @Id
    @Column(name = "pk_order_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID pkOrderId;

    /*
     * =========================================================
     * FOREIGN KEY → USERS
     * =========================================================
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_user_id", nullable = false)
    private User fkUser;

    /*
     * =========================================================
     * ORDER INFO
     * =========================================================
     */
    /*
     * =========================================================
     * FOREIGN KEY → ORDER STATUS LOOKUP
     * =========================================================
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_status_id", nullable = false)
    private OrderStatus fkStatus;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    /*
     * ===========================================================================
     * AUDIT
     * ===========================================================================
     */

    @Column(name = "created_at_utc", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAtUtc;
}