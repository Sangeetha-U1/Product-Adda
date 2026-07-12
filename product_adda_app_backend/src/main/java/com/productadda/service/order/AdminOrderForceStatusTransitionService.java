package com.productadda.service.order;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.order.AdminOrderStatusUpdateResponseDto;

import com.productadda.entity.DeliveryPartner;
import com.productadda.entity.Order;
import com.productadda.entity.OrderItem;
import com.productadda.entity.OrderStatus;
import com.productadda.entity.User;
import com.productadda.entity.UserRole;

import com.productadda.exception.ApiException;

import com.productadda.repository.DeliveryPartnerRepository;
import com.productadda.repository.OrderItemRepository;
import com.productadda.repository.OrderRepository;
import com.productadda.repository.OrderStatusRepository;
import com.productadda.repository.UserRepository;
import com.productadda.repository.UserRoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminOrderForceStatusTransitionService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final DeliveryPartnerRepository deliveryPartnerRepository;

    // private final InvoiceGenerationService invoiceGenerationService;

    // Order statuses confirmed as seeded in this project's order_statuses
    // table. "ABANDONED" was referenced in the text but is not
    // currently seeded, so it is intentionally excluded from this set.
    private static final Set<String> VALID_ORDER_STATUSES = Set.of(
            "PENDING", "CONFIRMED", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED", "RETURNED", "OUT_FOR_DELIVERY",
            "PAID");

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

        // TODO: Check this logic later
        // auto-generate invoice when order
        // reaches CONFIRMED for the first time.

        // if ("CONFIRMED".equals(safeRequestedStatus) &&
        // !"CONFIRMED".equals(previousStatusName)) {
        // invoiceGenerationService.generateInvoice(order.getPkOrderId());
        // }

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

}