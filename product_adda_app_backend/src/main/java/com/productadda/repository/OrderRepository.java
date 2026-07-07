package com.productadda.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.productadda.entity.Order;
import com.productadda.entity.User;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByFkUserAndIsActiveTrueOrderByCreatedAtUtcDesc(User user);

    Optional<Order> findByOrderNumber(String orderNumber);

    Optional<Order> findByIdempotencyKey(UUID idempotencyKey);

    Page<Order> findByFkUserAndIsActiveTrue(User user, Pageable pageable);

}