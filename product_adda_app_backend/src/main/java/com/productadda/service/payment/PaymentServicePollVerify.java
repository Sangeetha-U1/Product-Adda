package com.productadda.service.payment;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.payment.PaymentPollVerifyRequestDto;
import com.productadda.dto.payment.PaymentPollVerifyResponseDto;

import com.productadda.entity.Order;
import com.productadda.entity.Payment;
import com.productadda.entity.PaymentAuditLog;
import com.productadda.entity.PaymentStatus;
import com.productadda.entity.User;

import com.productadda.exception.ApiException;

import com.productadda.repository.OrderRepository;
import com.productadda.repository.PaymentAuditLogRepository;
import com.productadda.repository.PaymentRepository;
import com.productadda.repository.PaymentStatusRepository;
import com.productadda.repository.UserRepository;

import com.productadda.util.UuidUtil;

import lombok.RequiredArgsConstructor;

/*
 * ================================================================
 * PaymentServicePollVerify
 * API 4/15 - POST /api/payments/verify
 * Gateway-agnostic polling/verification endpoint. Frontend calls
 * this after redirect back from checkout, to cover the case where
 * the Razorpay webhook has not yet arrived (or never arrives).
 *
 * NAMING NOTE: A PaymentServicePaymentVerify already exists for the
 * legacy Razorpay Payment Links flow. This is a deliberately
 * distinct class
 * ================================================================
 */
@Service
@RequiredArgsConstructor
public class PaymentServicePollVerify {

    private final PaymentRepository paymentRepository;
    private final PaymentAuditLogRepository paymentAuditLogRepository;
    private final OrderRepository orderRepository;
    private final PaymentStatusRepository paymentStatusRepository;
    private final UserRepository userRepository;

    private final RazorpayGatewayFetchOrderStatusService razorpayGatewayFetchOrderStatusService;

    private final UuidUtil uuidUtil;

    /*
     * ================================================================
     * POLL / VERIFY PAYMENT STATUS
     * ================================================================
     */
    @Transactional
    public PaymentPollVerifyResponseDto pollVerifyPayment(PaymentPollVerifyRequestDto request) {

        Map<String, Object> metaData = new HashMap<>();

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        UUID paymentId;
        try {
            paymentId = UUID.fromString(request.getPaymentId());
        } catch (IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "paymentId is not a valid identifier");
        }

        // ==========================================
        // 1.2 CONTEXT AUTHENTICATION
        // ==========================================
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentication missing or invalid");
        }

        String email;
        if (authentication.getPrincipal() instanceof UserDetails userDetails) {
            email = userDetails.getUsername();
        } else {
            email = authentication.getName();
        }

        User authenticatedCustomer = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Authenticated user no longer exists"));

        // ==========================================
        // 1.3 DATABASE LOOKUP VALIDATION
        // ==========================================
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Payment not found"));

        if (!payment.getFkUser().getPkUserId().equals(authenticatedCustomer.getPkUserId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Payment does not belong to authenticated customer");
        }

        // Optional cross-check per the execution plan
        if (request.getOrderId() != null && !request.getOrderId().trim().isEmpty()) {
            UUID crossCheckOrderId;
            try {
                crossCheckOrderId = UUID.fromString(request.getOrderId());
            } catch (IllegalArgumentException exception) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "orderId is not a valid identifier");
            }
            if (!payment.getFkOrder().getPkOrderId().equals(crossCheckOrderId)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "orderId does not match the payment's linked order");
            }
        }

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        String currentStatusName = payment.getFkStatus().getStatusName();

        // CONFIRMED value: legacy PaymentServicePaymentVerify checks "SUCCESS"
        // directly.
        if ("SUCCESS".equalsIgnoreCase(currentStatusName)) {
            return PaymentPollVerifyResponseDto.builder()
                    .success(true)
                    .paymentId(payment.getPkPaymentId().toString())
                    .orderId(payment.getFkOrder().getPkOrderId().toString())
                    .status(currentStatusName)
                    .failureReason(null)
                    .message("Payment already verified")
                    .build();
        }

        // TODO: "FAILED" is not directly confirmed against real payment_statuses
        // data - see the same TODO in PaymentServiceRazorpayWebhookProcess.
        if ("FAILED".equalsIgnoreCase(currentStatusName)) {
            return PaymentPollVerifyResponseDto.builder()
                    .success(false)
                    .paymentId(payment.getPkPaymentId().toString())
                    .orderId(payment.getFkOrder().getPkOrderId().toString())
                    .status(currentStatusName)
                    .failureReason(payment.getFailureReason())
                    .message("Payment failed")
                    .build();
        }

        // Status is still PENDING: fall back to querying the gateway directly,
        // covering the case where the webhook never arrived.
        // NOTE: Razorpay's Orders API returns order-level status values
        // (created/attempted/paid), NOT the payment-entity-level values
        // (created/authorized/captured/failed) used in the webhook flow.
        // "paid" is the order-level success indicator here.
        String gatewayOrderStatus = razorpayGatewayFetchOrderStatusService
                .fetchOrderStatus(payment.getGatewayOrderId());
        LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);

        if ("paid".equalsIgnoreCase(gatewayOrderStatus)) {
            PaymentStatus successStatus = paymentStatusRepository.findByStatusName("SUCCESS")
                    .orElseThrow(
                            () -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "SUCCESS status not configured"));

            payment.setFkStatus(successStatus);
            payment.setCapturedAtUtc(nowUtc);
            payment.setWebhookReceivedAtUtc(nowUtc);

            Order order = payment.getFkOrder();
            // TODO: hook to inventory service to decrement stock / mark
            // order_items as 'processing' for every line item on this order.
            order.setPaymentConfirmedAtUtc(nowUtc);

            /*
             * ================================================================
             * 3. DB SAVING SECTION
             * ================================================================
             */
            payment = paymentRepository.save(payment);

            orderRepository.save(order);

            Map<String, Object> details = new HashMap<>();

            details.put("status", gatewayOrderStatus);

            metaData.put("gatewayOrder", details);

            PaymentAuditLog auditLog = PaymentAuditLog.builder()
                    .pkAuditLogId(uuidUtil.generateUuidV7())
                    .fkPayment(payment)
                    .action("manual_verification_recovered")
                    .fkActor(authenticatedCustomer)
                    .actorRole("CUSTOMER")
                    .oldStatus("PENDING")
                    .newStatus("SUCCESS")
                    .metadata(metaData)
                    .build();
                    
            paymentAuditLogRepository.save(auditLog);

            // TODO: trigger incident alert for missing webhook (per plan step 6)

            /*
             * ================================================================
             * 4. RESPONSE MAPPING
             * ================================================================
             */
            return PaymentPollVerifyResponseDto.builder()
                    .success(true)
                    .paymentId(payment.getPkPaymentId().toString())
                    .orderId(order.getPkOrderId().toString())
                    .status("SUCCESS")
                    .failureReason(null)
                    .message("Payment verified from gateway")
                    .build();
        }

        // Gateway still shows a non-final state: leave Payment as PENDING and
        // let the frontend keep polling. No DB mutation in this branch.
        return PaymentPollVerifyResponseDto.builder()
                .success(false)
                .paymentId(payment.getPkPaymentId().toString())
                .orderId(payment.getFkOrder().getPkOrderId().toString())
                .status(currentStatusName)
                .failureReason(null)
                .message("Payment still pending confirmation from gateway")
                .build();
    }
}
