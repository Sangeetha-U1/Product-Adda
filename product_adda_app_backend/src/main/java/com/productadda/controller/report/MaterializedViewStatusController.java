package com.productadda.controller.report;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.report.MaterializedViewStatusResponseDto;
import com.productadda.service.report.MaterializedViewStatusRetrievalService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class MaterializedViewStatusController {

    private final MaterializedViewStatusRetrievalService materializedViewStatusRetrievalService;

    // ==========================================
    // GET MATERIALIZED VIEW / SNAPSHOT TABLE STATUS
    // Description: Read-only freshness check against the sales_aggregates
    // and inventory_aggregates snapshot tables. Any authenticated user may
    // call this endpoint — no role restriction.
    // Clean Layering: Offloaded to MaterializedViewStatusRetrievalService.
    // ==========================================
    @GetMapping("/api/reports/materialized-view-status")
    public ResponseEntity<ApiSuccessResponseDto<MaterializedViewStatusResponseDto>> getMaterializedViewStatus() {

        MaterializedViewStatusResponseDto response = materializedViewStatusRetrievalService
                .getMaterializedViewStatus();

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiSuccessResponseDto.<MaterializedViewStatusResponseDto>builder()
                        .success(true)
                        .message("Snapshot table status retrieved")
                        .data(response)
                        .build());
    }
}
