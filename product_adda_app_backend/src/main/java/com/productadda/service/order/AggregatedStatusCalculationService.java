package com.productadda.service.order;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.entity.Order;
import com.productadda.entity.OrderItem;
import com.productadda.entity.OrderStatus;

import com.productadda.exception.ApiException;

import com.productadda.repository.OrderItemRepository;
import com.productadda.repository.OrderRepository;
import com.productadda.repository.OrderStatusRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AggregatedStatusCalculationService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final OrderStatusRepository orderStatusRepository;

    /*
     * ================================================================
     * RECALCULATE AGGREGATED ORDER STATUS
     * Description: Derives the order-level status from the collective
     * state of its active (non-CANCELLED) line items, and persists the
     * update if it differs from the order's current status. Only ever
     * called from within the vendor item-update workflow, so the
     * aggregated outcome is always either PROCESSING or SHIPPED --
     * this method never assigns PENDING, DELIVERED, CANCELLED, or
     * RETURNED, since those are driven by other workflows (checkout,
     * admin override, cancellation) and not by item-level aggregation.
     *
     * Aggregation rule (CANCELLED items excluded entirely):
     * - If there are no active (non-CANCELLED) items at all, the
     * order status is left unchanged (edge case: fully cancelled
     * order, nothing to aggregate).
     * - If ALL active items are SHIPPED, the order becomes SHIPPED.
     * - Otherwise (any mix of PENDING/PROCESSING/SHIPPED among the
     * active items), the order becomes PROCESSING.
     *
     * Runs inside the caller's existing transaction (default REQUIRED
     * propagation).
     * ================================================================
     */

    @Transactional
    public String recalculateAggregatedStatus(Order order) {
        // TODO: Expand aggregation rules when DELIVERY and RETURN workflows are
        // implemented.
        // Current implementation intentionally aggregates only PROCESSING and SHIPPED.
        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        if (order == null || order.getPkOrderId() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Order is required to recalculate aggregated status");
        }

        // ==========================================
        // 1.2 CONTEXT AUTHENTICATION
        // ==========================================
        // N/A - internal service invoked only by OrderStatusTransitionService,
        // which has already performed authentication and role checks.

        // ==========================================
        // 1.3 DATABASE LOOKUP VALIDATION
        // ==========================================
        List<OrderItem> allActiveItems = orderItemRepository.findByFkOrderAndIsActiveTrue(order);

        // Exclude CANCELLED items from the aggregation calculation entirely.
        List<OrderItem> nonCancelledItems = allActiveItems.stream()
                .filter(item -> item.getFkItemStatus() == null
                        || !"CANCELLED".equals(item.getFkItemStatus().getStatusName()))
                .toList();

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        if (nonCancelledItems.isEmpty()) {
            // Edge case: every item on this order is CANCELLED. Nothing
            // active to aggregate from, so the order status is left as-is.
            return order.getFkStatus() != null ? order.getFkStatus().getStatusName() : null;
        }

        boolean allShipped = nonCancelledItems.stream()
                .allMatch(item -> item.getFkItemStatus() != null
                        && "SHIPPED".equals(item.getFkItemStatus().getStatusName()));

        String aggregatedStatusName = allShipped ? "SHIPPED" : "PROCESSING";

        String currentStatusName = order.getFkStatus() != null ? order.getFkStatus().getStatusName() : null;

        if (aggregatedStatusName.equals(currentStatusName)) {
            // No change needed.
            return currentStatusName;
        }

        OrderStatus newOrderStatus = orderStatusRepository.findByStatusName(aggregatedStatusName)
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        aggregatedStatusName + " order status lookup row not found"));

        order.setFkStatus(newOrderStatus);

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * ================================================================
         */
        orderRepository.save(order);

        /*
         * ================================================================
         * 4. POST-SAVING DATA SANITIZATION & MASKING
         * Reason: No response payload to sanitize -- this method returns
         * a plain status name string, consumed by the caller's own
         * response mapping section.
         * ================================================================
         */

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * Reason: N/A - internal service, not directly bound to a REST
         * endpoint.
         * ================================================================
         */
        return aggregatedStatusName;
    }
}
