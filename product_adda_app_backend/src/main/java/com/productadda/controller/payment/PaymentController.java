package com.productadda.controller.payment;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.payment.PaymentCreateRequestDto;
import com.productadda.dto.payment.PaymentCreateResponseDto;
import com.productadda.dto.payment.PaymentGatewayHealthResponseDto;
import com.productadda.dto.payment.PaymentVerifyRequestDto;
import com.productadda.dto.payment.PaymentVerifyResponseDto;
import com.productadda.service.payment.PaymentServicePaymentCreate;
import com.productadda.service.payment.PaymentServicePaymentGatewayHealth;
import com.productadda.service.payment.PaymentServicePaymentVerify;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

        private final PaymentServicePaymentGatewayHealth paymentServicePaymentGatewayHealth;
        private final PaymentServicePaymentCreate paymentServicePaymentCreate;
        private final PaymentServicePaymentVerify paymentServicePaymentVerify;

        @GetMapping("/health")
        public ResponseEntity<ApiSuccessResponseDto<PaymentGatewayHealthResponseDto>> health() {

                PaymentGatewayHealthResponseDto paymentGatewayHealth = paymentServicePaymentGatewayHealth
                                .getGatewayHealth();

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(
                                                ApiSuccessResponseDto.<PaymentGatewayHealthResponseDto>builder()
                                                                .success(true)
                                                                .message("Payment gateway health verified")
                                                                .data(paymentGatewayHealth)
                                                                .build());
        }

        @PostMapping("/create")
        public ResponseEntity<ApiSuccessResponseDto<PaymentCreateResponseDto>> paymentCreate(
                        @Valid @RequestBody PaymentCreateRequestDto requestDto) {

                PaymentCreateResponseDto paymentCreate = paymentServicePaymentCreate.paymentCreate(requestDto);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(
                                                ApiSuccessResponseDto.<PaymentCreateResponseDto>builder()
                                                                .success(true)
                                                                .message("Payment order created")
                                                                .data(paymentCreate)
                                                                .build());
        }

        @PostMapping("/verify")
        public ResponseEntity<ApiSuccessResponseDto<PaymentVerifyResponseDto>> paymentVerify(
                        @Valid @RequestBody PaymentVerifyRequestDto requestDto) {

                PaymentVerifyResponseDto paymentVerify = paymentServicePaymentVerify.paymentVerify(requestDto);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(
                                                ApiSuccessResponseDto.<PaymentVerifyResponseDto>builder()
                                                                .success(true)
                                                                .message("Payment verification completed")
                                                                .data(paymentVerify)
                                                                .build());
        }
}