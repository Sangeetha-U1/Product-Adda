package com.productadda.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.Order;
import com.productadda.entity.OrderItem;

public interface OrderItemRepository
                extends JpaRepository<OrderItem, UUID> {

        // Conveniently fetch every item tied to a specific order
        List<OrderItem> findByFkOrder(Order fkOrder);

        /**
         * Fetches all items/snapshots bound to a historical order.
         */
        List<OrderItem> findByFkOrderAndIsActiveTrue(Order order);
}