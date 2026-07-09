package com.productadda.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.productadda.entity.Order;
import com.productadda.entity.User;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByFkUserAndIsActiveTrueOrderByCreatedAtUtcDesc(User user);

    Optional<Order> findByOrderNumber(String orderNumber);

    Optional<Order> findByIdempotencyKey(UUID idempotencyKey);

    Page<Order> findByFkUserAndIsActiveTrue(User user, Pageable pageable);

    // ==========================================
    // CUSTOMER ORDER RETRIEVAL WITH OPTIONAL FILTERS
    // Description: Supports optional status name and created-date range
    // filtering for the authenticated customer's own orders.
    // ==========================================
    @Query("SELECT o FROM Order o " +
            "WHERE o.fkUser = :user " +
            "AND o.isActive = true " +
            "AND (:statusName IS NULL OR o.fkStatus.statusName = :statusName) " +
            "AND (:startDate IS NULL OR o.createdAtUtc >= :startDate) " +
            "AND (:endDate IS NULL OR o.createdAtUtc <= :endDate)")
    Page<Order> findCustomerOrdersWithFilters(
            @Param("user") User user,
            @Param("statusName") String statusName,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    // ==========================================
    // ADMIN ORDER RETRIEVAL WITH ADVANCED FILTERS
    // Description: Full-visibility query with no user-ownership scoping.
    // Optional vendorOrderIds param narrows results to orders containing
    // at least one item from a specific vendor (resolved beforehand via
    // OrderItemRepository.findDistinctOrderIdsByVendorId).
    // ==========================================
    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN o.fkDeliveryPartner dp " +
            "WHERE (:statusName IS NULL OR o.fkStatus.statusName = :statusName) " +
            "AND (:userId IS NULL OR o.fkUser.pkUserId = :userId) " +
            "AND (:deliveryPartnerId IS NULL OR dp.pkDeliveryPartnerId = :deliveryPartnerId) " +
            "AND (:createdAfter IS NULL OR o.createdAtUtc >= :createdAfter) " +
            "AND (:createdBefore IS NULL OR o.createdAtUtc <= :createdBefore) " +
            "AND (:vendorOrderIds IS NULL OR o.pkOrderId IN :vendorOrderIds)")
    Page<Order> findAdminOrdersWithFilters(
            @Param("statusName") String statusName,
            @Param("userId") UUID userId,
            @Param("deliveryPartnerId") UUID deliveryPartnerId,
            @Param("createdAfter") LocalDateTime createdAfter,
            @Param("createdBefore") LocalDateTime createdBefore,
            @Param("vendorOrderIds") List<UUID> vendorOrderIds,
            Pageable pageable);

}