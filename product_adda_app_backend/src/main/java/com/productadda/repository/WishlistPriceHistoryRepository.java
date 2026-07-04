package com.productadda.repository;

import com.productadda.entity.WishlistItem;
import com.productadda.entity.WishlistPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface WishlistPriceHistoryRepository extends JpaRepository<WishlistPriceHistory, UUID> {

    @Query("SELECT wph FROM WishlistPriceHistory wph " +
            "WHERE wph.fkWishlistItem = :wishlistItem AND wph.isActive = true " +
            "ORDER BY wph.snapshotAtUtc DESC")
    List<WishlistPriceHistory> findTop10ByWishlistItemOrderBySnapshotAtUtcDesc(
            @Param("wishlistItem") WishlistItem wishlistItem);
}