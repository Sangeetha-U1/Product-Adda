package com.productadda.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.Payment;
import com.productadda.entity.PaymentAuditLog;

/*
 * ================================================================
 * NEW REPOSITORY : PaymentAuditLogRepository
 * Backs the immutable payment_audit_logs table.
 * ================================================================
 */
public interface PaymentAuditLogRepository extends JpaRepository<PaymentAuditLog, UUID> {

        // Secondary idempotency guard: checks whether a given action string
        // was already logged for a payment, before re-processing a webhook.
        boolean existsByFkPaymentAndAction(Payment fkPayment, String action);
}
