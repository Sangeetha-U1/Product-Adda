package com.productadda.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.Coupon;
import com.productadda.entity.CouponUsageHistory;
import com.productadda.entity.User;

public interface CouponUsageHistoryRepository extends JpaRepository<CouponUsageHistory, UUID> {

    long countByFkCouponAndFkUser(Coupon coupon, User user);

    List<CouponUsageHistory> findByFkUserOrderByCreatedAtUtcDesc(User user);
}