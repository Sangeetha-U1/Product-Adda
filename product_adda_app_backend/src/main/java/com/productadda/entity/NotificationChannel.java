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
@Table(name = "notification_channels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationChannel {

    /*
     * =========================================================
     * PRIMARY KEY
     * =========================================================
     */
    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "pk_channel_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID pkChannelId;

    /*
     * =========================================================
     * CHANNEL INFO
     * =========================================================
     */
    @Column(name = "channel_name", nullable = false, unique = true, length = 50)
    private String channelName;

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
