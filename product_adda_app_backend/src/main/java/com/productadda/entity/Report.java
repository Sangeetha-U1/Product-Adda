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

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    /*
     * =========================================================
     * PRIMARY KEY
     * =========================================================
     */
    @Id
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "pk_report_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID pkReportId;

    /*
     * =========================================================
     * FOREIGN KEYS / RELATIONSHIPS
     * =========================================================
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_user_id", nullable = false)
    private User fkUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_report_type_id", nullable = false)
    private ReportType fkReportType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_vendor_id")
    private Vendor fkVendor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_status_id", nullable = false)
    private ReportJobStatus fkStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_export_format_id", nullable = false)
    private ReportExportFormat fkExportFormat;

    /*
     * =========================================================
     * REPORT INFO
     * =========================================================
     */
    @Column(name = "report_name", nullable = false, length = 255)
    private String reportName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "filters")
    private Map<String, Object> filters;

    @Column(name = "file_url", length = 1000)
    private String fileUrl;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private Integer retryCount = 0;

    /*
     * =========================================================
     * PROCESSING STATUS TIMELINE
     * =========================================================
     */
    @Column(name = "status_started_at_utc")
    private LocalDateTime statusStartedAtUtc;

    @Column(name = "completed_at_utc")
    private LocalDateTime completedAtUtc;

    @Column(name = "generated_at_utc", nullable = false)
    private LocalDateTime generatedAtUtc;

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

    @Column(name = "updated_at_utc", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAtUtc;
}