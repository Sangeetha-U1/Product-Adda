package com.productadda.controller.payment;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.payment.ReconciliationResultDto;
import com.productadda.dto.payment.ReconciliationTriggerRequestDto;

import com.productadda.service.payment.ReconciliationServiceDailyRun;

import lombok.RequiredArgsConstructor;

/*
 * ================================================================
 * ReconciliationController
 * Admin-only manual trigger. The 2 AM UTC automatic run is
 * ScheduledReconciliationTask, calling the same underlying service.
 * ================================================================
 */
@RestController
@RequestMapping("/api/reconciliation")
@RequiredArgsConstructor
public class ReconciliationController {

        private final ReconciliationServiceDailyRun reconciliationServiceDailyRun;

        @PostMapping("/daily")
        @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
        public ResponseEntity<ApiSuccessResponseDto<ReconciliationResultDto>> runDailyReconciliation(
                        @RequestBody(required = false) ReconciliationTriggerRequestDto request) {

                ReconciliationResultDto response = reconciliationServiceDailyRun.runManualReconciliation(request);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(
                                                ApiSuccessResponseDto.<ReconciliationResultDto>builder()
                                                                .success(true)
                                                                .message("Reconciliation completed")
                                                                .data(response)
                                                                .build());
        }
}
