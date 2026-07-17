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

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "notification_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationTemplate {

    /*
     * =========================================================
     * PRIMARY KEY
     * =========================================================
     */
    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "pk_template_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID pkTemplateId;

    /*
     * =========================================================
     * RELATIONSHIPS
     * =========================================================
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_type_id", nullable = false)
    private NotificationType fkType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_channel_id", nullable = false)
    private NotificationChannel fkChannel;

    // Nullable -- NULL means "applies to all recipient roles for this
    // (type, channel) pair". Day 3 baseline seeds only role-agnostic
    // (NULL) rows; role-specific rows are a future-day possibility the
    // schema already supports.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_recipient_role_id")
    private RecipientRole fkRecipientRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_created_by_user_id")
    private User fkCreatedByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_updated_by_user_id")
    private User fkUpdatedByUser;

    /*
     * =========================================================
     * TEMPLATE CONTENT
     * =========================================================
     */
    @Column(name = "subject", nullable = false, length = 500)
    private String subject;

    @Lob
    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;

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
