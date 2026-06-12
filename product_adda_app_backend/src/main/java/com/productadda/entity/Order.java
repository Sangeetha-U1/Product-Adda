package com.productadda.entity;

import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
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
    @JdbcTypeCode(SqlTypes.BINARY)
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
    @Column(name = "order_status", nullable = false, length = 50)
    private String orderStatus;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
}