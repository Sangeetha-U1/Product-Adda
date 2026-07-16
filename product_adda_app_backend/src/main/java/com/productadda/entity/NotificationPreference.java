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
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "notification_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreference {

    /*
     * =========================================================
     * PRIMARY KEY
     * =========================================================
     */
    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "pk_preference_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID pkPreferenceId;

    /*
     * =========================================================
     * RELATIONSHIPS
     * =========================================================
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_recipient_type_id", nullable = false)
    private RecipientType fkRecipientType;

    // ==========================================
    // POLYMORPHIC RECIPIENT REFERENCE
    // Description: Points to users.pk_user_id, vendors.pk_vendor_id,
    // or delivery_partners.pk_delivery_partner_id depending on
    // fkRecipientType. Intentionally NOT a DB-level FK -- a single
    // column cannot foreign-key to three different tables. Enforcement
    // that recipient_id actually exists in the right table is a
    // service-layer responsibility (RecipientTypeAndIdResolverService),
    // not a DB constraint.
    // ==========================================
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "recipient_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID recipientId;

    /*
     * =========================================================
     * CHANNEL TOGGLES
     * =========================================================
     */
    @Column(name = "notifications_enabled", nullable = false)
    @Builder.Default
    private Boolean notificationsEnabled = true;

    @Column(name = "email_enabled", nullable = false)
    @Builder.Default
    private Boolean emailEnabled = true;

    @Column(name = "sms_enabled", nullable = false)
    @Builder.Default
    private Boolean smsEnabled = false;

    @Column(name = "push_enabled", nullable = false)
    @Builder.Default
    private Boolean pushEnabled = false;

    @Column(name = "in_app_enabled", nullable = false)
    @Builder.Default
    private Boolean inAppEnabled = true;

    /*
     * =========================================================
     * QUIET HOURS & OPT-OUTS
     * =========================================================
     */
    @Column(name = "quiet_hours_start", nullable = false)
    private LocalTime quietHoursStart;

    @Column(name = "quiet_hours_end", nullable = false)
    private LocalTime quietHoursEnd;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "event_opt_outs")
    private List<String> eventOptOuts;

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

    @Column(name = "updated_at_utc", nullable = false, insertable = false)
    private LocalDateTime updatedAtUtc;
}
