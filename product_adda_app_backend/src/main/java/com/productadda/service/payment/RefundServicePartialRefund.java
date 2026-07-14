package com.productadda.service.payment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.payment.RefundLineItemRequestDto;
import com.productadda.dto.payment.RefundLineItemResponseDto;
import com.productadda.dto.payment.RefundPartialRequestDto;
import com.productadda.dto.payment.RefundResponseDto;

import com.productadda.entity.Inventory;
import com.productadda.entity.Order;
import com.productadda.entity.OrderItem;
import com.productadda.entity.Payment;
import com.productadda.entity.PaymentAuditLog;
import com.productadda.entity.PaymentStatus;
import com.productadda.entity.Refund;
import com.productadda.entity.RefundLineItem;
import com.productadda.entity.User;

import com.productadda.exception.ApiException;

import com.productadda.repository.InventoryRepository;
import com.productadda.repository.OrderItemRepository;
import com.productadda.repository.OrderRepository;
import com.productadda.repository.PaymentAuditLogRepository;
import com.productadda.repository.PaymentRepository;
import com.productadda.repository.PaymentStatusRepository;
import com.productadda.repository.RefundLineItemRepository;
import com.productadda.repository.RefundRepository;
import com.productadda.repository.UserRepository;

import com.productadda.util.UuidUtil;

import lombok.RequiredArgsConstructor;

/*
 * ================================================================
 * RefundServicePartialRefund
 * API 8/14 - POST /api/refunds/partial
 * Admin-only, same rationale as RefundServiceFullRefund.
 * ================================================================
 */
@Service
@RequiredArgsConstructor
public class RefundServicePartialRefund {

        private final PaymentRepository paymentRepository;
        private final PaymentAuditLogRepository paymentAuditLogRepository;
        private final PaymentStatusRepository paymentStatusRepository;
        private final OrderRepository orderRepository;
        private final OrderItemRepository orderItemRepository;
        private final InventoryRepository inventoryRepository;
        private final RefundRepository refundRepository;
        private final RefundLineItemRepository refundLineItemRepository;
        private final UserRepository userRepository;

        private final RazorpayGatewayInitiateRefundService razorpayGatewayInitiateRefundService;

        private final UuidUtil uuidUtil;

