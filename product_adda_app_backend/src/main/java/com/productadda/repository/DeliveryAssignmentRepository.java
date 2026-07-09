package com.productadda.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.productadda.entity.DeliveryAssignment;

public interface DeliveryAssignmentRepository extends JpaRepository<DeliveryAssignment, UUID> {

    List<DeliveryAssignment> findByFkOrder_PkOrderId(UUID orderId);
    
    List<DeliveryAssignment> findByFkDeliveryPartner_PkDeliveryPartnerId(UUID deliveryPartnerId);
}