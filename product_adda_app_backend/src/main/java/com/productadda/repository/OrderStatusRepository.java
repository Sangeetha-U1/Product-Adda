package com.productadda.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.OrderStatus;

public interface OrderStatusRepository
        extends JpaRepository<OrderStatus, UUID> {

    Optional<OrderStatus> findByStatusName(String statusName);

}