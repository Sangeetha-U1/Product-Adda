package com.productadda.service.order;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.order.CustomerOrderListDto;
import com.productadda.dto.order.PaginatedCustomerOrderResponseDto;

import com.productadda.entity.Address;
import com.productadda.entity.Order;
import com.productadda.entity.User;
import com.productadda.entity.UserRole;

import com.productadda.exception.ApiException;

import com.productadda.repository.OrderItemRepository;
import com.productadda.repository.OrderRepository;
import com.productadda.repository.UserRepository;
import com.productadda.repository.UserRoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerOrderRetrievalService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    /*
     * ================================================================
     * GET CUSTOMER ORDERS
     * Description: Retrieves a paginated list of the authenticated
     * customer's own orders, with optional status and created-date
     * range filters. Excludes payment details from the response.
     * ================================================================
     */
    @Transactional(readOnly = true)
    public PaginatedCustomerOrderResponseDto getCustomerOrders(
            int page, int size, String status, String startDate, String endDate) {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        if (page < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "page must be >= 0");
        }
        if (size <= 0 || size > 100) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "size must be > 0 and <= 100");
        }

        String safeStatus = (status == null || status.trim().isEmpty())
                ? null
                : status.trim().toUpperCase();

        LocalDateTime startDateTime = null;
        if (startDate != null && !startDate.trim().isEmpty()) {
            try {
                startDateTime = LocalDate.parse(startDate.trim()).atStartOfDay();
            } catch (DateTimeParseException ex) {
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "startDate must be a valid ISO date (yyyy-MM-dd)");
            }
        }

        LocalDateTime endDateTime = null;
        if (endDate != null && !endDate.trim().isEmpty()) {
            try {
                endDateTime = LocalDate.parse(endDate.trim()).atTime(23, 59, 59);
            } catch (DateTimeParseException ex) {
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "endDate must be a valid ISO date (yyyy-MM-dd)");
            }
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
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "Authenticated user no longer exists"));

        // Zero-Trust role re-check: this endpoint must never trust that the
        // caller is a customer just because they hold a valid JWT. A VENDOR
        // or ADMIN token authenticates successfully but must be explicitly
        // rejected here, since /api/customers/orders has no controller-level
        // @PreAuthorize gate (customers have no single-role annotation to
        // hang off of the way VENDOR/ADMIN do).
        List<UserRole> userRoles = userRoleRepository.findByFkUser(currentUser);

        if (userRoles.isEmpty()) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "Customer role required to access this endpoint");
        }

        List<String> assignedRoleNames = userRoles.stream()
                .map(userRole -> userRole.getFkRole().getRoleName())
                .collect(Collectors.toList());

        boolean hasCustomerRole = assignedRoleNames.stream()
                .anyMatch(roleName -> "CUSTOMER".equals(roleName));

        if (!hasCustomerRole) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "Customer role required to access this endpoint");
        }

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, Order.SORT_BY_CREATED_AT_UTC));

        Page<Order> orderPage = orderRepository.findCustomerOrdersWithFilters(
                currentUser, safeStatus, startDateTime, endDateTime, pageRequest);

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
        List<CustomerOrderListDto> items = orderPage.getContent().stream()
                .map(order -> {

                    long itemCount = orderItemRepository.countByFkOrderAndIsActiveTrue(order);
                    Address address = order.getFkAddress();

                    return CustomerOrderListDto.builder()
                            .orderId(order.getPkOrderId())
                            .orderNumber(order.getOrderNumber())
                            .statusName(order.getFkStatus() != null
                                    ? order.getFkStatus().getStatusName()
                                    : "UNKNOWN")
                            .totalAmount(order.getTotalAmount())
                            .itemCount(itemCount)
                            .createdAtUtc(order.getCreatedAtUtc())
                            .shippingCity(address != null ? address.getCity() : null)
                            .shippingState(address != null ? address.getState() : null)
                            .build();
                })
                .collect(Collectors.toList());

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * ================================================================
         */
        return PaginatedCustomerOrderResponseDto.builder()
                .content(items)
                .totalElements(orderPage.getTotalElements())
                .page(page)
                .size(size)
                .build();
    }
}