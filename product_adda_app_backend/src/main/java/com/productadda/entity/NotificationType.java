package com.productadda.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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
@Table(name = "notification_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationType {

    /*
     * =========================================================
     * PRIMARY KEY
     * =========================================================
     */
    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "pk_notification_type_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID pkNotificationTypeId;

    /*
     * =========================================================
     * TYPE INFO
     * =========================================================
     */
    @Column(name = "notification_type_name", nullable = false, unique = true, length = 100)
    private String notificationTypeName;

    @Column(name = "description", length = 255)
    private String description;

    /*
     * =========================================================
     * STATUS
     * =========================================================
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

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
