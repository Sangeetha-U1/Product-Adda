package com.productadda.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.productadda.entity.Order;
import com.productadda.entity.OrderItem;
import com.productadda.entity.Vendor;

public interface OrderItemRepository
                extends JpaRepository<OrderItem, UUID> {

        // Conveniently fetch every item tied to a specific order
        List<OrderItem> findByFkOrder(Order fkOrder);

        /**
         * Fetches all items/snapshots bound to a historical order.
         */
        List<OrderItem> findByFkOrderAndIsActiveTrue(Order order);

        // ==========================================
        // 1. TOTAL ORDER LINE ITEMS BY VENDOR
        // Description: Counts every individual order
        // line item linked to this vendor's products.
        // ==========================================
        long countByFkProductFkVendor(Vendor vendor);

        // ==========================================
        // 2. DISTINCT ORDERS BY VENDOR
        // Description: Counts unique parent orders
        // that contain at least one product from
        // this vendor's catalogue.
        // ==========================================
        @Query("SELECT COUNT(DISTINCT oi.fkOrder.pkOrderId) FROM OrderItem oi WHERE oi.fkProduct.fkVendor = :vendor")
        long countDistinctOrdersByVendor(@Param("vendor") Vendor vendor);
}