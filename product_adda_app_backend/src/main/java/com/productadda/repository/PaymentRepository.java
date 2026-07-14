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
        // RECONCILIATION DATE-RANGE LOOKUP
        // ==========================================
        List<Payment> findByCreatedAtUtcBetween(LocalDateTime startDate, LocalDateTime endDate);

        // ==========================================
        // ANALYTICS: TOTAL TRANSACTION COUNT
        // ==========================================
        @Query("SELECT COUNT(p) FROM Payment p " +
                        "WHERE p.createdAtUtc >= :startDate AND p.createdAtUtc <= :endDate " +
                        "AND (:gatewayName IS NULL OR p.fkGateway.gatewayName = :gatewayName) " +
                        "AND (:paymentMethod IS NULL OR p.paymentMethod = :paymentMethod)")
        long countTransactions(
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        @Param("gatewayName") String gatewayName,
                        @Param("paymentMethod") String paymentMethod);

        // ==========================================
        // ANALYTICS: TRANSACTION COUNT BY STATUS
        // Used for both success_rate_percent and failed_rate_percent.
        // ==========================================
        @Query("SELECT COUNT(p) FROM Payment p " +
                        "WHERE p.createdAtUtc >= :startDate AND p.createdAtUtc <= :endDate " +
                        "AND p.fkStatus.statusName = :statusName " +
                        "AND (:gatewayName IS NULL OR p.fkGateway.gatewayName = :gatewayName) " +
                        "AND (:paymentMethod IS NULL OR p.paymentMethod = :paymentMethod)")
        long countTransactionsByStatus(
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        @Param("statusName") String statusName,
                        @Param("gatewayName") String gatewayName,
                        @Param("paymentMethod") String paymentMethod);

        // ==========================================
        // ANALYTICS: REVENUE SUM BY STATUS
        // CONFIRMED real status value is "SUCCESS" (not "SUCCESSFUL" as the
        // original plan draft assumed). "REFUNDED"/"PARTIALLY_REFUNDED" are
        // both confirmed real values too.
        // ==========================================
        @Query("SELECT COALESCE(SUM(p.amountInPaise), 0) FROM Payment p " +
                        "WHERE p.createdAtUtc >= :startDate AND p.createdAtUtc <= :endDate " +
                        "AND p.fkStatus.statusName = :statusName " +
                        "AND (:gatewayName IS NULL OR p.fkGateway.gatewayName = :gatewayName) " +
                        "AND (:paymentMethod IS NULL OR p.paymentMethod = :paymentMethod)")
        long sumAmountByStatus(
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        @Param("statusName") String statusName,
                        @Param("gatewayName") String gatewayName,
                        @Param("paymentMethod") String paymentMethod);

        // ==========================================
        // ANALYTICS: REVENUE SUM ACROSS MULTIPLE STATUSES
        // Used for total_refunded_in_paise = SUM WHERE status IN
        // (REFUNDED, PARTIALLY_REFUNDED).
        // ==========================================
        @Query("SELECT COALESCE(SUM(p.amountInPaise), 0) FROM Payment p " +
                        "WHERE p.createdAtUtc >= :startDate AND p.createdAtUtc <= :endDate " +
                        "AND p.fkStatus.statusName IN :statusNames " +
                        "AND (:gatewayName IS NULL OR p.fkGateway.gatewayName = :gatewayName) " +
                        "AND (:paymentMethod IS NULL OR p.paymentMethod = :paymentMethod)")
        long sumAmountByStatusIn(
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        @Param("statusNames") List<String> statusNames,
                        @Param("gatewayName") String gatewayName,
                        @Param("paymentMethod") String paymentMethod);

        // ==========================================
        // ANALYTICS: PAYMENT METHOD BREAKDOWN
        // Returns Object[]{ paymentMethod, count, revenueInPaise } rows,
        // revenue counted only for SUCCESS payments per the plan.
        // ==========================================
        @Query("SELECT p.paymentMethod, COUNT(p), COALESCE(SUM(CASE WHEN p.fkStatus.statusName = 'SUCCESS' THEN p.amountInPaise ELSE 0 END), 0) "
                        +
                        "FROM Payment p " +
                        "WHERE p.createdAtUtc >= :startDate AND p.createdAtUtc <= :endDate " +
                        "AND (:gatewayName IS NULL OR p.fkGateway.gatewayName = :gatewayName) " +
                        "GROUP BY p.paymentMethod")
        List<Object[]> getMethodBreakdown(
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        @Param("gatewayName") String gatewayName);

        // ==========================================
        // ANALYTICS: PAYMENT GATEWAY BREAKDOWN
        // Returns Object[]{ gatewayName, count, revenueInPaise } rows.
        // ==========================================
        @Query("SELECT p.fkGateway.gatewayName, COUNT(p), COALESCE(SUM(CASE WHEN p.fkStatus.statusName = 'SUCCESS' THEN p.amountInPaise ELSE 0 END), 0) "
                        +
                        "FROM Payment p " +
                        "WHERE p.createdAtUtc >= :startDate AND p.createdAtUtc <= :endDate " +
                        "AND (:paymentMethod IS NULL OR p.paymentMethod = :paymentMethod) " +
                        "GROUP BY p.fkGateway.gatewayName")
        List<Object[]> getGatewayBreakdown(
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        @Param("paymentMethod") String paymentMethod);

        // ==========================================
        // ANALYTICS: DAILY REVENUE TREND
        // Returns Object[]{ dateString, revenueInPaise } rows, one per
        // calendar day with at least one SUCCESS payment in range.
        // TODO: only day-level grouping is implemented. The plan also
        // mentions week/month grouping via an optional group_by param -
        // not implemented; day-level is returned regardless of any
        // group_by value the frontend might send.
        // ==========================================
        @Query("SELECT FUNCTION('DATE', p.createdAtUtc), COALESCE(SUM(p.amountInPaise), 0) " +
                        "FROM Payment p " +
                        "WHERE p.createdAtUtc >= :startDate AND p.createdAtUtc <= :endDate " +
                        "AND p.fkStatus.statusName = 'SUCCESS' " +
                        "AND (:gatewayName IS NULL OR p.fkGateway.gatewayName = :gatewayName) " +
                        "AND (:paymentMethod IS NULL OR p.paymentMethod = :paymentMethod) " +
                        "GROUP BY FUNCTION('DATE', p.createdAtUtc) " +
                        "ORDER BY FUNCTION('DATE', p.createdAtUtc) ASC")
        List<Object[]> getRevenueTrendByDay(
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        @Param("gatewayName") String gatewayName,
                        @Param("paymentMethod") String paymentMethod);
}