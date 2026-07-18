package com.productadda.dto.report;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportRequestDto {

    @NotNull(message = "date_from is required")
    private LocalDate dateFrom;

    @NotNull(message = "date_to is required")
    private LocalDate dateTo;

    @NotBlank(message = "format is required")
    private String format;

    // Admin-only scoping filter. Ignored by vendor/finance initiation services.
    private List<String> vendorIds;

    // Admin-only scoping filter. Ignored by vendor/finance initiation services.
    private List<String> categoryIds;

    // Finance-reconciliation-only. Must be SUMMARY or DETAILED when present.
    private String reportDepth;
}
