package com.productadda.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
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

@Entity
@Table(name = "vendor_bank_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorBankDetail {

    /*
     * =========================================================
     * PRIMARY KEY
     * =========================================================
     */
    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "pk_vendor_bank_detail_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID pkVendorBankDetailId;

    /*
     * =========================================================
     * RELATION → VENDORS (One-to-One unique constraint in DB)
     * =========================================================
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_vendor_id", nullable = false, unique = true)
    private Vendor fkVendor;

    /*
     * =========================================================
     * BANKING ROUTING METRICS
     * =========================================================
     */
    @Column(name = "account_holder_name", nullable = false, length = 255)
    private String accountHolderName;

    @Column(name = "bank_name", nullable = false, length = 255)
    private String bankName;

    @Column(name = "account_number", nullable = false, length = 50)
    private String accountNumber;

    @Column(name = "ifsc_code", nullable = false, length = 20)
    private String ifscCode;

    @Column(name = "branch_name", length = 255)
    private String branchName;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

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