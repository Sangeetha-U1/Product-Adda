package com.productadda.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "products")

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    /*
     * =========================================================
     * PRIMARY KEY
     * =========================================================
     */
    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "pk_product_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID pkProductId;

    /*
     * =========================================================
     * FOREIGN KEYS
     * =========================================================
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_vendor_id", nullable = false)
    private Vendor fkVendor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_category_id", nullable = false)
    private Category fkCategory;

    /*
     * =========================================================
     * PRODUCT INFO
     * =========================================================
     */

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "discount_price", precision = 10, scale = 2)
    private BigDecimal discountPrice;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

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