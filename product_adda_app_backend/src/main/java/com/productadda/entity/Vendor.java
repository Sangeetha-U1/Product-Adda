package com.productadda.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "vendors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vendor {

    /*
     * =========================================================
     * PRIMARY KEY
     * =========================================================
     */
    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "pk_vendor_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID pkVendorId;

    /*
     * =========================================================
     * RELATION → USERS (One-to-One unique constraint in DB)
     * =========================================================
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_user_id", nullable = false, unique = true)
    private User fkUser;

    /*
     * =========================================================
     * BUSINESS INFO
     * =========================================================
     */
    @Column(name = "business_name", nullable = false, length = 255)
    private String businessName;

    @Column(name = "store_name", nullable = false, length = 255)
    private String storeName;

    @Column(name = "gst_number", nullable = false, length = 50, unique = true)
    private String gstNumber;

    @Column(name = "business_description", columnDefinition = "TEXT")
    private String businessDescription;

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