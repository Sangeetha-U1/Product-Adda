package com.productadda.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "vendor_payouts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorPayout {

    /*
     * =========================================================
     * PRIMARY KEY
     * =========================================================
     */
    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "pk_vendor_payout_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID pkVendorPayoutId;

    /*
     * =========================================================
     * RELATIONSHIPS
     * =========================================================
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_vendor_id", nullable = false)
    private Vendor fkVendor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_status_id", nullable = false)
    private PaymentStatus fkStatus;

    /*
     * =========================================================
     * PAYOUT PERIOD
     * =========================================================
     */
    @Column(name = "period_start_date", nullable = false)
    private LocalDate periodStartDate;

    @Column(name = "period_end_date", nullable = false)
    private LocalDate periodEndDate;

    /*
     * =========================================================
     * AMOUNTS
     * =========================================================
     */
    @Column(name = "total_amount_in_paise", nullable = false)
    private Long totalAmountInPaise;

    @Column(name = "platform_commission_in_paise", nullable = false)
    private Long platformCommissionInPaise;

    @Column(name = "net_payout_amount_in_paise", nullable = false)
    private Long netPayoutAmountInPaise;

    /*
     * =========================================================
     * PAYOUT DETAILS
     * =========================================================
     */
    @Column(name = "scheduled_payout_date", nullable = false)
    private LocalDate scheduledPayoutDate;

    @Column(name = "actual_payout_at_utc")
    private LocalDateTime actualPayoutAtUtc;

    @Column(name = "payout_method", length = 50)
    private String payoutMethod;

    @Column(name = "payout_reference_id", length = 255)
    private String payoutReferenceId;

    @Lob
    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

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