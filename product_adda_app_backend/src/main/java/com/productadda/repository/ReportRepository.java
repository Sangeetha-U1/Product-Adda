package com.productadda.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.productadda.entity.Report;
import com.productadda.entity.ReportJobStatus;
import com.productadda.entity.ReportType;
import com.productadda.entity.User;

public interface ReportRepository extends JpaRepository<Report, UUID> {

        // Cursor-paginated, newest-first report history. All filters are
        // optional (pass null to skip); scopedUser null means unrestricted
        // (admin) access, non-null enforces tenant isolation to that user's
        // own report jobs.
        @Query("SELECT r FROM Report r WHERE "
                        + "(:scopedUser IS NULL OR r.fkUser = :scopedUser) "
                        + "AND (:jobStatus IS NULL OR r.fkStatus = :jobStatus) "
                        + "AND (:reportType IS NULL OR r.fkReportType = :reportType) "
                        + "AND (:cursorCreatedAtUtc IS NULL OR r.createdAtUtc < :cursorCreatedAtUtc "
                        + "     OR (r.createdAtUtc = :cursorCreatedAtUtc AND r.pkReportId < :cursorReportId)) "
                        + "ORDER BY r.createdAtUtc DESC, r.pkReportId DESC")
        List<Report> findHistoryPage(
                        @Param("scopedUser") User scopedUser,
                        @Param("jobStatus") ReportJobStatus jobStatus,
                        @Param("reportType") ReportType reportType,
                        @Param("cursorCreatedAtUtc") LocalDateTime cursorCreatedAtUtc,
                        @Param("cursorReportId") UUID cursorReportId,
                        Pageable pageable);

        // Statistics: all reports created today (start-of-day UTC cutoff).
        List<Report> findByCreatedAtUtcGreaterThanEqual(LocalDateTime since);

        // Statistics: current-hour job count, used for load% against
        // MAX_JOBS_PER_HOUR.
        long countByCreatedAtUtcGreaterThanEqual(LocalDateTime since);

        // System health: cumulative pending-job backlog, by status name
        // string rather than a pre-resolved ReportJobStatus lookup, so this
        // never fails if a status row is momentarily absent.
        @Query("SELECT COUNT(r) FROM Report r WHERE r.fkStatus.statusName = :statusName")
        long countByStatusName(@Param("statusName") String statusName);

        // System health: failed-job count within a trailing time window.
        @Query("SELECT COUNT(r) FROM Report r WHERE r.fkStatus.statusName = :statusName "
                        + "AND r.createdAtUtc >= :since")
        long countByStatusNameSince(@Param("statusName") String statusName, @Param("since") LocalDateTime since);
}