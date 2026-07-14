package com.productadda.controller.payment;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.payment.PaymentInitiateRequestDto;
import com.productadda.dto.payment.PaymentInitiateResponseDto;
import com.productadda.dto.payment.PaymentPollVerifyRequestDto;
import com.productadda.dto.payment.PaymentPollVerifyResponseDto;
import com.productadda.dto.payment.PaymentStatusResponseDto;

import com.productadda.service.payment.PaymentServiceGetPaymentStatus;
import com.productadda.service.payment.PaymentServicePaymentInitiate;
import com.productadda.service.payment.PaymentServicePollVerify;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/*
 * ================================================================
 * NEW CONTROLLER: PaymentInitiationController
 * Endpoints under /api/payments/** (plural).
 * REVISION NOTE: extended with the verify and getStatus
 * endpoints below /initiate.
 *
 * NAMING NOTE: The existing PaymentController already owns the
 * singular /api/payment/** namespace (Razorpay Payment Links flow:
 * create, verify, health). This is a deliberately distinct class
 * name to avoid a file collision at the same path.
 * ================================================================
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentInitiationController {

        private final PaymentServicePaymentInitiate paymentServicePaymentInitiate;
        private final PaymentServicePollVerify paymentServicePollVerify;
        private final PaymentServiceGetPaymentStatus paymentServiceGetPaymentStatus;

        @PostMapping("/initiate")
        @PreAuthorize("hasAuthority('CUSTOMER')")
        public ResponseEntity<ApiSuccessResponseDto<PaymentInitiateResponseDto>> initiatePayment(
                        @Valid @RequestBody PaymentInitiateRequestDto request) {

                PaymentInitiateResponseDto response = paymentServicePaymentInitiate.initiatePayment(request);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(
                                                ApiSuccessResponseDto.<PaymentInitiateResponseDto>builder()
                                                                .success(true)
                                                                .message("Payment initiated successfully")
                                                                .data(response)
                                                                .build());
        }

        @PostMapping("/verify")
        @PreAuthorize("hasAuthority('CUSTOMER')")
        public ResponseEntity<ApiSuccessResponseDto<PaymentPollVerifyResponseDto>> verifyPayment(
                        @Valid @RequestBody PaymentPollVerifyRequestDto request) {

                PaymentPollVerifyResponseDto response = paymentServicePollVerify.pollVerifyPayment(request);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(
                                                ApiSuccessResponseDto.<PaymentPollVerifyResponseDto>builder()
                                                                .success(true)
                                                                .message("Payment verification completed")
                                                                .data(response)
                                                                .build());
        }

        @GetMapping("/{paymentId}")
        @PreAuthorize("hasAnyAuthority('CUSTOMER', 'VENDOR', 'ADMIN', 'SUPER_ADMIN')")
        public ResponseEntity<ApiSuccessResponseDto<PaymentStatusResponseDto>> getPaymentStatus(
                        @PathVariable UUID paymentId) {

                PaymentStatusResponseDto response = paymentServiceGetPaymentStatus.getPaymentStatus(paymentId);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(
                                                ApiSuccessResponseDto.<PaymentStatusResponseDto>builder()
                                                                .success(true)
                                                                .message("Payment status retrieved successfully")
                                                                .data(response)
                                                                .build());
        }
}

