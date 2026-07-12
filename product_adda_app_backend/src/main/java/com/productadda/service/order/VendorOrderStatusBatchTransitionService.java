package com.productadda.service.order;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.order.ItemStatusBatchEntryDto;
import com.productadda.dto.order.ItemStatusBatchResponseDto;
import com.productadda.dto.order.OrderItemResponseDto;

import com.productadda.entity.ItemStatus;
import com.productadda.entity.Order;
import com.productadda.entity.OrderItem;
import com.productadda.entity.User;
import com.productadda.entity.UserRole;
import com.productadda.entity.Vendor;

import com.productadda.exception.ApiException;

import com.productadda.repository.ItemStatusRepository;
import com.productadda.repository.OrderItemRepository;
import com.productadda.repository.OrderRepository;
import com.productadda.repository.UserRepository;
import com.productadda.repository.UserRoleRepository;
import com.productadda.repository.VendorRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VendorOrderStatusBatchTransitionService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final VendorRepository vendorRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ItemStatusRepository itemStatusRepository;

    private final AggregatedStatusCalculationService aggregatedStatusCalculationService;

    /*
     * ================================================================
     * UPDATE VENDOR ITEM STATUS (BATCH)
     * Description: Transitions multiple order items belonging to the
     * same order and the same vendor, in a single all-or-nothing
     * request. Every entry is validated before ANY mutation is applied.
     * ================================================================
     */
    @Transactional
    public ItemStatusBatchResponseDto batchUpdateVendorItemStatus(UUID orderId, List<ItemStatusBatchEntryDto> items) {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        if (orderId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "orderId is required");
        }
        if (items == null || items.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "items list must not be empty");
        }

        Set<UUID> seenItemIds = new HashSet<>();
        for (ItemStatusBatchEntryDto entry : items) {
            if (entry.getItemId() == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "itemId is required for every batch entry");
            }
            if (!seenItemIds.add(entry.getItemId())) {
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "Duplicate itemId in batch request: " + entry.getItemId());
            }
            if (entry.getRequestedStatus() == null || entry.getRequestedStatus().trim().isEmpty()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "requestedStatus is required for every batch entry");
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

        List<UserRole> userRoles = userRoleRepository.findByFkUser(currentUser);

        if (userRoles.isEmpty()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Vendor role required to access this endpoint");
        }

        boolean hasVendorRole = userRoles.stream()
                .map(userRole -> userRole.getFkRole().getRoleName())
                .anyMatch(roleName -> "VENDOR".equals(roleName));

        if (!hasVendorRole) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Vendor role required to access this endpoint");
        }

        Vendor vendor = vendorRepository.findByFkUser(currentUser)
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN,
                        "Vendor role required to access this endpoint"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));

        String orderStatusName = order.getFkStatus() != null ? order.getFkStatus().getStatusName() : null;

        if (!"PAID".equals(orderStatusName) && !"PROCESSING".equals(orderStatusName)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Cannot update items when order is in " + orderStatusName
                            + " status. Items can only be updated when the order is PAID or PROCESSING.");
        }

        // First pass: validate every entry (existence, ownership, order
        // linkage, legal transition) WITHOUT mutating anything yet, so the
        // batch is genuinely all-or-nothing.
        List<OrderItem> validatedItems = new ArrayList<>();
        List<ItemStatus> validatedNewStatuses = new ArrayList<>();

        for (ItemStatusBatchEntryDto entry : items) {

            String safeRequestedStatus = entry.getRequestedStatus().trim().toUpperCase();

            if (!"PROCESSING".equals(safeRequestedStatus) && !"SHIPPED".equals(safeRequestedStatus)) {
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "requestedStatus must be PROCESSING or SHIPPED for item " + entry.getItemId());
            }

            OrderItem item = orderItemRepository.findByPkOrderItemIdAndFkOrder_PkOrderId(entry.getItemId(), orderId)
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                            "Order item not found: " + entry.getItemId()));

            if (item.getFkVendor() == null || !item.getFkVendor().getPkVendorId().equals(vendor.getPkVendorId())) {
                throw new ApiException(HttpStatus.FORBIDDEN,
                        "You do not have permission to update item: " + entry.getItemId());
            }

            String currentItemStatusName = item.getFkItemStatus() != null
                    ? item.getFkItemStatus().getStatusName()
                    : null;

            if ("CANCELLED".equals(currentItemStatusName)) {
                throw new ApiException(HttpStatus.CONFLICT, "Cannot update cancelled items");
            }

            if ("SHIPPED".equals(currentItemStatusName)) {
                throw new ApiException(HttpStatus.CONFLICT, "Item already shipped");
            }

            if ("PENDING".equals(currentItemStatusName) && !"PROCESSING".equals(safeRequestedStatus)) {
                throw new ApiException(HttpStatus.CONFLICT,
                        "Cannot skip from PENDING directly to SHIPPED");
            }

            if ("PROCESSING".equals(currentItemStatusName) && !"SHIPPED".equals(safeRequestedStatus)) {
                throw new ApiException(HttpStatus.CONFLICT, "Cannot move backwards in status");
            }

            ItemStatus newItemStatus = itemStatusRepository.findByStatusName(safeRequestedStatus)
                    .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                            safeRequestedStatus + " item status lookup row not found"));

            validatedItems.add(item);
            validatedNewStatuses.add(newItemStatus);
        }

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        for (int i = 0; i < validatedItems.size(); i++) {
            validatedItems.get(i).setFkItemStatus(validatedNewStatuses.get(i));
        }

        // TODO: Batch notification trigger (Week 8).
        // TODO: Check delivery assignment trigger if now fully SHIPPED (Week 6 Day 4).

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * ================================================================
         */
        orderItemRepository.saveAll(validatedItems);

        aggregatedStatusCalculationService.recalculateAggregatedStatus(order);

        List<UUID> updatedIds = validatedItems.stream().map(OrderItem::getPkOrderItemId).collect(Collectors.toList());
        List<OrderItem> reloadedItems = orderItemRepository.findAllById(updatedIds);

        /*
         * ================================================================
         * 4. POST-SAVING DATA SANITIZATION & MASKING
         * ================================================================
         */
        List<OrderItemResponseDto> responseItems = reloadedItems.stream()
                .map(reloadedItem -> OrderItemResponseDto.builder()
                        .orderItemId(reloadedItem.getPkOrderItemId())
                        .orderId(orderId)
                        .productId(reloadedItem.getFkProduct() != null
                                ? reloadedItem.getFkProduct().getPkProductId()
                                : null)
                        .productNameSnapshot(reloadedItem.getProductNameSnapshot())
                        .quantity(reloadedItem.getQuantity())
                        .unitPrice(reloadedItem.getUnitPrice())
                        .lineTotal(reloadedItem.getLineTotal())
                        .itemStatusName(reloadedItem.getFkItemStatus().getStatusName())
                        .updatedAtUtc(reloadedItem.getUpdatedAtUtc())
                        .build())
                .collect(Collectors.toList());

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * ================================================================
         */
        return ItemStatusBatchResponseDto.builder()
                .items(responseItems)
                .updatedCount(responseItems.size())
                .build();
    }

}
