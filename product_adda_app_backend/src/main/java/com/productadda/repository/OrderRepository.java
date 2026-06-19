package com.productadda.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.productadda.entity.Order;
import com.productadda.entity.User;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByFkUserAndIsActiveTrueOrderByCreatedAtUtcDesc(User user);
}