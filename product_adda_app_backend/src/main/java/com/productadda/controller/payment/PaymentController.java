package com.productadda.controller.payment;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
import com.productadda.service.payment.PaymentServiceRetrievePaymentDetails;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Validated
public class PaymentController {

        private final PaymentServicePaymentGatewayHealth paymentServicePaymentGatewayHealth;
        private final PaymentServicePaymentCreate paymentServicePaymentCreate;
        private final PaymentServicePaymentVerify paymentServicePaymentVerify;
        private final PaymentServiceRetrievePaymentDetails paymentServiceRetrievePaymentDetails;

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

        @GetMapping("/{orderId}")
        public ResponseEntity<ApiSuccessResponseDto<PaymentCreateResponseDto>> retrievePaymentDetails(
                        @PathVariable @NotNull UUID orderId) {

                PaymentCreateResponseDto paymentDetails = paymentServiceRetrievePaymentDetails
                                .retrievePaymentDetails(orderId);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(
                                                ApiSuccessResponseDto.<PaymentCreateResponseDto>builder()
                                                                .success(true)
                                                                .message("Payment details retrieved")
                                                                .data(paymentDetails)
                                                                .build());
        }
}