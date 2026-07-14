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
import com.productadda.dto.payment.PaginatedPaymentResponseDto;
import com.productadda.dto.payment.PaymentDetailResponseDto;

import com.productadda.service.payment.PaymentServicePaymentCreate;
import com.productadda.service.payment.PaymentServicePaymentGatewayHealth;
import com.productadda.service.payment.PaymentServicePaymentVerify;
import com.productadda.service.payment.PaymentServiceGetPaymentByOrderId;
import com.productadda.service.payment.PaymentsGetAllPaymentsService;
import com.productadda.service.payment.PaymentsGetByPaymentService;

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
        private final PaymentServiceGetPaymentByOrderId paymentServiceGetPaymentByOrderId;
        private final PaymentsGetAllPaymentsService paymentsGetAllPaymentsService;
        private final PaymentsGetByPaymentService paymentsGetByPaymentService;

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

        @GetMapping("/order/{orderId}")
        public ResponseEntity<ApiSuccessResponseDto<PaymentCreateResponseDto>> retrievePaymentDetails(
                        @PathVariable @NotNull UUID orderId) {

                PaymentCreateResponseDto paymentDetails = paymentServiceGetPaymentByOrderId
                                .getPaymentByOrderId(orderId);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(
                                                ApiSuccessResponseDto.<PaymentCreateResponseDto>builder()
                                                                .success(true)
                                                                .message("Payment details retrieved")
                                                                .data(paymentDetails)
                                                                .build());
        }

        // ==========================================
        // GET ALL PAYMENTS FOR AUTHENTICATED USER
        // Description: Paginated, filterable, searchable payment history.
        // ==========================================
        @GetMapping
        public ResponseEntity<ApiSuccessResponseDto<PaginatedPaymentResponseDto>> getAllPayments(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int pageSize,
                        @RequestParam(defaultValue = "createdAtUtc") String sortBy,
                        @RequestParam(defaultValue = "DESC") String sortOrder,
                        @RequestParam(required = false) String status,
                        @RequestParam(required = false) String gatewayName,
                        @RequestParam(required = false) String startDate,
                        @RequestParam(required = false) String endDate,
                        @RequestParam(required = false) Long minAmountInPaise,
                        @RequestParam(required = false) Long maxAmountInPaise,
                        @RequestParam(required = false) String keyword) {

                PaginatedPaymentResponseDto response = paymentsGetAllPaymentsService.getAllPayments(
                                page, pageSize, sortBy, sortOrder, status, gatewayName,
                                startDate, endDate, minAmountInPaise, maxAmountInPaise, keyword);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(
                                                ApiSuccessResponseDto.<PaginatedPaymentResponseDto>builder()
                                                                .success(true)
                                                                .message("Payment history retrieved successfully")
                                                                .data(response)
                                                                .build());
        }

        // ==========================================
        // GET PAYMENT DETAILS BY PAYMENT ID
        // Description: Rich detail view for a single payment.
        // ==========================================
        @GetMapping("/{paymentId}")
        public ResponseEntity<ApiSuccessResponseDto<PaymentDetailResponseDto>> getPaymentDetails(
                        @PathVariable @NotNull UUID paymentId) {

                PaymentDetailResponseDto response = paymentsGetByPaymentService.getPaymentDetails(paymentId);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(
                                                ApiSuccessResponseDto.<PaymentDetailResponseDto>builder()
                                                                .success(true)
                                                                .message("Payment details retrieved successfully")
                                                                .data(response)
                                                                .build());
        }
}