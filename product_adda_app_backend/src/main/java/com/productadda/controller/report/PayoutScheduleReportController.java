package com.productadda.controller.report;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.report.PayoutScheduleReportRequestDto;
import com.productadda.dto.report.ReportJobStatusDto;
import com.productadda.service.report.PayoutScheduleReportInitiationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class PayoutScheduleReportController {

    private final PayoutScheduleReportInitiationService payoutScheduleReportInitiationService;

    // ==========================================
    // INITIATE PAYOUT SCHEDULE REPORT
    // Description: Creates an async report generation job (PENDING status)
    // for a vendor payout schedule report. Admin or vendor — admins view
    // all vendors, vendors are hard-scoped to their own payout schedule.
    // Clean Layering: Offloaded to PayoutScheduleReportInitiationService.
    // ==========================================
    @PostMapping("/api/reports/payout-schedule")
    public ResponseEntity<ApiSuccessResponseDto<ReportJobStatusDto>> initiatePayoutScheduleReport(
            @Valid @RequestBody PayoutScheduleReportRequestDto request) {

        ReportJobStatusDto response = payoutScheduleReportInitiationService
                .initiatePayoutScheduleReport(request);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiSuccessResponseDto.<ReportJobStatusDto>builder()
                .success(true)
                .message("Report generation initiated")
                .data(response)
                .build());
    }
}
