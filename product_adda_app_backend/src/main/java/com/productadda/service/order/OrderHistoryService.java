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

        // 1. Context Authentication Extraction
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentication missing or invalid");
        }

        String email;
        if (authentication.getPrincipal() instanceof UserDetails userDetails) {
            email = userDetails.getUsername();
        } else {
            email = authentication.getPrincipal().toString();
        }

        // 2. Strict User Isolation Verification
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Authenticated user no longer exists"));

        // 3. Chronological Order Fetching
        List<Order> userOrders = orderRepository.findByFkUserAndIsActiveTrueOrderByCreatedAtUtcDesc(currentUser);

        // 4. Data Transformation Pipeline
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