package com.productadda.service.order;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.order.OrderAddressSummaryDto;
import com.productadda.dto.order.OrderCouponSummaryDto;
import com.productadda.dto.order.OrderDetailResponseDto;
import com.productadda.dto.order.OrderItemSummaryDto;
import com.productadda.dto.order.OrderPaymentSummaryDto;

import com.productadda.entity.Address;
import com.productadda.entity.Coupon;
import com.productadda.entity.Order;
import com.productadda.entity.Payment;
import com.productadda.entity.User;

import com.productadda.exception.ApiException;

import com.productadda.repository.OrderItemRepository;
import com.productadda.repository.OrderRepository;
import com.productadda.repository.PaymentRepository;
import com.productadda.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderRetrievalService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public OrderDetailResponseDto getOrderById(UUID orderId) {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        if (orderId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Order id is required");
        }

        // ==========================================
        // 1.2 CONTEXT AUTHENTICATION
        // ==========================================
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentication missing or invalid");
        }

        String currentUsername = authentication.getName();

        // ==========================================
        // 1.3 DATABASE LOOKUP VALIDATION
        // ==========================================
        User currentUser = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Authenticated user no longer exists"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));

        if (!order.getFkUser().getPkUserId().equals(currentUser.getPkUserId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied: this order does not belong to you");
        }

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        List<OrderItemSummaryDto> itemSummaries = orderItemRepository.findByFkOrderAndIsActiveTrue(order)
                .stream()
                .map(item -> OrderItemSummaryDto.builder()
                        .orderItemId(item.getPkOrderItemId())
                        .productId(item.getFkProduct() != null ? item.getFkProduct().getPkProductId() : null)
                        .productNameSnapshot(item.getProductNameSnapshot())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .lineTotal(item.getLineTotal())
                        .build())
                .collect(Collectors.toList());

        Address address = order.getFkAddress();
        Coupon coupon = order.getFkCoupon();
        Payment payment = paymentRepository.findByFkOrder(order).orElse(null);

        /*
         * ================================================================
         * 3. DB SAVING SECTION (Skip if Read-Only GET)
         * Reason: Read-only order detail lookup, no mutations performed.
         * ================================================================
         */

        /*
         * ================================================================
         * 4. POST-SAVING DATA SANITIZATION & MASKING
         * ================================================================
         */
        OrderAddressSummaryDto addressSummary = OrderAddressSummaryDto.builder()
                .addressId(address.getPkAddressId())
                .addressLine1(address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .landmark(address.getLandmark())
                .city(address.getCity())
                .state(address.getState())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .build();

        OrderCouponSummaryDto couponSummary = coupon == null ? null : OrderCouponSummaryDto.builder()
                .couponId(coupon.getPkCouponId())
                .couponCode(coupon.getCouponCode())
                .discountType(coupon.getFkDiscountType().getDiscountTypeCode())
                .discountValue(coupon.getDiscountValue())
                .build();

        // NOTE: Assumes PaymentStatus exposes getStatusName(), mirroring the
        // OrderStatus entity pattern used elsewhere in this codebase.
        OrderPaymentSummaryDto paymentSummary = payment == null ? null : OrderPaymentSummaryDto.builder()
                .paymentId(payment.getPkPaymentId())
                .statusName(payment.getFkStatus().getStatusName())
                .paymentMethod(payment.getPaymentMethod())
                .amountPaidInPaise(payment.getAmountInPaise())
                .paidAtUtc(payment.getPaidAtUtc())
                .build();

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * ================================================================
         */
        return OrderDetailResponseDto.builder()
                .orderId(order.getPkOrderId())
                .orderNumber(order.getOrderNumber())
                .statusName(order.getFkStatus().getStatusName())
                .subtotal(order.getSubtotal())
                .couponDiscount(order.getCouponDiscount())
                .shippingCost(order.getShippingCost())
                .taxAmount(order.getTaxAmount())
                .totalAmount(order.getTotalAmount())
                .createdAtUtc(order.getCreatedAtUtc())
                .items(itemSummaries)
                .address(addressSummary)
                .coupon(couponSummary)
                .payment(paymentSummary)
                .build();
    }
}
