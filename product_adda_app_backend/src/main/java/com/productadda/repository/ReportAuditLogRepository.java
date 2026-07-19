package com.productadda.repository;

import java.util.UUID;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.AuditActionType;
import com.productadda.entity.User;
import com.productadda.entity.ReportAuditLog;

public interface ReportAuditLogRepository extends JpaRepository<ReportAuditLog, UUID> {
    // Cursor-paginated, newest-first audit trail. All filters are
    // optional (pass null to skip); scopedUser null means unrestricted
    // (admin) access, non-null enforces tenant isolation to that user's
    // own audit rows.
    @Query("SELECT a FROM ReportAuditLog a WHERE "
            + "(:scopedUser IS NULL OR a.fkUser = :scopedUser) "
            + "AND (:actionType IS NULL OR a.fkAuditActionType = :actionType) "
            + "AND (:dateFrom IS NULL OR a.createdAtUtc >= :dateFrom) "
            + "AND (:dateTo IS NULL OR a.createdAtUtc <= :dateTo) "
            + "AND (:cursorCreatedAtUtc IS NULL OR a.createdAtUtc < :cursorCreatedAtUtc "
            + "     OR (a.createdAtUtc = :cursorCreatedAtUtc AND a.pkReportAuditLogId < :cursorAuditLogId)) "
            + "ORDER BY a.createdAtUtc DESC, a.pkReportAuditLogId DESC")
    List<ReportAuditLog> findAuditLogHistoryPage(
            @Param("scopedUser") User scopedUser,
            @Param("actionType") AuditActionType actionType,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            @Param("cursorCreatedAtUtc") LocalDateTime cursorCreatedAtUtc,
            @Param("cursorAuditLogId") UUID cursorAuditLogId,
            Pageable pageable);
}
