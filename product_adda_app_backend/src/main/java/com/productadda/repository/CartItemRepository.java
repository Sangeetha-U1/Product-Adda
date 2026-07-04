package com.productadda.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.Cart;
import com.productadda.entity.CartItem;
import com.productadda.entity.Product;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    List<CartItem> findByFkCartAndIsActiveTrue(Cart cart);

    Optional<CartItem> findByFkCartAndFkProductAndIsActiveTrue(Cart cart, Product product);
}