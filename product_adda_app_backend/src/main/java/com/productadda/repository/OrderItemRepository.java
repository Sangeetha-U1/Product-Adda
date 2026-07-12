package com.productadda.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.productadda.entity.Order;
import com.productadda.entity.OrderItem;
import com.productadda.entity.Vendor;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

        // Conveniently fetch every item tied to a specific order
        List<OrderItem> findByFkOrder(Order fkOrder);

        /**
         * Fetches all items/snapshots bound to a historical order.
         */
        List<OrderItem> findByFkOrderAndIsActiveTrue(Order order);

        // ==========================================
        // 1. TOTAL ORDER LINE ITEMS BY VENDOR
        // Description: Counts every individual order
        // line item directly linked to this vendor.
        // ==========================================
        long countByFkVendor(Vendor vendor);

        // ==========================================
        // 2. DISTINCT ORDERS BY VENDOR
        // Description: Counts unique parent orders
        // that contain at least one product from
        // this vendor's catalogue.
        // ==========================================
        @Query("SELECT COUNT(DISTINCT oi.fkOrder.pkOrderId) FROM OrderItem oi WHERE oi.fkVendor = :vendor")
        long countDistinctOrdersByVendor(@Param("vendor") Vendor vendor);

        // ==========================================
        // 3. TOTAL ITEM COUNT FOR ONE ORDER
        // Description: Counts active line items belonging to a single
        // order, used to populate itemCount on CustomerOrderListDto.
        // ==========================================
        long countByFkOrderAndIsActiveTrue(Order order);

        // ==========================================
        // 4. VENDOR-SCOPED ITEMS WITHIN ONE ORDER
        // Description: Returns only this vendor's items within a given
        // order, enforcing vendor isolation on multi-vendor orders.
        // ==========================================
        List<OrderItem> findByFkOrderAndFkVendorAndIsActiveTrue(Order order, Vendor vendor);

        // ==========================================
        // 5. DISTINCT PAGINATED ORDERS FOR A VENDOR
        // Description: Returns the distinct parent Order entities that
        // contain at least one active item from this vendor, paginated
        // at the order level (not the item level).
        // ==========================================
        @Query(value = "SELECT DISTINCT oi.fkOrder FROM OrderItem oi " +
                        "WHERE oi.fkVendor.pkVendorId = :vendorId AND oi.isActive = true", countQuery = "SELECT COUNT(DISTINCT oi.fkOrder) FROM OrderItem oi "
                                        +
                                        "WHERE oi.fkVendor.pkVendorId = :vendorId AND oi.isActive = true")
        Page<Order> findDistinctOrdersByVendorId(@Param("vendorId") UUID vendorId, Pageable pageable);

        // ==========================================
        // 6. DISTINCT ORDER IDS FOR A VENDOR (ADMIN FILTER)
        // Description: Used by AdminOrderRetrievalService to narrow the
        // admin order query down to orders containing this vendor's items,
        // when the optional vendorId filter is supplied.
        // ==========================================
        @Query("SELECT DISTINCT oi.fkOrder.pkOrderId FROM OrderItem oi WHERE oi.fkVendor.pkVendorId = :vendorId")
        List<UUID> findDistinctOrderIdsByVendorId(@Param("vendorId") UUID vendorId);

        // ==========================================
        // 7. SINGLE ITEM SCOPED TO A SPECIFIC ORDER
        // Description: Fetches one order item by its own primary key,
        // additionally verifying it belongs to the given order, in a
        // single query rather than a separate manual ownership check.
        // ==========================================
        Optional<OrderItem> findByPkOrderItemIdAndFkOrder_PkOrderId(UUID orderItemId, UUID orderId);

        // ==========================================
        // BATCH ITEMS RETRIEVAL FOR ORDER PAGE
        // Description: Fetches all active line items belonging to a collection
        // of orders in a single database round-trip to avoid N+1 queries.
        // ==========================================
        List<OrderItem> findByFkOrderInAndIsActiveTrue(List<Order> orders);
}