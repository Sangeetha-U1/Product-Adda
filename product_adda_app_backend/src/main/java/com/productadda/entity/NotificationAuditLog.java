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
import java.util.Map;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "notification_audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationAuditLog {

    /*
     * =========================================================
     * PRIMARY KEY
     * =========================================================
     */
    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "pk_audit_log_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID pkAuditLogId;

    /*
     * =========================================================
     * RELATIONSHIPS
     * =========================================================
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_admin_user_id")
    private User fkAdminUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_audit_action_type_id", nullable = false)
    private AuditActionType fkAuditActionType;

    /*
     * =========================================================
     * TARGET & DIFF
     * =========================================================
     */
    @Column(name = "target_table", nullable = false, length = 100)
    private String targetTable;

    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "target_id", columnDefinition = "BINARY(16)")
    private UUID targetId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "old_value")
    private Map<String, Object> oldValue;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_value")
    private Map<String, Object> newValue;

    /*
     * =========================================================
     * AUDIT
     * =========================================================
     */
    @Column(name = "created_at_utc", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAtUtc;
}
