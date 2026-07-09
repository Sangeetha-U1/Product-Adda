package com.productadda.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

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
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    /*
     * =========================================================
     * PRIMARY KEY
     * =========================================================
     */
    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "pk_invoice_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID pkInvoiceId;

    /*
     * =========================================================
     * FOREIGN KEYS / RELATIONSHIPS
     * =========================================================
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_order_id", nullable = false, unique = true)
    private Order fkOrder;

    /*
     * =========================================================
     * INVOICE INFO
     * =========================================================
     */
    @Column(name = "invoice_number", nullable = false, unique = true, length = 100)
    private String invoiceNumber;

    @Column(name = "invoice_version", nullable = false)
    private Integer invoiceVersion;

    @Column(name = "invoice_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal invoiceAmount;

    @Column(name = "file_url", nullable = false, length = 1000)
    private String fileUrl;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

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