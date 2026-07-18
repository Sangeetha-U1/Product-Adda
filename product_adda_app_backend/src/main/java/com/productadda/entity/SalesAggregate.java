package com.productadda.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "sales_aggregates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesAggregate {

    /*
     * =========================================================
     * PRIMARY KEY
     * =========================================================
     */
    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "pk_sales_aggregate_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID pkSalesAggregateId;

    /*
     * =========================================================
     * FOREIGN KEYS / RELATIONSHIPS
     * =========================================================
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_vendor_id")
    private Vendor fkVendor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_category_id")
    private Category fkCategory;

    /*
     * =========================================================
     * AGGREGATION INFO
     * =========================================================
     */
    @Column(name = "aggregation_date", nullable = false)
    private LocalDate aggregationDate;

    @Column(name = "order_count", nullable = false)
    @Builder.Default
    private Integer orderCount = 0;

    @Column(name = "total_gmv", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalGmv = BigDecimal.ZERO;

    @Column(name = "cancelled_count", nullable = false)
    @Builder.Default
    private Integer cancelledCount = 0;

    @Column(name = "refunded_value", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal refundedValue = BigDecimal.ZERO;

    @Column(name = "avg_order_value", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal avgOrderValue = BigDecimal.ZERO;

    /*
     * =========================================================
     * STATUS
     * =========================================================
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /*
     * =========================================================
     * AUDIT
     * =========================================================
     */
    @Column(name = "created_at_utc", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAtUtc;

    @Column(name = "updated_at_utc", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAtUtc;
}