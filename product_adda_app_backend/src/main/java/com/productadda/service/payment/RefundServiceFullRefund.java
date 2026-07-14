package com.productadda.service.payment;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.payment.RefundFullRequestDto;
import com.productadda.dto.payment.RefundResponseDto;
import com.productadda.entity.Inventory;
import com.productadda.entity.Order;
import com.productadda.entity.OrderItem;
import com.productadda.entity.Payment;
import com.productadda.entity.PaymentAuditLog;
import com.productadda.entity.PaymentStatus;
import com.productadda.entity.Refund;
import com.productadda.entity.User;
import com.productadda.exception.ApiException;
import com.productadda.repository.InventoryRepository;
import com.productadda.repository.OrderItemRepository;
import com.productadda.repository.OrderRepository;
import com.productadda.repository.PaymentAuditLogRepository;
import com.productadda.repository.PaymentRepository;
import com.productadda.repository.PaymentStatusRepository;
import com.productadda.repository.RefundRepository;
import com.productadda.repository.UserRepository;
import com.productadda.util.UuidUtil;

import lombok.RequiredArgsConstructor;

/*
 * ================================================================
 * NEW SERVICE: RefundServiceFullRefund
 * API 7/14 - POST /api/refunds/full
 * Admin-only (confirmed: no CUSTOMER_SUPPORT role exists in this
 * project's roles table - customers use the existing
 * OrderCancellationService self-service path instead).
 * ================================================================
 */
@Service
@RequiredArgsConstructor
public class RefundServiceFullRefund {

    private final PaymentRepository paymentRepository;
    private final PaymentAuditLogRepository paymentAuditLogRepository;
    private final PaymentStatusRepository paymentStatusRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final InventoryRepository inventoryRepository;
    private final RefundRepository refundRepository;
    private final UserRepository userRepository;

    private final RazorpayGatewayInitiateRefundService razorpayGatewayInitiateRefundService;

    private final UuidUtil uuidUtil;

    private static final long REFUND_WINDOW_DAYS = 30;

