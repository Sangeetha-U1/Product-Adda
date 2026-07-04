package com.productadda.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.CouponStatus;

public interface CouponStatusRepository extends JpaRepository<CouponStatus, UUID> {

    Optional<CouponStatus> findByStatusCode(String statusCode);
}