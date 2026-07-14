package com.productadda.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
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
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    public static final String SORT_BY_ORDER_CREATED_AT_UTC = "fkOrder.createdAtUtc";

    /*
     * =========================================================
     * PRIMARY KEY
     * =========================================================
     */
    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "pk_order_item_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID pkOrderItemId;

    /*
     * =========================================================
     * RELATIONSHIPS
     * =========================================================
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_order_id", nullable = false)
    private Order fkOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_product_id", nullable = false)
    private Product fkProduct;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_vendor_id", nullable = false)
    private Vendor fkVendor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_item_status_id", nullable = false)
    private ItemStatus fkItemStatus;

    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "fk_delivery_partner_id")
    // private DeliveryPartner fkDeliveryPartner;

    /*
     * =========================================================
     * ORDER ITEM DETAILS & SNAPSHOTS
     * =========================================================
     */
    @Column(name = "product_name_snapshot", nullable = false, length = 255)
    private String productNameSnapshot;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "line_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal lineTotal;

    // ==========================================
    // CANCELLATION TRACKING
    // ==========================================
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
     * =========================================================
     * AUDIT
     * =========================================================
     */
    @Column(name = "created_at_utc", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAtUtc;

    @Column(name = "updated_at_utc", nullable = false, insertable = false)
    private LocalDateTime updatedAtUtc;
}