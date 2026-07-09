package com.productadda.service.order;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.order.OrderCancellationResponseDto;
import com.productadda.entity.Cart;
import com.productadda.entity.CartStatus;
import com.productadda.entity.DeliveryAssignment;
import com.productadda.entity.DeliveryAssignmentStatus;
import com.productadda.entity.ItemStatus;
import com.productadda.entity.Order;
import com.productadda.entity.OrderItem;
import com.productadda.entity.OrderStatus;
import com.productadda.entity.User;
import com.productadda.entity.UserRole;

import com.productadda.exception.ApiException;

import com.productadda.repository.DeliveryAssignmentRepository;
import com.productadda.repository.DeliveryAssignmentStatusRepository;
import com.productadda.repository.ItemStatusRepository;
import com.productadda.repository.OrderItemRepository;
import com.productadda.repository.OrderRepository;
import com.productadda.repository.OrderStatusRepository;
import com.productadda.repository.UserRepository;
import com.productadda.repository.UserRoleRepository;
import com.productadda.repository.CartStatusRepository;
import com.productadda.repository.CartRepository;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderCancellationService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final ItemStatusRepository itemStatusRepository;
    private final DeliveryAssignmentRepository deliveryAssignmentRepository;
    private final DeliveryAssignmentStatusRepository deliveryAssignmentStatusRepository;
    private final CartStatusRepository cartStatusRepository;
    private final CartRepository cartRepository;

    private final InventoryReleaseService inventoryReleaseService;

    /*
     * ================================================================
     * CANCEL USEr ORDER
     * Description: Allows the authenticated user to cancel their
     * own order, restricted to PENDING or CONFIRMED status only.
     * Triggers immediate inventory release. Refund workflow deferred
     * to Week 7 (see TODO below).
     * ================================================================
     */

    @Transactional
    public OrderCancellationResponseDto cancelUserOrder(UUID orderId, String cancellationReason) {

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

        // Blank/omitted reason falls back to a default explanatory string.
        String safeCancellationReason = (cancellationReason == null || cancellationReason.trim().isEmpty())
                ? "User requested cancellation"
                : cancellationReason.trim();

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
            throw new ApiException(HttpStatus.FORBIDDEN, "User role required to access this endpoint");
        }

        boolean hasUserRole = userRoles.stream()
                .map(userRole -> userRole.getFkRole().getRoleName())
                .anyMatch(roleName -> "USER".equals(roleName));

        if (!hasUserRole) {
            throw new ApiException(HttpStatus.FORBIDDEN, "User role required to access this endpoint");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));

        if (order.getFkUser() == null || !order.getFkUser().getPkUserId().equals(currentUser.getPkUserId())) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "You do not have permission to cancel this order");
        }

        String currentStatusName = order.getFkStatus() != null ? order.getFkStatus().getStatusName() : null;

        boolean isCancellableStatus = "PENDING".equals(currentStatusName) || "CONFIRMED".equals(currentStatusName);

        if (!isCancellableStatus) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Cannot cancel orders in " + currentStatusName
                            + " status. Only PENDING or CONFIRMED orders can be cancelled by users.");
        }

        OrderStatus cancelledOrderStatus = orderStatusRepository.findByStatusName("CANCELLED")
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "CANCELLED order status lookup row not found"));

        ItemStatus cancelledItemStatus = itemStatusRepository.findByStatusName("CANCELLED")
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "CANCELLED item status lookup row not found"));

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        inventoryReleaseService.releaseInventoryForOrder(order);

        order.setFkStatus(cancelledOrderStatus);
        order.setCancelledBy("USER");
        order.setCancellationReason(safeCancellationReason);
        order.setCancelledAtUtc(now);

        List<OrderItem> orderItems = orderItemRepository.findByFkOrderAndIsActiveTrue(order);
        for (OrderItem item : orderItems) {
            item.setFkItemStatus(cancelledItemStatus);
            item.setCancelledAtUtc(now);
        }

        // Update carts.fk_cart_status_id to 'ABANDONED' if order.getFkCart() is
        // non-null
        if (order.getFkCart() != null) {
            CartStatus abandonedCartStatus = cartStatusRepository.findByStatusCode("ABANDONED")
                    .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "ABANDONED cart status lookup row not found"));

            Cart cart = order.getFkCart();
            cart.setFkCartStatus(abandonedCartStatus);
            cartRepository.save(cart);
        }

        // TODO: Invoke refund workflow service call (Week 7 integration).

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * ================================================================
         */
        Order savedOrder = orderRepository.save(order);
        orderItemRepository.saveAll(orderItems);

        /*
         * ================================================================
         * 4. POST-SAVING DATA SANITIZATION & MASKING
         * ================================================================
         */

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * ================================================================
         */
        return OrderCancellationResponseDto.builder()
                .orderId(savedOrder.getPkOrderId())
                .statusName(savedOrder.getFkStatus().getStatusName())
                .cancelledBy(savedOrder.getCancelledBy())
                .cancelledAtUtc(savedOrder.getCancelledAtUtc())
                .inventoryReleased(true)
                .previousStatusName(null)
                .cancellationReason(savedOrder.getCancellationReason())
                .build();
    }

    /*
     * ================================================================
     * CANCEL CUSTOMER ORDER
     * Description: Allows the authenticated customer to cancel their
     * own order, restricted to PENDING or CONFIRMED status only.
     * Triggers immediate inventory release. Refund workflow deferred
     * to Week 7 (see TODO below).
     * ================================================================
     */
    @Transactional
    public OrderCancellationResponseDto cancelCustomerOrder(UUID orderId, String cancellationReason) {

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

        // Blank/omitted reason is allowed for customer-initiated cancellation
        // and falls back to a default explanatory string.
        String safeCancellationReason = (cancellationReason == null || cancellationReason.trim().isEmpty())
                ? "Customer requested cancellation"
                : cancellationReason.trim();

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

        // Zero-Trust role re-check, matching the pattern already applied to
        // CustomerOrderRetrievalService.
        List<UserRole> userRoles = userRoleRepository.findByFkUser(currentUser);

        if (userRoles.isEmpty()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Customer role required to access this endpoint");
        }

        boolean hasCustomerRole = userRoles.stream()
                .map(userRole -> userRole.getFkRole().getRoleName())
                .anyMatch(roleName -> "CUSTOMER".equals(roleName));

        if (!hasCustomerRole) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Customer role required to access this endpoint");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));

        if (order.getFkUser() == null || !order.getFkUser().getPkUserId().equals(currentUser.getPkUserId())) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "You do not have permission to cancel this order");
        }

        String currentStatusName = order.getFkStatus() != null ? order.getFkStatus().getStatusName() : null;

        boolean isCancellableStatus = "PENDING".equals(currentStatusName) || "CONFIRMED".equals(currentStatusName);

        if (!isCancellableStatus) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Cannot cancel orders in " + currentStatusName
                            + " status. Only PENDING or CONFIRMED orders can be cancelled by customers.");
        }

        OrderStatus cancelledOrderStatus = orderStatusRepository.findByStatusName("CANCELLED")
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "CANCELLED order status lookup row not found"));

        ItemStatus cancelledItemStatus = itemStatusRepository.findByStatusName("CANCELLED")
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "CANCELLED item status lookup row not found"));

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        inventoryReleaseService.releaseInventoryForOrder(order);

        order.setFkStatus(cancelledOrderStatus);
        order.setCancelledBy("CUSTOMER");
        order.setCancellationReason(safeCancellationReason);
        order.setCancelledAtUtc(now);

        List<OrderItem> orderItems = orderItemRepository.findByFkOrderAndIsActiveTrue(order);
        for (OrderItem item : orderItems) {
            item.setFkItemStatus(cancelledItemStatus);
            item.setCancelledAtUtc(now);
        }

        if (order.getFkCart() != null) {
            CartStatus abandonedCartStatus = cartStatusRepository.findByStatusCode("ABANDONED")
                    .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "ABANDONED cart status lookup row not found"));

            Cart cart = order.getFkCart();
            cart.setFkCartStatus(abandonedCartStatus);
            cartRepository.save(cart);
        }

        // TODO: Invoke refund workflow service call (Week 7 integration).
        // Example: refundWorkflowService.initiateRefund(orderId,
        // currentUser.getPkUserId());

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * ================================================================
         */
        Order savedOrder = orderRepository.save(order);
        orderItemRepository.saveAll(orderItems);

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
        return OrderCancellationResponseDto.builder()
                .orderId(savedOrder.getPkOrderId())
                .statusName(savedOrder.getFkStatus().getStatusName())
                .cancelledBy(savedOrder.getCancelledBy())
                .cancelledAtUtc(savedOrder.getCancelledAtUtc())
                .inventoryReleased(true)
                .previousStatusName(null)
                .cancellationReason(savedOrder.getCancellationReason())
                .build();

    }

    /*
     * ================================================================
     * FORCE CANCEL ORDER (ADMIN)
     * Description: Allows an authenticated admin to force-cancel any
     * order regardless of status, except DELIVERED or RETURNED.
     * Requires a non-blank cancellation reason. Also rejects any
     * pending delivery assignment tied to the order. Refund workflow
     * and audit-log persistence deferred (see TODOs below).
     * ================================================================
     */
    @Transactional
    public OrderCancellationResponseDto forceCancelOrder(UUID orderId, String cancellationReason) {

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

        if (cancellationReason == null || cancellationReason.trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "cancellationReason is required for admin force-cancellation");
        }

        String safeCancellationReason = cancellationReason.trim();

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

        // Zero-Trust role re-check, matching the pattern already applied to
        // AdminOrderRetrievalService.
        List<UserRole> userRoles = userRoleRepository.findByFkUser(currentUser);

        if (userRoles.isEmpty()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Admin role required to force-cancel orders");
        }

        boolean hasAdminPrivileges = userRoles.stream()
                .map(userRole -> userRole.getFkRole().getRoleName())
                .anyMatch(roleName -> "ADMIN".equals(roleName) || "SUPER_ADMIN".equals(roleName));

        if (!hasAdminPrivileges) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Admin role required to force-cancel orders");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));

        String previousStatusName = order.getFkStatus() != null ? order.getFkStatus().getStatusName() : null;

        // NOTE: "RETURNED" was not confirmed to exist in this project's
        // order_statuses seed data (only CANCELLED was explicitly
        // confirmed). If "RETURNED" is never actually assigned to any
        // order under the current feature set, this comparison is
        // harmless (it simply never matches), but is left here as
        // written in the Day 2 plan and should be verified once a
        // return-flow exists.
        boolean isBlockedStatus = "DELIVERED".equals(previousStatusName) || "RETURNED".equals(previousStatusName);

        if (isBlockedStatus) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Cannot force-cancel orders in " + previousStatusName + " status");
        }

        OrderStatus cancelledOrderStatus = orderStatusRepository.findByStatusName("CANCELLED")
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "CANCELLED order status lookup row not found"));

        ItemStatus cancelledItemStatus = itemStatusRepository.findByStatusName("CANCELLED")
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "CANCELLED item status lookup row not found"));

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        inventoryReleaseService.releaseInventoryForOrder(order);

        order.setFkStatus(cancelledOrderStatus);
        order.setCancelledBy("ADMIN");
        order.setCancellationReason(safeCancellationReason);
        order.setCancelledAtUtc(now);

        List<OrderItem> orderItems = orderItemRepository.findByFkOrderAndIsActiveTrue(order);
        for (OrderItem item : orderItems) {
            item.setFkItemStatus(cancelledItemStatus);
            item.setCancelledAtUtc(now);
        }

        List<DeliveryAssignment> assignments = deliveryAssignmentRepository.findByFkOrder_PkOrderId(orderId);

        if (!assignments.isEmpty()) {

            DeliveryAssignmentStatus rejectedAssignmentStatus = deliveryAssignmentStatusRepository
                    .findByStatusName("REJECTED")
                    .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "REJECTED delivery assignment status lookup row not found"));

            for (DeliveryAssignment assignment : assignments) {
                assignment.setFkAssignmentStatus(rejectedAssignmentStatus);
                assignment.setRejectionReason("Order force-cancelled by admin: " + safeCancellationReason);
                assignment.setRejectedAt(now);
                // TODO: Notify delivery partner (Week 8 notifications).
            }
            deliveryAssignmentRepository.saveAll(assignments);
        }

        // TODO: Persist an audit log entry (event_type =
        // 'ORDER_FORCE_CANCELLED', actor, reason, previous/new status,
        // timestamp) once an audit_logs table exists. No such table
        // currently exists in this project's schema; per instruction,
        // this is deferred rather than introducing a new table now.

        // TODO: Invoke refund workflow service call (Week 7 integration),
        // with special handling for admin-initiated cancellations that
        // may require manual approval.
        // Example: refundWorkflowService.initiateRefund(orderId, null);

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * ================================================================
         */
        Order savedOrder = orderRepository.save(order);
        orderItemRepository.saveAll(orderItems);

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
        return OrderCancellationResponseDto.builder()
                .orderId(savedOrder.getPkOrderId())
                .statusName(savedOrder.getFkStatus().getStatusName())
                .cancelledBy(savedOrder.getCancelledBy())
                .cancelledAtUtc(savedOrder.getCancelledAtUtc())
                .inventoryReleased(true)
                .previousStatusName(previousStatusName)
                .cancellationReason(savedOrder.getCancellationReason())
                .build();
    }
}
