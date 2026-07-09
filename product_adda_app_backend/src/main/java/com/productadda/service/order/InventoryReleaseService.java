package com.productadda.service.order;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.entity.Inventory;
import com.productadda.entity.Order;
import com.productadda.entity.OrderItem;

import com.productadda.exception.ApiException;

import com.productadda.repository.InventoryRepository;
import com.productadda.repository.OrderItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryReleaseService {

    private final OrderItemRepository orderItemRepository;
    private final InventoryRepository inventoryRepository;

    /*
     * ================================================================
     * RELEASE INVENTORY FOR ORDER
     * Description: For every active line item on a cancelled order,
     * releases the reserved stock back to available stock. Runs inside
     * the caller's existing transaction (default REQUIRED propagation),
     * so inventory changes and order-status changes commit or roll
     * back together atomically.
     * ================================================================
     */
    @Transactional
    public void releaseInventoryForOrder(Order order) {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        if (order == null || order.getPkOrderId() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Order is required to release inventory");
        }

        // ==========================================
        // 1.2 CONTEXT AUTHENTICATION
        // ==========================================
        // N/A - This is an internal service invoked only by
        // OrderCancellationService, which has already performed the
        // authentication and role checks for the incoming request.

        // ==========================================
        // 1.3 DATABASE LOOKUP VALIDATION
        // ==========================================
        List<OrderItem> items = orderItemRepository.findByFkOrderAndIsActiveTrue(order);

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        for (OrderItem item : items) {

            Inventory inventory = inventoryRepository.findByFkProduct(item.getFkProduct())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                            "Inventory record not found for product on order item"));

            int releasedReservedQuantity = inventory.getReservedQuantity() - item.getQuantity();

            // Guard against negative reserved_quantity in case of prior
            // manual data correction or partial-release edge cases.
            inventory.setReservedQuantity(Math.max(releasedReservedQuantity, 0));
            inventory.setAvailableQuantity(inventory.getAvailableQuantity() + item.getQuantity());

            /*
             * ================================================================
             * 3. DB SAVING SECTION
             * ================================================================
             */
            inventoryRepository.save(inventory);
        }

        /*
         * ================================================================
         * 4. POST-SAVING DATA SANITIZATION & MASKING
         * Reason: No response payload -- this method returns void and is
         * consumed only by OrderCancellationService's own response
         * mapping section.
         * ================================================================
         */

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * Reason: N/A - internal void-returning service, not directly
         * bound to a REST endpoint. The caller (OrderCancellationService)
         * performs its own response mapping.
         * ================================================================
         */
    }
}
