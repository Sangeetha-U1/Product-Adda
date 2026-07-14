package com.productadda.controller.payment;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.payment.RefundFullRequestDto;
import com.productadda.dto.payment.RefundPartialRequestDto;
import com.productadda.dto.payment.RefundResponseDto;

import com.productadda.service.payment.RefundServiceFullRefund;
import com.productadda.service.payment.RefundServiceGetStatus;
import com.productadda.service.payment.RefundServicePartialRefund;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/*
 * ================================================================
 * RefundController
 * POST endpoints are Admin-only (confirmed: no CUSTOMER_SUPPORT
 * role exists). GET is open to CUSTOMER/ADMIN/SUPER_ADMIN, with
 * ownership narrowing done in RefundServiceGetStatus.
 * ================================================================
 */
@RestController
@RequestMapping("/api/refunds")
@RequiredArgsConstructor
public class RefundController {

        private final RefundServiceFullRefund refundServiceFullRefund;
        private final RefundServicePartialRefund refundServicePartialRefund;
        private final RefundServiceGetStatus refundServiceGetStatus;

        @PostMapping("/full")
        @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
        public ResponseEntity<ApiSuccessResponseDto<RefundResponseDto>> initiateFullRefund(
                        @Valid @RequestBody RefundFullRequestDto request) {

                RefundResponseDto response = refundServiceFullRefund.initiateFullRefund(request);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(
                                                ApiSuccessResponseDto.<RefundResponseDto>builder()
                                                                .success(true)
                                                                .message("Full refund initiated successfully")
                                                                .data(response)
                                                                .build());
        }

        @PostMapping("/partial")
        @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
        public ResponseEntity<ApiSuccessResponseDto<RefundResponseDto>> initiatePartialRefund(
                        @Valid @RequestBody RefundPartialRequestDto request) {

                RefundResponseDto response = refundServicePartialRefund.initiatePartialRefund(request);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(
                                                ApiSuccessResponseDto.<RefundResponseDto>builder()
                                                                .success(true)
                                                                .message("Partial refund initiated successfully")
                                                                .data(response)
                                                                .build());
        }

        @GetMapping("/{refundId}")
        @PreAuthorize("hasAnyAuthority('CUSTOMER', 'ADMIN', 'SUPER_ADMIN')")
        public ResponseEntity<ApiSuccessResponseDto<RefundResponseDto>> getRefundStatus(
                        @PathVariable UUID refundId) {

                RefundResponseDto response = refundServiceGetStatus.getRefundStatus(refundId);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(
                                                ApiSuccessResponseDto.<RefundResponseDto>builder()
                                                                .success(true)
                                                                .message("Refund status retrieved successfully")
                                                                .data(response)
                                                                .build());
        }
}
