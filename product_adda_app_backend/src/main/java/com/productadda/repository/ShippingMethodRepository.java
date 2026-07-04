package com.productadda.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.ShippingMethod;

public interface ShippingMethodRepository extends JpaRepository<ShippingMethod, UUID> {

    List<ShippingMethod> findByIsActiveTrue();

    // Changed from findByMethodCode to match shippingMethodName
    Optional<ShippingMethod> findByShippingMethodName(String shippingMethodName);
}