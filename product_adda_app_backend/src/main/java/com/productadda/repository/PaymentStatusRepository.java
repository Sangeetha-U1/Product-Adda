package com.productadda.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.PaymentStatus;

public interface PaymentStatusRepository
        extends JpaRepository<PaymentStatus, UUID> {

    Optional<PaymentStatus> findByStatusName(String statusName);

}