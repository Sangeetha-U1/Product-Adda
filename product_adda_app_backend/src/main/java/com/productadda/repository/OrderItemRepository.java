package com.productadda.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.OrderItem;

public interface OrderItemRepository
        extends JpaRepository<OrderItem, UUID> {

}