package com.productadda.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.productadda.entity.Wishlist;
import com.productadda.entity.WishlistItem;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, UUID> {
    List<WishlistItem> findByFkWishlist(Wishlist wishlist);
}