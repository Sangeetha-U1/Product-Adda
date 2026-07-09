package com.productadda.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.DeliveryPartnerStatus;

public interface DeliveryPartnerStatusRepository extends JpaRepository<DeliveryPartnerStatus, UUID> {

    Optional<DeliveryPartnerStatus> findByStatusName(String statusName);

}