        /*
         * ================================================================
         * INITIATE PARTIAL REFUND
         * ================================================================
         */
        @Transactional
        public RefundResponseDto initiatePartialRefund(RefundPartialRequestDto request) {

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

                UUID orderId;
                try {
                        orderId = UUID.fromString(request.getOrderId());
                } catch (IllegalArgumentException exception) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "orderId is not a valid identifier");
                }

                if (!isValidRefundReason(request.getReason())) {
                        throw new ApiException(HttpStatus.BAD_REQUEST,
                                        "reason must be one of: cancellation, return, manual_admin");
                }

                if (request.getRefundLineItems() == null || request.getRefundLineItems().isEmpty()) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "At least one refund line item is required");
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

                User actingAdmin = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                "Authenticated user no longer exists"));

                // ==========================================
                // 1.3 DATABASE LOOKUP VALIDATION
                // ==========================================
                Payment payment = paymentRepository.findById(paymentId)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Payment not found"));

                // Eligible statuses: SUCCESS (first refund) or PARTIALLY_REFUNDED
                // (a subsequent partial refund on an already-partially-refunded payment).
                String currentPaymentStatus = payment.getFkStatus().getStatusName();
                boolean isRefundEligiblePaymentStatus = "SUCCESS".equalsIgnoreCase(currentPaymentStatus)
                                || "PARTIALLY_REFUNDED".equalsIgnoreCase(currentPaymentStatus);
                if (!isRefundEligiblePaymentStatus) {
                        throw new ApiException(HttpStatus.BAD_REQUEST,
                                        "Payment must be in SUCCESS or PARTIALLY_REFUNDED status to refund");
                }

                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));

                if (!payment.getFkOrder().getPkOrderId().equals(order.getPkOrderId())) {
                        throw new ApiException(HttpStatus.BAD_REQUEST,
                                        "orderId does not match the payment's linked order");
                }

                // TODO: same caveat as RefundServiceFullRefund - only "PROCESSING" is
                // confirmed.
                String orderStatusName = order.getFkStatus().getStatusName();
                boolean isRefundEligibleOrderStatus = "PROCESSING".equalsIgnoreCase(orderStatusName)
                                || "SHIPPED".equalsIgnoreCase(orderStatusName)
                                || "DELIVERED".equalsIgnoreCase(orderStatusName);
                if (!isRefundEligibleOrderStatus) {
                        throw new ApiException(HttpStatus.BAD_REQUEST,
                                        "Order must be in PROCESSING, SHIPPED, or DELIVERED status to refund");
                }

                // Resolve and validate each requested line item up front, before any writes.
                List<OrderItem> resolvedOrderItems = new ArrayList<>();
                long totalRequestedRefundInPaise = 0;
                for (RefundLineItemRequestDto lineItemRequest : request.getRefundLineItems()) {

                        UUID orderItemId;
                        try {
                                orderItemId = UUID.fromString(lineItemRequest.getOrderItemId());
                        } catch (IllegalArgumentException exception) {
                                throw new ApiException(HttpStatus.BAD_REQUEST, "orderItemId is not a valid identifier");
                        }

                        OrderItem orderItem = orderItemRepository
                                        .findByPkOrderItemIdAndFkOrder_PkOrderId(orderItemId, orderId)
                                        .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST,
                                                        "Order item not found"));

                        long lineTotalInPaise = Math.round(orderItem.getLineTotal().doubleValue() * 100);
                        if (lineItemRequest.getRefundAmountInPaise() <= 0
                                        || lineItemRequest.getRefundAmountInPaise() > lineTotalInPaise) {
                                throw new ApiException(HttpStatus.BAD_REQUEST,
                                                "Refund amount for an order item must be > 0 and <= its line total");
                        }

                        resolvedOrderItems.add(orderItem);
                        totalRequestedRefundInPaise += lineItemRequest.getRefundAmountInPaise();
                }

                long alreadyRefundedInPaise = order.getTotalRefundedAmountInPaise() != null
                                ? order.getTotalRefundedAmountInPaise()
                                : 0;
                long remainingEligibleInPaise = Math.round(order.getTotalAmount().doubleValue() * 100)
                                - alreadyRefundedInPaise;

                if (totalRequestedRefundInPaise > remainingEligibleInPaise) {
                        throw new ApiException(HttpStatus.BAD_REQUEST,
                                        "Total refund amount exceeds remaining refund-eligible amount");
                }

                /*
                 * ================================================================
                 * 2. BUSINESS RULES & PROCESSING / WORKFLOW
                 * ================================================================
                 */

                // TODO: "INITIATED"/"PROCESSING" refund-lifecycle status rows are
                // NOT confirmed against real payment_statuses data - see
                // CHANGES_DAY3.md for the corrected seed script.
                PaymentStatus initiatedStatus = paymentStatusRepository.findByStatusName("INITIATED")
                                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                "INITIATED status not configured"));

                Refund refund = Refund.builder()
                                .pkRefundId(uuidUtil.generateUuidV7())
                                .fkPayment(payment)
                                .fkOrder(order)
                                .refundType("partial")
                                .refundAmountInPaise(totalRequestedRefundInPaise)
                                .fkStatus(initiatedStatus)
                                .initiatedBy("ADMIN")
                                .reason(request.getReason())
                                .refundTriggeredByOrderEvent(request.getReason())
                                .isActive(true)
                                .build();
                refund = refundRepository.save(refund);

                List<RefundLineItem> refundLineItems = new ArrayList<>();
                for (int i = 0; i < resolvedOrderItems.size(); i++) {
                        OrderItem orderItem = resolvedOrderItems.get(i);
                        Long lineRefundAmount = request.getRefundLineItems().get(i).getRefundAmountInPaise();

                        RefundLineItem refundLineItem = RefundLineItem.builder()
                                        .pkRefundLineItemId(uuidUtil.generateUuidV7())
                                        .fkRefund(refund)
                                        .fkOrderItem(orderItem)
                                        .refundAmountInPaise(lineRefundAmount)
                                        .isActive(true)
                                        .build();
                        refundLineItems.add(refundLineItemRepository.save(refundLineItem));

                        // Inventory restoration: this line item's full quantity only -
                        // partial-quantity-within-a-line-item refunds are out of scope.
                        Inventory inventory = inventoryRepository.findByFkProduct(orderItem.getFkProduct())
                                        .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                        "Inventory record missing for a product on this order"));
                        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + orderItem.getQuantity());
                        inventoryRepository.save(inventory);
                }

                // Gateway call: razorpayPaymentId is Payment.gatewayTransactionId,
                // set during Day 1 webhook processing - NOT the gatewayOrderId.
                RazorpayGatewayInitiateRefundService.RazorpayRefundResult gatewayResult = razorpayGatewayInitiateRefundService
                                .initiateRefund(
                                                payment.getGatewayTransactionId(), totalRequestedRefundInPaise);

                PaymentStatus processingStatus = paymentStatusRepository.findByStatusName("PROCESSING")
                                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                "PROCESSING status not configured"));

                refund.setGatewayRefundId(gatewayResult.getGatewayRefundId());
                refund.setFkStatus(processingStatus);

                PaymentStatus partiallyRefundedStatus = paymentStatusRepository.findByStatusName("PARTIALLY_REFUNDED")
                                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                "PARTIALLY_REFUNDED status not configured"));

                String oldPaymentStatus = payment.getFkStatus().getStatusName();
                payment.setFkStatus(partiallyRefundedStatus);

                order.setTotalRefundedAmountInPaise(alreadyRefundedInPaise + totalRequestedRefundInPaise);

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
                details.put("refundedItemsCount", refundLineItems.size());
                details.put("totalAmountInPaise", totalRequestedRefundInPaise);

                metaData.put("fullRefundOrder", details);

                PaymentAuditLog auditLog = PaymentAuditLog.builder()
                                .pkAuditLogId(uuidUtil.generateUuidV7())
                                .fkPayment(payment)
                                .fkRefund(refund)
                                .action("partial_refund_initiated")
                                .fkActor(actingAdmin)
                                .actorRole("ADMIN")
                                .oldStatus(oldPaymentStatus)
                                .newStatus("PARTIALLY_REFUNDED")
                                .metadata(metaData)
                                .build();
                paymentAuditLogRepository.save(auditLog);

                // TODO: will trigger refund_initiated notification hooks

                /*
                 * ================================================================
                 * 4. RESPONSE MAPPING
                 * ================================================================
                 */
                List<RefundLineItemResponseDto> lineItemResponseDtos = new ArrayList<>();
                for (RefundLineItem refundLineItem : refundLineItems) {
                        lineItemResponseDtos.add(RefundLineItemResponseDto.builder()
                                        .orderItemId(refundLineItem.getFkOrderItem().getPkOrderItemId().toString())
                                        .productNameSnapshot(refundLineItem.getFkOrderItem().getProductNameSnapshot())
                                        .refundAmountInPaise(refundLineItem.getRefundAmountInPaise())
                                        .build());
                }

                return RefundResponseDto.builder()
                                .refundId(refund.getPkRefundId().toString())
                                .paymentId(payment.getPkPaymentId().toString())
                                .orderId(order.getPkOrderId().toString())
                                .refundType("partial")
                                .refundAmountInPaise(totalRequestedRefundInPaise)
                                .status(refund.getFkStatus().getStatusName())
                                .gatewayRefundId(refund.getGatewayRefundId())
                                .initiatedBy(refund.getInitiatedBy())
                                .reason(refund.getReason())
                                .createdAtUtc(refund.getCreatedAtUtc())
                                .processedAtUtc(refund.getProcessedAtUtc())
                                .failureReason(null)
                                .lineItems(lineItemResponseDtos)
                                .build();
        }

        private boolean isValidRefundReason(String reason) {
                return "cancellation".equalsIgnoreCase(reason)
                                || "return".equalsIgnoreCase(reason)
                                || "manual_admin".equalsIgnoreCase(reason);
        }
}
