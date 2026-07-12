package com.productadda.controller.payment;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.payment.PaymentInitiateRequestDto;
import com.productadda.dto.payment.PaymentInitiateResponseDto;

import com.productadda.service.payment.PaymentServicePaymentInitiate;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/*
 * ================================================================
 * PaymentInitiationController
 * Endpoints under /api/payments/** (plural).
 *
 * NAMING NOTE: The existing PaymentController already owns the
 * singular /api/payment/** namespace (Razorpay Payment Links flow:
 * create, verify, health). This is a deliberately distinct class
 * name to avoid a file collision at the same path. See
 * CHANGES_DAY1.md for the full explanation of why Week 7 introduces
 * a parallel controller rather than extending the legacy one.
 * ================================================================
 */
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentInitiationController {

        private final PaymentServicePaymentInitiate paymentServicePaymentInitiate;

        @PostMapping("/initiate")
        @PreAuthorize("hasAuthority('USER', 'CUSTOMER')")
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
}
