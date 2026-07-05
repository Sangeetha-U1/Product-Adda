package com.productadda.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.Cart;
import com.productadda.entity.CartStatus;
import com.productadda.entity.User;

public interface CartRepository extends JpaRepository<Cart, UUID> {

    Optional<Cart> findByFkUserAndFkCartStatusAndIsActiveTrue(
            User user,
            CartStatus fkCartStatus);
}