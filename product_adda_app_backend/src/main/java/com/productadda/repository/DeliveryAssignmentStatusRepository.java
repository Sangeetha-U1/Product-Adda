package com.productadda.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.DeliveryAssignmentStatus;

public interface DeliveryAssignmentStatusRepository extends JpaRepository<DeliveryAssignmentStatus, UUID> {

    Optional<DeliveryAssignmentStatus> findByStatusName(String statusName);
}