package com.productadda.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.PaymentGateway;

public interface PaymentGatewayRepository
        extends JpaRepository<PaymentGateway, UUID> {

    Optional<PaymentGateway> findByGatewayName(String gatewayName);
}