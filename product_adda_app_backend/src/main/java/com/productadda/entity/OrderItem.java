package com.productadda.entity;

import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_items")

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "pk_order_item_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID pkOrderItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_order_id", nullable = false)
    private Order fkOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_product_id", nullable = false)
    private Product fkProduct;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;
}