package com.productadda.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.DeliveryPartner;

public interface DeliveryPartnerRepository extends JpaRepository<DeliveryPartner, UUID> {
    
    Optional<DeliveryPartner> findByEmail(String email);
    
    Optional<DeliveryPartner> findByPhone(String phone);
}