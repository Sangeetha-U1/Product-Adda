package com.productadda.service.customer;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.customer.CustomerDeliveryHistoryListDto;
import com.productadda.dto.customer.PaginatedCustomerDeliveryHistoryResponseDto;
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
public class CustomerGetOrdersDeliveryHistoryService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    /*
     * ================================================================
     * GET CUSTOMER DELIVERY HISTORY
     * Description: Retrieves a paginated list of the authenticated
     * customer's DELIVERED-only orders, with the full delivery
     * timestamp trail. Dedicated endpoint
     * (separate from the general /api/customers/orders filter).
     * ================================================================
     */
    @Transactional(readOnly = true)
    public PaginatedCustomerDeliveryHistoryResponseDto getDeliveryHistory(int page, int size) {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */
        if (page < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "page must be >= 0");
        }
        if (size <= 0 || size > 100) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "size must be > 0 and <= 100");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentication missing or invalid");
        }

        String currentUsername = authentication.getName();

        User currentUser = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "Authenticated user no longer exists"));

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
        PageRequest pageRequest = PageRequest.of(page, size);

        Page<Order> historyPage = orderRepository.findDeliveryHistoryForCustomer(currentUser, pageRequest);

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
        List<CustomerDeliveryHistoryListDto> items = historyPage.getContent().stream()
                .map(order -> {

                    long itemCount = orderItemRepository.countByFkOrderAndIsActiveTrue(order);
                    Address address = order.getFkAddress();

                    return CustomerDeliveryHistoryListDto.builder()
                            .orderId(order.getPkOrderId())
                            .orderNumber(order.getOrderNumber())
                            .statusName(order.getFkStatus() != null
                                    ? order.getFkStatus().getStatusName()
                                    : "UNKNOWN")
                            .totalAmount(order.getTotalAmount())
                            .itemCount(itemCount)
                            .orderedAtUtc(order.getCreatedAtUtc())
                            .pickedUpAtUtc(order.getPickedUpAtUtc())
                            .outForDeliveryAtUtc(order.getOutForDeliveryAtUtc())
                            .deliveredAtUtc(order.getDeliveredAtUtc())
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
        return PaginatedCustomerDeliveryHistoryResponseDto.builder()
                .content(items)
                .totalElements(historyPage.getTotalElements())
                .page(page)
                .size(size)
                .build();
    }
}