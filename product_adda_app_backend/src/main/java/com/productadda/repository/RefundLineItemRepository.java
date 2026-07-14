package com.productadda.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.Refund;
import com.productadda.entity.RefundLineItem;

/*
 * ================================================================
 * NEW REPOSITORY (Week 7, Day 3): RefundLineItemRepository
 * ================================================================
 */
public interface RefundLineItemRepository extends JpaRepository<RefundLineItem, UUID> {

        List<RefundLineItem> findByFkRefund(Refund refund);
}
