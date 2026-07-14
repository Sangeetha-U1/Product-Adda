package com.productadda.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.PaymentMethod;
import com.productadda.entity.User;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, UUID> {

        Optional<PaymentMethod> findByFkUserAndGatewayTokenId(User user, String gatewayTokenId);

        List<PaymentMethod> findByFkUserOrderByIsPrimaryDescCreatedAtUtcDesc(User user);

        List<PaymentMethod> findByFkUserAndIsActiveOrderByIsPrimaryDescCreatedAtUtcDesc(User user, Boolean isActive);

        // ==========================================
        // PRIMARY-FLAG DEMOTION
        // Description: Used when a new method is marked is_primary=true -
        // every other active method for this user must be demoted first.
        // ==========================================
        List<PaymentMethod> findByFkUserAndIsPrimaryTrue(User user);
}
