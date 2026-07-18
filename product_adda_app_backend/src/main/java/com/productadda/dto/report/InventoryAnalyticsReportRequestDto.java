package com.productadda.dto.report;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

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
public class InventoryAnalyticsReportRequestDto {

    @NotBlank(message = "format is required")
    private String format;

    // Admin-only scoping filter. Hard-overridden server-side for vendor callers.
    private List<String> vendorIds;

    // Optional filter, no DB-existence validation at initiation time.
    private List<String> categoryIds;
}
