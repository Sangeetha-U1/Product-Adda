package com.productadda.repository;

import java.util.UUID;
import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

        @Query("SELECT al FROM PaymentAuditLog al " +
                        "WHERE (:startDate IS NULL OR al.createdAtUtc >= :startDate) " +
                        "AND (:endDate IS NULL OR al.createdAtUtc <= :endDate) " +
                        "AND (:action IS NULL OR al.action = :action) " +
                        "AND (:actorRole IS NULL OR al.actorRole = :actorRole) " +
                        "AND (:paymentId IS NULL OR al.fkPayment.pkPaymentId = :paymentId) " +
                        "ORDER BY al.createdAtUtc DESC")
        Page<PaymentAuditLog> findAuditLogsWithFilters(
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        @Param("action") String action,
                        @Param("actorRole") String actorRole,
                        @Param("paymentId") UUID paymentId,
                        Pageable pageable);
}
