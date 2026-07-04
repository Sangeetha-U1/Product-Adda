package com.productadda.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.CouponDiscountType;

public interface CouponDiscountTypeRepository extends JpaRepository<CouponDiscountType, UUID> {

    Optional<CouponDiscountType> findByDiscountTypeCode(String discountTypeCode);
}