package com.productadda.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.Order;
import com.productadda.entity.Payment;
import com.productadda.entity.Refund;

/*
 * ================================================================
 * RefundRepository
 * ================================================================
 */
public interface RefundRepository extends JpaRepository<Refund, UUID> {

        List<Refund> findByFkPayment(Payment payment);

        List<Refund> findByFkOrder(Order order);

        // ==========================================
        // CUSTOMER-SCOPED REFUND FETCH
        // Description: Fetches one refund by its own primary key while
        // additionally verifying ownership via Payment.fkCustomer, in a
        // single query rather than a separate manual ownership check.
        // Mirrors OrderItemRepository.findByPkOrderItemIdAndFkOrder_PkOrderId.
        // ==========================================
        Optional<Refund> findByPkRefundIdAndFkPayment_FkUser_PkUserId(UUID refundId, UUID customerId);

        Optional<Refund> findByGatewayRefundId(String gatewayRefundId);
}
