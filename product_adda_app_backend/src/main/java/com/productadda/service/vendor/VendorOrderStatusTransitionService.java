package com.productadda.service.vendor;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.productadda.service.order.AggregatedStatusCalculationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VendorOrderStatusTransitionService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final VendorRepository vendorRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ItemStatusRepository itemStatusRepository;

    // TODO: when vendor updates to SHIPPED then we need to add logic related to
    // assign delivery partner and or it can be a deliver partner can look up orders
    // nearst his own choice so that is good right instead of autoassigning yeh i
    // want this where delivery partner GET orders low details only necessary order
    // details to be show
    // if delivery partner manually accepts then this service file can just update
    // status to either procesing when vendor accepts and shipped when it sready to
    // ship right

    private final AggregatedStatusCalculationService aggregatedStatusCalculationService;

    /*
     * ================================================================
     * UPDATE VENDOR ITEM STATUS (SINGLE)
     * Description: Transitions one order item through the vendor
     * fulfillment workflow (PENDING -> PROCESSING -> SHIPPED), then
     * recalculates the order-level aggregated status.
     * ================================================================
     */
    @Transactional
    public OrderItemResponseDto updateVendorItemStatus(UUID orderId, UUID itemId, String requestedStatus) {

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
        if (itemId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "itemId is required");
        }
        if (requestedStatus == null || requestedStatus.trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "requestedStatus is required");
        }

        String safeRequestedStatus = requestedStatus.trim().toUpperCase();

        if (!"PROCESSING".equals(safeRequestedStatus) && !"SHIPPED".equals(safeRequestedStatus)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "requestedStatus must be PROCESSING or SHIPPED");
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

        OrderItem item = orderItemRepository.findByPkOrderItemIdAndFkOrder_PkOrderId(itemId, orderId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order item not found"));

        if (item.getFkVendor() == null || !item.getFkVendor().getPkVendorId().equals(vendor.getPkVendorId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You do not have permission to update this item");
        }

        String orderStatusName = order.getFkStatus() != null ? order.getFkStatus().getStatusName() : null;

        if (!"PAID".equals(orderStatusName) && !"PROCESSING".equals(orderStatusName)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Cannot update items when order is in " + orderStatusName
                            + " status. Items can only be updated when the order is PAID or PROCESSING.");
        }

        String currentItemStatusName = item.getFkItemStatus() != null
                ? item.getFkItemStatus().getStatusName()
                : null;

        // ==========================================
        // 1.4
        // ==========================================

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

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        item.setFkItemStatus(newItemStatus);

        // TODO: Trigger notifications when item transitions to SHIPPED.
        // Example: notificationService.sendItemShippedNotification(orderId, itemId,
        // customerId);

        // TODO: When the aggregated order status becomes SHIPPED,
        // the order becomes visible in
        // GET /api/delivery-partners/available-orders.
        // A delivery partner may manually claim the order.

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * ================================================================
         */
        orderItemRepository.save(item);

        aggregatedStatusCalculationService.recalculateAggregatedStatus(order);

        // Reload item so updatedAtUtc reflects the DB-trigger value, per
        // the Unified Temporal Alignment standard.
        OrderItem reloadedItem = orderItemRepository.findById(item.getPkOrderItemId())
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Order item disappeared immediately after save"));

        /*
         * ================================================================
         * 4. POST-SAVING DATA SANITIZATION & MASKING
         * ================================================================
         */
        // No sensitive fields to strip; response is already a minimal
        // confirmation shape.

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * ================================================================
         */
        return OrderItemResponseDto.builder()
                .orderItemId(reloadedItem.getPkOrderItemId())
                .orderId(orderId)
                .productId(reloadedItem.getFkProduct() != null ? reloadedItem.getFkProduct().getPkProductId() : null)
                .productNameSnapshot(reloadedItem.getProductNameSnapshot())
                .quantity(reloadedItem.getQuantity())
                .unitPrice(reloadedItem.getUnitPrice())
                .lineTotal(reloadedItem.getLineTotal())
                .itemStatusName(reloadedItem.getFkItemStatus().getStatusName())
                .updatedAtUtc(reloadedItem.getUpdatedAtUtc())
                .build();
    }

}
