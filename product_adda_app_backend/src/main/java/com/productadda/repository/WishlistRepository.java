package com.productadda.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.productadda.entity.Wishlist;
import com.productadda.entity.User;

public interface WishlistRepository extends JpaRepository<Wishlist, UUID> {
    Optional<Wishlist> findByFkUser(User user);
}