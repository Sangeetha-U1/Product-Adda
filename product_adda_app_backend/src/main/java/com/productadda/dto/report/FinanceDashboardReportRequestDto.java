package com.productadda.dto.report;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
 * =========================================================
 * FINANCE DASHBOARD REPORT REQUEST
 * Description: Dedicated request shape for the finance dashboard report,
 * kept deliberately separate from the shared ReportRequestDto. There is no
 * vendorIds field here by design — this endpoint is ADMIN-only with no
 * per-vendor breakdown, unlike the admin-unrestricted / vendor-hard-scoped
 * pattern used by Day 3's endpoints.
 * =========================================================
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinanceDashboardReportRequestDto {

    @NotNull(message = "date_from is required")
    private LocalDate dateFrom;

    @NotNull(message = "date_to is required")
    private LocalDate dateTo;

    @NotBlank(message = "format is required")
    private String format;
}
