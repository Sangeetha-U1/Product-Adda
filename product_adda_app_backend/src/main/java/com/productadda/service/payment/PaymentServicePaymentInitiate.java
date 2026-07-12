package com.productadda.service.payment;

import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.productadda.dto.payment.PaymentInitiateRequestDto;
import com.productadda.dto.payment.PaymentInitiateResponseDto;

import com.productadda.entity.Order;
import com.productadda.entity.Payment;
import com.productadda.entity.PaymentAuditLog;
import com.productadda.entity.PaymentGateway;
import com.productadda.entity.PaymentStatus;
import com.productadda.entity.User;

import com.productadda.exception.ApiException;

import com.productadda.repository.OrderRepository;
import com.productadda.repository.PaymentAuditLogRepository;
import com.productadda.repository.PaymentGatewayRepository;
import com.productadda.repository.PaymentRepository;
import com.productadda.repository.PaymentStatusRepository;
import com.productadda.repository.UserRepository;

import com.productadda.util.UuidUtil;

import lombok.RequiredArgsConstructor;

/*
 * ================================================================
 * PaymentServicePaymentInitiate
 * API 1/15 - POST /api/payments/initiate
 * Granular, single-responsibility: payment initiation only.
 * ================================================================
 */
@Service
@RequiredArgsConstructor
public class PaymentServicePaymentInitiate {

    private final PaymentRepository paymentRepository;
    private final PaymentAuditLogRepository paymentAuditLogRepository;
    private final OrderRepository orderRepository;
    private final PaymentStatusRepository paymentStatusRepository;
    private final PaymentGatewayRepository paymentGatewayRepository;
    private final UserRepository userRepository;

    private final RazorpayGatewayService razorpayGatewayService;

    private final UuidUtil uuidUtil;

    /*
     * ================================================================
     * INITIATE PAYMENT
     * ================================================================
     */
    @Transactional
    public PaymentInitiateResponseDto initiatePayment(PaymentInitiateRequestDto request) {
        
        ObjectMapper objectMapper = new ObjectMapper();

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        UUID orderId;
        
        try {
            orderId = UUID.fromString(request.getOrderId());
        } catch (IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "orderId is not a valid identifier");
        }

        UUID idempotencyKey;

