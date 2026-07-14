package com.productadda.controller.admin;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.payment.PaymentAnalyticsResponseDto;

import com.productadda.service.payment.PaymentAnalyticsServiceDashboard;

import lombok.RequiredArgsConstructor;

/*
 * ================================================================
 * AnalyticsController
 * Admin-only.
 * ================================================================
 */
@RestController
@RequiredArgsConstructor
public class AdminAnalyticsController {

        private final PaymentAnalyticsServiceDashboard paymentAnalyticsServiceDashboard;

        @GetMapping("/api/admin/analytics/payments")
        @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
        public ResponseEntity<ApiSuccessResponseDto<PaymentAnalyticsResponseDto>> getPaymentAnalytics(
                        @RequestParam(required = false) String startDate,
                        @RequestParam(required = false) String endDate,
                        @RequestParam(required = false) String paymentGateway,
                        @RequestParam(required = false) String paymentMethod) {

                PaymentAnalyticsResponseDto response = paymentAnalyticsServiceDashboard
                                .getPaymentAnalytics(startDate, endDate, paymentGateway, paymentMethod);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(ApiSuccessResponseDto.<PaymentAnalyticsResponseDto>builder()
                                                .success(true)
                                                .message("Payment analytics retrieved successfully")
                                                .data(response)
                                                .build());
        }
}
