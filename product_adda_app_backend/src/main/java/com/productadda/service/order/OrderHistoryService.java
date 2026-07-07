package com.productadda.service.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.order.OrderListItemDto;
import com.productadda.dto.order.PaginatedOrderResponseDto;

import com.productadda.entity.Order;
import com.productadda.entity.User;

import com.productadda.exception.ApiException;

import com.productadda.repository.OrderRepository;
import com.productadda.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderHistoryService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public PaginatedOrderResponseDto getMyOrderHistory(int page, int pageSize, String sortBy, String sortOrder) {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        int safePage = page < 0 ? 0 : page;
        int safePageSize = (pageSize <= 0 || pageSize > 100) ? 20 : pageSize;

        String safeSortBy = (sortBy == null || sortBy.trim().isEmpty()) ? "createdAtUtc" : sortBy.trim();

        Sort.Direction direction = "ASC".equalsIgnoreCase(sortOrder)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

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
        PageRequest pageRequest = PageRequest.of(safePage, safePageSize, Sort.by(direction, safeSortBy));

        Page<Order> orderPage;
        try {
            orderPage = orderRepository.findByFkUserAndIsActiveTrue(currentUser, pageRequest);
        } catch (IllegalArgumentException  ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid sortBy field: " + safeSortBy);
        }

        /*
         * ================================================================
         * 3. DB SAVING SECTION (Skip if Read-Only GET)
         * Reason: Read-only paginated lookup, no mutations performed.
         * ================================================================
         */

        /*
         * ================================================================
         * 4. POST-SAVING DATA SANITIZATION & MASKING
         * ================================================================
         */
        List<OrderListItemDto> items = orderPage.getContent().stream()
                .map(order -> OrderListItemDto.builder()
                        .orderId(order.getPkOrderId())
                        .orderNumber(order.getOrderNumber())
                        .statusName(order.getFkStatus() != null ? order.getFkStatus().getStatusName() : "UNKNOWN")
                        .totalAmount(order.getTotalAmount())
                        .createdAtUtc(order.getCreatedAtUtc())
                        .build())
                .collect(Collectors.toList());

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * ================================================================
         */
        return PaginatedOrderResponseDto.builder()
                .orders(items)
                .totalCount(orderPage.getTotalElements())
                .page(safePage)
                .pageSize(safePageSize)
                .build();
    }
}