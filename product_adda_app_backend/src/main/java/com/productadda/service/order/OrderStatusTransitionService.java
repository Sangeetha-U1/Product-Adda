package com.productadda.service.order;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
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

import com.productadda.dto.order.AdminOrderStatusUpdateResponseDto;
import com.productadda.dto.order.ItemStatusBatchEntryDto;
import com.productadda.dto.order.ItemStatusBatchResponseDto;
import com.productadda.dto.order.OrderItemResponseDto;

import com.productadda.entity.DeliveryPartner;
import com.productadda.entity.ItemStatus;
import com.productadda.entity.Order;
import com.productadda.entity.OrderItem;
import com.productadda.entity.OrderStatus;
import com.productadda.entity.User;
import com.productadda.entity.UserRole;
import com.productadda.entity.Vendor;

import com.productadda.exception.ApiException;

import com.productadda.repository.DeliveryPartnerRepository;
import com.productadda.repository.ItemStatusRepository;
import com.productadda.repository.OrderItemRepository;
import com.productadda.repository.OrderRepository;
import com.productadda.repository.OrderStatusRepository;
import com.productadda.repository.UserRepository;
import com.productadda.repository.UserRoleRepository;
import com.productadda.repository.VendorRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderStatusTransitionService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final VendorRepository vendorRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final ItemStatusRepository itemStatusRepository;
    private final DeliveryPartnerRepository deliveryPartnerRepository;
    private final AggregatedStatusCalculationService aggregatedStatusCalculationService;

    // Order statuses confirmed as seeded in this project's order_statuses
    // table. "ABANDONED" was referenced in the Day 3 plan text but is not
    // currently seeded, so it is intentionally excluded from this set.
    private static final Set<String> VALID_ORDER_STATUSES = Set.of(
            "PENDING", "CONFIRMED", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED", "RETURNED");

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
        Vendor vendor = resolveAuthenticatedVendor(currentUsername);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));

        OrderItem item = orderItemRepository.findByPkOrderItemIdAndFkOrder_PkOrderId(itemId, orderId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order item not found"));

        if (item.getFkVendor() == null || !item.getFkVendor().getPkVendorId().equals(vendor.getPkVendorId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You do not have permission to update this item");
        }

        String orderStatusName = order.getFkStatus() != null ? order.getFkStatus().getStatusName() : null;

        if (!"CONFIRMED".equals(orderStatusName) && !"PROCESSING".equals(orderStatusName)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Cannot update items when order is in " + orderStatusName
                            + " status. Items can only be updated when the order is CONFIRMED or PROCESSING.");
        }

        String currentItemStatusName = item.getFkItemStatus() != null
                ? item.getFkItemStatus().getStatusName()
                : null;

        validateItemStatusTransition(currentItemStatusName, safeRequestedStatus);

        ItemStatus newItemStatus = itemStatusRepository.findByStatusName(safeRequestedStatus)
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        safeRequestedStatus + " item status lookup row not found"));

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        item.setFkItemStatus(newItemStatus);

        // TODO: Trigger notifications when item transitions to SHIPPED (Week 8).
        // Example: notificationService.sendItemShippedNotification(orderId, itemId,
        // customerId);

        // TODO: Check if order now fully SHIPPED - if so, trigger delivery
        // partner assignment (Week 6 Day 4).
        // Example: if (allItemsShipped)
        // deliveryAssignmentService.triggerAutoAssignment(orderId);

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
        Vendor vendor = resolveAuthenticatedVendor(currentUsername);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));

        String orderStatusName = order.getFkStatus() != null ? order.getFkStatus().getStatusName() : null;

        if (!"CONFIRMED".equals(orderStatusName) && !"PROCESSING".equals(orderStatusName)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Cannot update items when order is in " + orderStatusName
                            + " status. Items can only be updated when the order is CONFIRMED or PROCESSING.");
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

            validateItemStatusTransition(currentItemStatusName, safeRequestedStatus);

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

    /*
     * ================================================================
     * ADMIN FORCE STATUS TRANSITION
     * Description: Allows an authenticated admin to force-transition an
     * order to any valid status, bypassing the normal vendor state
     * machine, except for the terminal DELIVERED and RETURNED states.
     * ================================================================
     */
    @Transactional
    public AdminOrderStatusUpdateResponseDto forceStatusTransition(UUID orderId, String requestedStatus,
            String reason) {

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
        if (requestedStatus == null || requestedStatus.trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "requestedStatus is required");
        }

        String safeRequestedStatus = requestedStatus.trim().toUpperCase();

        if (!VALID_ORDER_STATUSES.contains(safeRequestedStatus)) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "requestedStatus must be one of " + VALID_ORDER_STATUSES);
        }

        String safeReason = (reason == null || reason.trim().isEmpty()) ? null : reason.trim();

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
            throw new ApiException(HttpStatus.FORBIDDEN, "Admin role required to change order status");
        }

        boolean hasAdminPrivileges = userRoles.stream()
                .map(userRole -> userRole.getFkRole().getRoleName())
                .anyMatch(roleName -> "ADMIN".equals(roleName) || "SUPER_ADMIN".equals(roleName));

        if (!hasAdminPrivileges) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Admin role required to change order status");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));

        String previousStatusName = order.getFkStatus() != null ? order.getFkStatus().getStatusName() : null;

        if ("DELIVERED".equals(previousStatusName)) {
            throw new ApiException(HttpStatus.CONFLICT, "Cannot change status of DELIVERED orders");
        }
        if ("RETURNED".equals(previousStatusName)) {
            throw new ApiException(HttpStatus.CONFLICT, "Cannot change status of RETURNED orders");
        }

        OrderStatus newOrderStatus = orderStatusRepository.findByStatusName(safeRequestedStatus)
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        safeRequestedStatus + " order status lookup row not found"));

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        String warning = null;

        if ("CANCELLED".equals(previousStatusName) && !"CANCELLED".equals(safeRequestedStatus)) {
            // Admin can un-cancel for operational needs.
            // TODO: Un-cancelling does NOT currently restore inventory that
            // was released during cancellation (see OrderCancellationService
            // from Day 2). This is a known gap -- inventory re-reservation
            // on un-cancel is not implemented and should be addressed before
            // this override is relied on for real operational recovery.
            warning = "Order was un-cancelled at" + now + "by admin override. Inventory was NOT automatically "
                    + "re-reserved; verify stock manually before proceeding.";
        }

        if ("DELIVERED".equals(safeRequestedStatus)) {
            List<OrderItem> activeItems = orderItemRepository.findByFkOrderAndIsActiveTrue(order);
            boolean anyStillProcessing = activeItems.stream()
                    .anyMatch(item -> item.getFkItemStatus() != null
                            && "PROCESSING".equals(item.getFkItemStatus().getStatusName()));
            if (anyStillProcessing) {
                warning = "Order marked DELIVERED while one or more items are still in PROCESSING status.";
            }
        }

        order.setFkStatus(newOrderStatus);

        boolean activeDeliveriesIncremented = false;

        if ("DELIVERED".equals(safeRequestedStatus) && !"DELIVERED".equals(previousStatusName)
                && order.getFkDeliveryPartner() != null) {

            DeliveryPartner deliveryPartner = order.getFkDeliveryPartner();
            deliveryPartner.setActiveDeliveries(deliveryPartner.getActiveDeliveries() + 1);
            deliveryPartnerRepository.save(deliveryPartner);
            activeDeliveriesIncremented = true;
        }

        // TODO: Persist an audit log entry (event_type =
        // 'ORDER_STATUS_CHANGED', actor, previous/new status, reason,
        // timestamp) once an audit_logs table exists. No such table
        // currently exists in this project's schema, so this is deferred
        // rather than introducing a new table now (same decision made for
        // Day 2's force-cancel endpoint).

        // TODO: Trigger notifications based on status change (Week 8).
        // Example: if ("SHIPPED".equals(safeRequestedStatus))
        // notificationService.sendOrderShippedNotification(orderId);

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * ================================================================
         */
        Order savedOrder = orderRepository.save(order);

        // Reload so updatedAtUtc reflects the DB-trigger value, per the
        // Unified Temporal Alignment standard.
        Order reloadedOrder = orderRepository.findById(savedOrder.getPkOrderId())
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Order disappeared immediately after save"));

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
        return AdminOrderStatusUpdateResponseDto.builder()
                .orderId(reloadedOrder.getPkOrderId())
                .previousStatusName(previousStatusName)
                .newStatusName(reloadedOrder.getFkStatus().getStatusName())
                .reason(safeReason)
                .updatedAtUtc(reloadedOrder.getUpdatedAtUtc())
                .deliveryPartnerActiveDeliveriesIncremented(activeDeliveriesIncremented)
                .warning(warning)
                .build();
    }

    /*
     * ================================================================
     * PRIVATE HELPER: RESOLVE AUTHENTICATED VENDOR
     * Description: Shared DB-level role and profile resolution used by
     * both vendor-facing methods in this service. NOT a mapping helper
     * (Zero-Helper Inline Mapping Rule applies only to entity-to-DTO
     * transformations, not to shared validation/lookup logic).
     * ================================================================
     */
    private Vendor resolveAuthenticatedVendor(String username) {

        User currentUser = userRepository.findByEmail(username)
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

        return vendorRepository.findByFkUser(currentUser)
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN,
                        "Vendor role required to access this endpoint"));
    }

    /*
     * ================================================================
     * PRIVATE HELPER: VALIDATE ITEM STATUS TRANSITION
     * Description: Shared state-machine legality check used by both
     * the single and batch vendor update methods.
     * ================================================================
     */
    private void validateItemStatusTransition(String currentStatusName, String requestedStatusName) {

        if ("CANCELLED".equals(currentStatusName)) {
            throw new ApiException(HttpStatus.CONFLICT, "Cannot update cancelled items");
        }

        if ("SHIPPED".equals(currentStatusName)) {
            throw new ApiException(HttpStatus.CONFLICT, "Item already shipped");
        }

        if ("PENDING".equals(currentStatusName) && !"PROCESSING".equals(requestedStatusName)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Cannot skip from PENDING directly to SHIPPED");
        }

        if ("PROCESSING".equals(currentStatusName) && !"SHIPPED".equals(requestedStatusName)) {
            throw new ApiException(HttpStatus.CONFLICT, "Cannot move backwards in status");
        }
    }
}