    /*
     * ================================================================
     * INITIATE FULL REFUND
     * ================================================================
     */
    @Transactional
    public RefundResponseDto initiateFullRefund(RefundFullRequestDto request) {
        Map<String, Object> metaData = new HashMap<>();
        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================

        if (!isValidRefundReason(request.getReason())) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "reason must be one of: cancellation, return, manual_admin");
        }

        // ==========================================
        // 1.2 CONTEXT AUTHENTICATION
        // ==========================================
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentication missing or invalid");
        }

        String currentUsername = authentication.getName();

        User actingAdmin = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Authenticated user no longer exists"));

        // ==========================================
        // 1.3 DATABASE LOOKUP VALIDATION
        // ==========================================
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Payment not found"));

        // CONFIRMED value: legacy PaymentServicePaymentVerify checks "SUCCESS"
        // directly.
        if (!"SUCCESS".equalsIgnoreCase(payment.getFkStatus().getStatusName())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Payment must be in SUCCESS status to refund");
        }

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));

        if (!payment.getFkOrder().getPkOrderId().equals(order.getPkOrderId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "orderId does not match the payment's linked order");
        }

        // TODO: "PROCESSING" is confirmed (legacy PaymentServicePaymentVerify
        // transitions orders into it). "SHIPPED"/"DELIVERED" are assumed
        // from the plan's prose only - verify these OrderStatus rows exist
        // before deploying.
        String orderStatusName = order.getFkStatus().getStatusName();

        boolean isRefundEligibleOrderStatus = "PROCESSING".equalsIgnoreCase(orderStatusName)
                || "SHIPPED".equalsIgnoreCase(orderStatusName)
                || "DELIVERED".equalsIgnoreCase(orderStatusName);
        if (!isRefundEligibleOrderStatus) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Order must be in PROCESSING, SHIPPED, or DELIVERED status to refund");
        }

        if (order.getTotalRefundedAmountInPaise() != null && order.getTotalRefundedAmountInPaise() > 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Order is already fully or partially refunded");
        }

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */

        // Refund window check. The plan leaves the exact approval policy as
        // a TODO ("//TODO: define approval policy") - since this endpoint is
        // already Admin-only (no separate customer-initiated path exists),
        // the window is enforced as a hard block unless adminApprovalOverride
        // is explicitly set to true on the request.
        boolean windowClosed = order.getCreatedAtUtc().plusDays(REFUND_WINDOW_DAYS)
                .isBefore(LocalDateTime.now(ZoneOffset.UTC));
        if (windowClosed && !Boolean.TRUE.equals(request.getAdminApprovalOverride())) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Refund window (30 days) has closed - set adminApprovalOverride to true to proceed");
        }

        long refundAmountInPaise = Math.round(order.getTotalAmount().doubleValue() * 100)
                - (order.getTotalRefundedAmountInPaise() != null ? order.getTotalRefundedAmountInPaise() : 0);

        if (refundAmountInPaise <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "No refundable amount remaining on this order");
        }

        // TODO: "INITIATED"/"PROCESSING"/"REFUNDED" refund-lifecycle status
        // rows are NOT confirmed against real payment_statuses data - see
        // CHANGES_DAY3.md for the corrected seed script.
        PaymentStatus initiatedStatus = paymentStatusRepository.findByStatusName("INITIATED")
                .orElseThrow(
                        () -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "INITIATED status not configured"));

        // LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);

        Refund refund = Refund.builder()
                .pkRefundId(uuidUtil.generateUuidV7())
                .fkPayment(payment)
                .fkOrder(order)
                .refundType("full")
                .refundAmountInPaise(refundAmountInPaise)
                .fkStatus(initiatedStatus)
                .initiatedBy("ADMIN")
                .reason(request.getReason())
                .refundTriggeredByOrderEvent(request.getReason())
                .isActive(true)
                .build();

        refund = refundRepository.save(refund);

        // Gateway call: razorpayPaymentId is Payment.gatewayTransactionId,
        // set during Day 1 webhook processing - NOT the gatewayOrderId.
        RazorpayGatewayInitiateRefundService.RazorpayRefundResult gatewayResult = razorpayGatewayInitiateRefundService.initiateRefund(
                payment.getGatewayTransactionId(), refundAmountInPaise);

        PaymentStatus processingStatus = paymentStatusRepository.findByStatusName("PROCESSING")
                .orElseThrow(
                        () -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "PROCESSING status not configured"));

        refund.setGatewayRefundId(gatewayResult.getGatewayRefundId());
        refund.setFkStatus(processingStatus);

        PaymentStatus refundedStatus = paymentStatusRepository.findByStatusName("REFUNDED")
                .orElseThrow(
                        () -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "REFUNDED status not configured"));

        String oldPaymentStatus = payment.getFkStatus().getStatusName();
        payment.setFkStatus(refundedStatus);

        // Inventory restoration: increment available_quantity for every
        // active item on this order.
        List<OrderItem> orderItems = orderItemRepository.findByFkOrderAndIsActiveTrue(order);
        for (OrderItem orderItem : orderItems) {
            Inventory inventory = inventoryRepository.findByFkProduct(orderItem.getFkProduct())
                    .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Inventory record missing for a product on this order"));
            inventory.setAvailableQuantity(inventory.getAvailableQuantity() + orderItem.getQuantity());
            inventoryRepository.save(inventory);
        }

        order.setTotalRefundedAmountInPaise(refundAmountInPaise);
        // NOTE: Order.fkStatus is deliberately left unchanged here. The plan
        // itself marks the post-refund order state transition as
        // "//TODO: define order state machine" - rather than guess at an
        // unconfirmed OrderStatus value, this is left for a future,
        // explicitly-scoped decision.

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * ================================================================
         */
        refund = refundRepository.save(refund);

        payment = paymentRepository.save(payment);

        order = orderRepository.save(order);

        Map<String, Object> details = new HashMap<>();

        details.put("refundId", refund.getPkRefundId());
        details.put("amountInPaise", refund.getRefundAmountInPaise());

        metaData.put("fullRefundOrder", details);

        PaymentAuditLog auditLog = PaymentAuditLog.builder()
                .pkAuditLogId(uuidUtil.generateUuidV7())
                .fkPayment(payment)
                .fkRefund(refund)
                .action("full_refund_initiated")
                .fkActor(actingAdmin)
                .actorRole("ADMIN")
                .oldStatus(oldPaymentStatus)
                .newStatus("REFUNDED")
                .metadata(metaData)
                .build();

        paymentAuditLogRepository.save(auditLog);

        // TODO: will trigger refund_initiated notification hooks

        /*
         * ================================================================
         * 4. RESPONSE MAPPING
         * ================================================================
         */
        return RefundResponseDto.builder()
                .refundId(refund.getPkRefundId().toString())
                .paymentId(payment.getPkPaymentId().toString())
                .orderId(order.getPkOrderId().toString())
                .refundType("full")
                .refundAmountInPaise(refundAmountInPaise)
                .status(refund.getFkStatus().getStatusName())
                .gatewayRefundId(refund.getGatewayRefundId())
                .initiatedBy(refund.getInitiatedBy())
                .reason(refund.getReason())
                .createdAtUtc(refund.getCreatedAtUtc())
                .processedAtUtc(refund.getProcessedAtUtc())
                .failureReason(null)
                .lineItems(null)
                .build();
    }

    private boolean isValidRefundReason(String reason) {
        return "cancellation".equalsIgnoreCase(reason)
                || "return".equalsIgnoreCase(reason)
                || "manual_admin".equalsIgnoreCase(reason);
    }
}
