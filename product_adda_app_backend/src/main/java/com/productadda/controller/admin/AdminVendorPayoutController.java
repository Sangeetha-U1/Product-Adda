package com.productadda.controller.admin;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.payment.VendorPayoutResponseDto;
import com.productadda.dto.payment.VendorPayoutScheduleRequestDto;

import com.productadda.service.payment.VendorPayoutServiceSchedule;

import lombok.RequiredArgsConstructor;

/*
 * ================================================================
 * VendorPayoutController
 * Admin-only.
 * ================================================================
 */
@RestController
@RequestMapping("/api/vendor-payouts")
@RequiredArgsConstructor
public class AdminVendorPayoutController {

        private final VendorPayoutServiceSchedule vendorPayoutServiceSchedule;

        @PostMapping("/schedule")
        @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
        public ResponseEntity<ApiSuccessResponseDto<List<VendorPayoutResponseDto>>> scheduleVendorPayouts(
                        @RequestBody(required = false) VendorPayoutScheduleRequestDto request) {

                List<VendorPayoutResponseDto> response = vendorPayoutServiceSchedule.scheduleVendorPayouts(request);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(
                                                ApiSuccessResponseDto.<List<VendorPayoutResponseDto>>builder()
                                                                .success(true)
                                                                .message(response.size() + " vendor payout(s) scheduled")
                                                                .data(response)
                                                                .build());
        }
}
