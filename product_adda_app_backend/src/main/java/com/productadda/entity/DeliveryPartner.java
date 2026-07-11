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
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "delivery_partners")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryPartner {

    /*
     * =========================================================
     * PRIMARY KEY
     * =========================================================
     */
    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "pk_delivery_partner_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID pkDeliveryPartnerId;

    /*
     * =========================================================
     * FOREIGN KEYS / RELATIONSHIPS
     * =========================================================
     */
    // TODO: Add unique true and in db too.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_user_id", nullable = false)
    private User fkUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_status_id", nullable = false)
    private DeliveryPartnerStatus fkStatus;

    /*
     * =========================================================
     * DELIVERY PARTNER INFO
     * =========================================================
     */
    @Column(name = "partner_name", nullable = false, length = 255)
    private String partnerName;

    @Column(name = "current_location", length = 500)
    private String currentLocation;

    /*
     * =========================================================
     * DELIVERY METRICS
     * =========================================================
     */
    @Column(name = "max_concurrent_deliveries", nullable = false)
    private Integer maxConcurrentDeliveries;

    @Column(name = "active_deliveries", nullable = false)
    private Integer activeDeliveries;

    @Column(name = "rating", nullable = false, precision = 3, scale = 2)
    private BigDecimal rating;

    @Column(name = "total_deliveries", nullable = false)
    private Integer totalDeliveries;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

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