package com.productadda.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.PaymentReconciliationLog;

public interface PaymentReconciliationLogRepository extends JpaRepository<PaymentReconciliationLog, UUID> {

}
