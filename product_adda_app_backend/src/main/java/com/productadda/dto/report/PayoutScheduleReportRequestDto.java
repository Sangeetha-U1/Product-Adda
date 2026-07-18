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
 * PAYOUT SCHEDULE REPORT REQUEST
 * Description: Dedicated request shape for the vendor payout schedule
 * report. statusFilter is optional and intentionally not validated against
 * a DB lookup table at this layer — per the Day 4 scope decision, that
 * validation is deferred to the future ReportGenerationWorker, matching
 * the same deferred-validation approach used for Day 3's vendorIds and
 * categoryIds filters.
 * =========================================================
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayoutScheduleReportRequestDto {

    @NotNull(message = "date_from is required")
    private LocalDate dateFrom;

    @NotNull(message = "date_to is required")
    private LocalDate dateTo;

    @NotBlank(message = "format is required")
    private String format;

    // Optional filter — expected values are SCHEDULED, PROCESSING,
    // COMPLETED, or FAILED, but not enforced here (zero-enum rule deferred,
    // not skipped; see the TODO in PayoutScheduleReportInitiationService).
    private String statusFilter;
}
