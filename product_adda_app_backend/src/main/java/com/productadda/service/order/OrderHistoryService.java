package com.productadda.service.order;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.order.OrderHistoryResponseDto;
import com.productadda.dto.order.OrderItemSummaryDto;
import com.productadda.entity.Order;
import com.productadda.entity.User;
import com.productadda.exception.ApiException;
import com.productadda.repository.OrderItemRepository;
import com.productadda.repository.OrderRepository;
import com.productadda.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderHistoryService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional(readOnly = true)
    public List<OrderHistoryResponseDto> getMyOrderHistory() {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        // Note: Parameterless methodology tracking. No request variables to assert.

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

        // ==========================================
        // 1.3 DATABASE LOOKUP VALIDATION
        // ==========================================
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Authenticated user no longer exists"));

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        List<Order> userOrders = orderRepository.findByFkUserAndIsActiveTrueOrderByCreatedAtUtcDesc(currentUser);

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * Note: Read-only query execution profile context.
         * ================================================================
         */

        /*
         * ================================================================
         * 4. RESPONSE MAPPING
         * ================================================================
         */
        return userOrders.stream().map(order -> {

            List<OrderItemSummaryDto> itemSummaries = orderItemRepository.findByFkOrderAndIsActiveTrue(order)
                    .stream()
                    .map(item -> OrderItemSummaryDto.builder()
                            .orderItemId(item.getPkOrderItemId())
                            .productId(item.getFkProduct() != null ? item.getFkProduct().getPkProductId() : null)
                            .productNameSnapshot(item.getProductNameSnapshot())
                            .quantity(item.getQuantity())
                            .unitPrice(item.getUnitPrice())
                            .build())
                    .collect(Collectors.toList());

            return OrderHistoryResponseDto.builder()
                    .orderId(order.getPkOrderId())
                    .statusName(order.getFkStatus() != null ? order.getFkStatus().getStatusName() : "UNKNOWN")
                    .totalAmount(order.getTotalAmount())
                    .createdAtUtc(order.getCreatedAtUtc())
                    .items(itemSummaries)
                    .build();
        }).collect(Collectors.toList());
    }
}