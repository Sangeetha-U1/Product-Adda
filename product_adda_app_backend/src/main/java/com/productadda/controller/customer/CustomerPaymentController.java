package com.productadda.controller.customer;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.payment.PaginatedPaymentHistoryResponseDto;
import com.productadda.service.customer.CustomerGetCustomerPaymentsService;

import lombok.RequiredArgsConstructor;

/*
 * ================================================================
 * CustomerPaymentController
 * API 6/15 - GET /api/customers/payments
 * Mirrors CustomerOrderController's style exactly (no class-level
 * @RequestMapping, full path per method).
 * ================================================================
 */
@RestController
@RequiredArgsConstructor
public class CustomerPaymentController {

        private final CustomerGetCustomerPaymentsService customerPaymentRetrievalService;

        @GetMapping("/api/customers/payments")
        @PreAuthorize("hasAuthority('CUSTOMER')")
        public ResponseEntity<ApiSuccessResponseDto<PaginatedPaymentHistoryResponseDto>> getCustomerPayments(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        @RequestParam(required = false) String status,
                        @RequestParam(required = false) String startDate,
                        @RequestParam(required = false) String endDate,
                        @RequestParam(required = false) String paymentGateway) {

                PaginatedPaymentHistoryResponseDto response = customerPaymentRetrievalService
                                .getCustomerPayments(page, size, status, startDate, endDate, paymentGateway);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(ApiSuccessResponseDto.<PaginatedPaymentHistoryResponseDto>builder()
                                                .success(true)
                                                .message("Payment history retrieved successfully")
                                                .data(response)
                                                .build());
        }
}