        try {
            idempotencyKey = UUID.fromString(request.getIdempotencyKey());
        } catch (IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "idempotencyKey is not a valid identifier");
        }

        // ==========================================
        // 1.2 CONTEXT AUTHENTICATION
        // ==========================================

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentication missing or invalid");
        }

        String currentUsername = authentication.getName();

        User authenticatedCustomer = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Authenticated user no longer exists"));

        // ==========================================
        // 1.3 DATABASE LOOKUP VALIDATION
        // ==========================================

        // Idempotency check happens first: repeated requests with the same key
        // must short-circuit before touching the order or any gateway.
        Optional<Payment> existingPayment = paymentRepository.findByIdempotencyKey(idempotencyKey);
        if (existingPayment.isPresent()) {
            return buildResponseFromExistingPayment(existingPayment.get());
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));

        if (!order.getFkUser().getPkUserId().equals(authenticatedCustomer.getPkUserId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Order does not belong to authenticated customer");
        }

        if (!"CONFIRMED".equalsIgnoreCase(order.getFkStatus().getStatusName())) {
            throw new ApiException(HttpStatus.CONFLICT, "Order is not in CONFIRMED status");
        }

        PaymentGateway paymentGateway = paymentGatewayRepository
                .findByGatewayName(request.getPaymentGateway().toUpperCase())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Unsupported payment gateway"));

        // TODO: "PENDING" is assumed (matches the plan's described lifecycle
        // and is the only sensible pre-capture state), but only "SUCCESS" is
        // directly confirmed against real payment_statuses data so far.
        // Verify the exact status_name string before deploying.
        PaymentStatus pendingStatus = paymentStatusRepository.findByStatusName("PENDING")
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "PENDING status not configured"));

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */

        // Amount is derived server-side from the order, never trusted from the client.
        Long amountInPaise = Math.round(order.getTotalAmount().doubleValue() * 100);

        // Gateway limit per the execution plan: Razorpay max 10,00,00,000 paise.
        if ("RAZORPAY".equalsIgnoreCase(paymentGateway.getGatewayName()) && amountInPaise > 1000000000L) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Amount exceeds Razorpay gateway limit");
        }

        String gatewayOrderId;
        String checkoutRedirectUrl;
        String checkoutKey;

        if ("RAZORPAY".equalsIgnoreCase(paymentGateway.getGatewayName())) {
            RazorpayGatewayService.RazorpayOrderResult gatewayResult = razorpayGatewayService.createOrder(amountInPaise,
                    order.getPkOrderId());
            gatewayOrderId = gatewayResult.getGatewayOrderId();
            checkoutRedirectUrl = gatewayResult.getCheckoutRedirectUrl();
            checkoutKey = gatewayResult.getCheckoutKey();
        } else {
            // COD: immediate success, no external gateway call required
            gatewayOrderId = "COD-" + order.getPkOrderId();
            checkoutRedirectUrl = null;
            checkoutKey = null;
        }

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * ================================================================
         */
        Payment payment = Payment.builder()
                .pkPaymentId(uuidUtil.generateUuidV7())
                .fkOrder(order)
                .fkUser(authenticatedCustomer)
                .fkStatus(pendingStatus)
                .fkGateway(paymentGateway)
                .paymentMethod(request.getPaymentMethod())
                .gatewayTransactionId(gatewayOrderId)
                .gatewayOrderId(gatewayOrderId)
                .idempotencyKey(idempotencyKey)
                .amountInPaise(amountInPaise)
                .currency("INR")
                .isActive(true)
                .build();
        payment = paymentRepository.save(payment);

        ObjectNode auditDetailsJson = objectMapper.createObjectNode();

        auditDetailsJson
                .put("gatewayOrderId", gatewayOrderId)
                .put("method", request.getPaymentMethod());

        PaymentAuditLog auditLog = PaymentAuditLog.builder()
                .pkAuditLogId(uuidUtil.generateUuidV7())
                .fkPayment(payment)
                .action("payment_initiated")
                .fkActor(authenticatedCustomer)
                .actorRole("CUSTOMER")
                .oldStatus(null)
                .newStatus("PENDING")
                .details(auditDetailsJson)
                .build();
        paymentAuditLogRepository.save(auditLog);

        // TODO: Week 8 notification service hook - payment_initiated event

        /*
         * ================================================================
         * 4. RESPONSE MAPPING
         * ================================================================
         */
        return PaymentInitiateResponseDto.builder()
                .paymentId(payment.getPkPaymentId().toString())
                .orderId(order.getPkOrderId().toString())
                .status(pendingStatus.getStatusName())
                .amountInPaise(payment.getAmountInPaise())
                .currency(payment.getCurrency())
                .paymentGateway(paymentGateway.getGatewayName())
                .gatewayOrderId(gatewayOrderId)
                .checkoutRedirectUrl(checkoutRedirectUrl)
                .checkoutKey(checkoutKey)
                .build();
    }

    /*
     * Idempotency short-circuit path: reconstructs the response DTO from an
     * already-persisted Payment row without calling any gateway again.
     */
    private PaymentInitiateResponseDto buildResponseFromExistingPayment(Payment payment) {
        return PaymentInitiateResponseDto.builder()
                .paymentId(payment.getPkPaymentId().toString())
                .orderId(payment.getFkOrder().getPkOrderId().toString())
                .status(payment.getFkStatus().getStatusName())
                .amountInPaise(payment.getAmountInPaise())
                .currency(payment.getCurrency())
                .paymentGateway(payment.getFkGateway().getGatewayName())
                .gatewayOrderId(payment.getGatewayOrderId())
                .checkoutRedirectUrl(null)
                .checkoutKey(null)
                .build();
    }
}
