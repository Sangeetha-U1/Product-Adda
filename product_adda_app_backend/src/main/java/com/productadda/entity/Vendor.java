package com.productadda.entity;

import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

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
     * FOREIGN KEY → USERS
     * =========================================================
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_user_id", nullable = false)
    private User fkUser;

    /*
     * =========================================================
     * BUSINESS INFO
     * =========================================================
     */
    @Column(name = "business_name", nullable = false, length = 255)
    private String businessName;

    @Column(name = "gst_number", nullable = false, length = 50, unique = true)
    private String gstNumber;
}