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

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/*
 * ================================================================
 * NEW ENTITY: RefundLineItem
 * Partial-refund line-item mapping. fkOrderItem references the real
 * OrderItem entity (order_items table) - the original draft
 * called this order_line_items, which does not exist in this schema.
 * No updatedAtUtc: line items are immutable once created (a refund
 * cannot be edited in place, only superseded by a new Refund).
 * ================================================================
 */
@Entity
@Table(name = "refund_line_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundLineItem {

    /*
     * =========================================================
     * PRIMARY KEY
     * =========================================================
     */
    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "pk_refund_line_item_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID pkRefundLineItemId;

    /*
     * =========================================================
     * RELATIONSHIPS
     * =========================================================
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_refund_id", nullable = false)
    private Refund fkRefund;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_order_item_id", nullable = false)
    private OrderItem fkOrderItem;

    /*
     * =========================================================
     * REFUND LINE ITEM DETAILS
     * =========================================================
     */
    @Column(name = "refund_amount_in_paise", nullable = false)
    private Long refundAmountInPaise;

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
}
