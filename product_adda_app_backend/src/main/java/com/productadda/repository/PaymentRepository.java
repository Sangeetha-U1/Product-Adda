package com.productadda.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.productadda.entity.Order;
import com.productadda.entity.Payment;
import com.productadda.entity.User;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

        Optional<Payment> findByGatewayOrderId(String gatewayOrderId);

        Optional<Payment> findByGatewayPaymentLinkId(String gatewayPaymentLinkId);

        Optional<Payment> findByFkOrder(Order fkOrder);

        // idempotency enforcement lookup for POST /api/payments/initiate
        Optional<Payment> findByIdempotencyKey(UUID idempotencyKey);

        // webhook handlers resolve the Payment row by gateway transaction id
        Optional<Payment> findByGatewayTransactionId(String gatewayTransactionId);

        // ==========================================
        // CUSTOMER PAYMENT HISTORY WITH OPTIONAL FILTERS
        // ==========================================
        @Query("SELECT p FROM Payment p " +
                        "WHERE p.fkUser = :customer " +
                        "AND (:statusName IS NULL OR p.fkStatus.statusName = :statusName) " +
                        "AND (:gatewayName IS NULL OR p.fkGateway.gatewayName = :gatewayName) " +
                        "AND (:startDate IS NULL OR p.createdAtUtc >= :startDate) " +
                        "AND (:endDate IS NULL OR p.createdAtUtc <= :endDate)")
        Page<Payment> findCustomerPaymentsWithFilters(
                        @Param("customer") User customer,
                        @Param("statusName") String statusName,
                        @Param("gatewayName") String gatewayName,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        Pageable pageable);

        // ==========================================
        // ALL PAYMENTS FOR AUTHENTICATED USER — FILTER + SEARCH
        // ==========================================
        @Query("SELECT p FROM Payment p " +
                        "LEFT JOIN FETCH p.fkOrder o " +
                        "LEFT JOIN FETCH p.fkStatus " +
                        "LEFT JOIN FETCH p.fkGateway " +
                        "WHERE p.fkUser = :user " +
                        "AND (:statusName IS NULL OR p.fkStatus.statusName = :statusName) " +
                        "AND (:gatewayName IS NULL OR p.fkGateway.gatewayName = :gatewayName) " +
                        "AND (:startDate IS NULL OR p.createdAtUtc >= :startDate) " +
                        "AND (:endDate IS NULL OR p.createdAtUtc <= :endDate) " +
                        "AND (:minAmountInPaise IS NULL OR p.amountInPaise >= :minAmountInPaise) " +
                        "AND (:maxAmountInPaise IS NULL OR p.amountInPaise <= :maxAmountInPaise) " +
                        "AND (:keyword IS NULL OR " +
                        "     o.orderNumber LIKE :keyword OR " +
                        "     p.fkGateway.gatewayName LIKE :keyword OR " +
                        "     p.paymentMethod LIKE :keyword)")
        Page<Payment> findAllPaymentsForUserWithFiltersAndSearch(
                        @Param("user") User user,
                        @Param("statusName") String statusName,
                        @Param("gatewayName") String gatewayName,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        @Param("minAmountInPaise") Long minAmountInPaise,
                        @Param("maxAmountInPaise") Long maxAmountInPaise,
                        @Param("keyword") String keyword,
                        Pageable pageable);

        // ==========================================
        // RECONCILIATION DATE-RANGE LOOKUP (Day 4)
        // ==========================================
        List<Payment> findByCreatedAtUtcBetween(LocalDateTime startDate, LocalDateTime endDate);
}