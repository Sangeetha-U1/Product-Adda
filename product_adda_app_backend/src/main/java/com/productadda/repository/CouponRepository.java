package com.productadda.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.Coupon;

public interface CouponRepository extends JpaRepository<Coupon, UUID> {
    
    Optional<Coupon> findByCouponCodeAndIsActiveTrue(String couponCode);
}