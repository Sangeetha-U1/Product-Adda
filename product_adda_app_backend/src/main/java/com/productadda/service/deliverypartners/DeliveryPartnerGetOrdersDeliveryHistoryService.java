package com.productadda.service.deliverypartners;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.deliverypartner.DeliveryPartnerMyOrderListDto;
import com.productadda.dto.deliverypartner.PaginatedDeliveryPartnerMyOrderResponseDto;
import com.productadda.entity.DeliveryPartner;
import com.productadda.entity.Order;
import com.productadda.entity.OrderItem;
import com.productadda.entity.User;
import com.productadda.entity.UserRole;

import com.productadda.exception.ApiException;

import com.productadda.repository.DeliveryPartnerRepository;
import com.productadda.repository.OrderItemRepository;
import com.productadda.repository.OrderRepository;
import com.productadda.repository.UserRepository;
import com.productadda.repository.UserRoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeliveryPartnerGetOrdersDeliveryHistoryService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final DeliveryPartnerRepository deliveryPartnerRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    /*
     * ================================================================
     * RETRIEVE DELIVERY HISTORY FOR THE AUTHENTICATED DELIVERY PARTNER
     * Description: DELIVERED-only orders assigned to this partner.
     * ================================================================
     */
    @Transactional(readOnly = true)
    public PaginatedDeliveryPartnerMyOrderResponseDto getDeliveryHistory(Pageable pageable) {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */
        if (pageable == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Pageable parameters are required");
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
                    "Delivery partner role required to access this endpoint");
        }

        boolean hasDeliveryPartnerRole = userRoles.stream()
                .map(userRole -> userRole.getFkRole().getRoleName())
                .anyMatch(roleName -> "DELIVERY_PARTNER".equals(roleName));

        if (!hasDeliveryPartnerRole) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "Delivery partner role required to access this endpoint");
        }

        DeliveryPartner partner = deliveryPartnerRepository.findByFkUser(currentUser)
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN,
                        "Delivery partner profile not found"));

        if (!Boolean.TRUE.equals(partner.getIsActive())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Delivery partner account is inactive");
        }

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        Page<Order> historyPage = orderRepository.findDeliveryHistoryForDeliveryPartner(partner, pageable);
        List<Order> ordersList = historyPage.getContent();

        Map<UUID, List<OrderItem>> itemsMap = Collections.emptyMap();
        if (!ordersList.isEmpty()) {
            List<OrderItem> allItems = orderItemRepository.findByFkOrderInAndIsActiveTrue(ordersList);
            itemsMap = allItems.stream()
                    .filter(item -> item.getFkOrder() != null)
                    .collect(Collectors.groupingBy(item -> item.getFkOrder().getPkOrderId()));
        }

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * ================================================================
         */
        // No saving for retrieval endpoints

        /*
         * ================================================================
         * 4. POST-SAVING DATA SANITIZATION & MASKING
         * ================================================================
         */
        // Data masking is handled during the conversion process into the minimal
        // exposure response models.

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * ================================================================
         */
        final Map<UUID, List<OrderItem>> finalItemsMap = itemsMap;
        List<DeliveryPartnerMyOrderListDto> mappedOrders = ordersList.stream()
                .map(order -> {
                    List<OrderItem> orderItems = finalItemsMap.getOrDefault(order.getPkOrderId(),
                            Collections.emptyList());

                    int distinctItemsCount = orderItems.size();
                    int totalQuantity = orderItems.stream()
                            .mapToInt(item -> item.getQuantity() != null
                                    ? item.getQuantity()
                                    : 0)
                            .sum();

                    String itemSummaryText = orderItems.stream()
                            .map(item -> (item.getQuantity() != null ? item.getQuantity()
                                    : 0) + "x " + item.getProductNameSnapshot())
                            .collect(Collectors.joining(", "));

                    String city = order.getFkAddress() != null ? order.getFkAddress().getCity()
                            : null;
                    String state = order.getFkAddress() != null ? order.getFkAddress().getState()
                            : null;
                    String postalCode = order.getFkAddress() != null
                            ? order.getFkAddress().getPostalCode()
                            : null;

                    return DeliveryPartnerMyOrderListDto.builder()
                            .orderId(order.getPkOrderId())
                            .orderNumber(order.getOrderNumber())
                            .statusName(order.getFkStatus() != null
                                    ? order.getFkStatus().getStatusName()
                                    : "UNKNOWN")
                            .createdAtUtc(order.getCreatedAtUtc())
                            .totalAmount(order.getTotalAmount())
                            .totalItems(distinctItemsCount)
                            .estimatedPackageCount(totalQuantity)
                            .itemSummary(itemSummaryText)
                            .city(city)
                            .state(state)
                            .postalCode(postalCode)
                            .pickedUpAtUtc(order.getPickedUpAtUtc())
                            .outForDeliveryAtUtc(order.getOutForDeliveryAtUtc())
                            .deliveredAtUtc(order.getDeliveredAtUtc())
                            .build();
                })
                .collect(Collectors.toList());

        return PaginatedDeliveryPartnerMyOrderResponseDto.builder()
                .orders(mappedOrders)
                .currentPage(historyPage.getNumber())
                .totalPages(historyPage.getTotalPages())
                .totalElements(historyPage.getTotalElements())
                .pageSize(historyPage.getSize())
                .isFirst(historyPage.isFirst())
                .isLast(historyPage.isLast())
                .build();
    }

}