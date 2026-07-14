package com.productadda.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.DeliveryOtp;
import com.productadda.entity.Order;

public interface DeliveryOtpRepository extends JpaRepository<DeliveryOtp, UUID> {
    Optional<DeliveryOtp> findByFkOrder(Order order);
}