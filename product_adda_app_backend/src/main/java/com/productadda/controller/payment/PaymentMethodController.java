package com.productadda.controller.payment;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.payment.PaymentMethodListDto;
import com.productadda.dto.payment.PaymentMethodRequestDto;
import com.productadda.dto.payment.PaymentMethodResponseDto;

import com.productadda.service.payment.CustomerPaymentMethodRetrievalService;
import com.productadda.service.payment.PaymentMethodServiceSave;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/*
 * ================================================================
 * PaymentMethodController
 * Both endpoints are Customer-only, per the execution plan.
 * ================================================================
 */
@RestController
@RequiredArgsConstructor
public class PaymentMethodController {

        private final PaymentMethodServiceSave paymentMethodServiceSave;
        private final CustomerPaymentMethodRetrievalService customerPaymentMethodRetrievalService;

        @PostMapping("/api/payment-methods/save")
        @PreAuthorize("hasAuthority('CUSTOMER')")
        public ResponseEntity<ApiSuccessResponseDto<PaymentMethodResponseDto>> savePaymentMethod(
                        @Valid @RequestBody PaymentMethodRequestDto request) {

                PaymentMethodResponseDto response = paymentMethodServiceSave.savePaymentMethod(request);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(
                                                ApiSuccessResponseDto.<PaymentMethodResponseDto>builder()
                                                                .success(true)
                                                                .message("Payment method saved successfully")
                                                                .data(response)
                                                                .build());
        }

        @GetMapping("/api/customers/payment-methods")
        @PreAuthorize("hasAuthority('CUSTOMER')")
        public ResponseEntity<ApiSuccessResponseDto<List<PaymentMethodListDto>>> getSavedPaymentMethods(
                        @RequestParam(required = false) Boolean isActive) {

                List<PaymentMethodListDto> response = customerPaymentMethodRetrievalService
                                .getSavedPaymentMethods(isActive);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(
                                                ApiSuccessResponseDto.<List<PaymentMethodListDto>>builder()
                                                                .success(true)
                                                                .message("Payment methods retrieved successfully")
                                                                .data(response)
                                                                .build());
        }
}
