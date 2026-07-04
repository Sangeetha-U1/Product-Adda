package com.productadda.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.Product;
import com.productadda.entity.WishlistPriceHistory;

public interface WishlistPriceHistoryRepository extends JpaRepository<WishlistPriceHistory, UUID> {

    // Chaining properties: fkWishlistItem -> fkProduct inside WishlistItem entity
    List<WishlistPriceHistory> findByFkWishlistItemFkProductOrderByCreatedAtUtcDesc(Product product);
}