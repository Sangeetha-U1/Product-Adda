package com.productadda.service.notifications;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.productadda.entity.Order;
import com.productadda.entity.OrderItem;
import com.productadda.entity.User;

import com.productadda.exception.ApiException;

import com.productadda.repository.OrderItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecipientResolverServiceGetOrderVendorUsers {

    private final OrderItemRepository orderItemRepository;

    /*
     * ================================================================
     * GET ORDER VENDOR USERS
     * Description: Resolves the distinct set of vendor-owning User
     * accounts for an order, derived from order_items.fk_vendor_id via
     * each vendor's fk_user_id. A multi-vendor order returns multiple
     * users; an order with only cancelled items returns an empty list.
     * distinct() relies on Hibernate returning the same Java object
     * reference for the same entity within one persistence context,
     * which holds here since this always runs inside the caller's
     * existing transaction.
     * ================================================================
     */
    public List<User> getOrderVendorUsers(Order order) {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */
        if (order == null || order.getPkOrderId() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Order is required to resolve vendor recipients");
        }

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        List<OrderItem> items = orderItemRepository.findByFkOrderAndIsActiveTrue(order);

        List<User> vendorUsers = items.stream()
                .map(OrderItem::getFkVendor)
                .filter(vendor -> vendor != null && vendor.getFkUser() != null)
                .map(vendor -> vendor.getFkUser())
                .distinct()
                .toList();

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * Reason: Read-only resolution, no mutations performed.
         * ================================================================
         */

        /*
         * ================================================================
         * 4. POST-SAVING DATA SANITIZATION & MASKING
         * Reason: N/A - see section 3.
         * ================================================================
         */

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * ================================================================
         */
        return vendorUsers;
    }
}
