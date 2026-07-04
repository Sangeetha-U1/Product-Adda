package com.productadda.service.wishlist;

import com.productadda.entity.User;
import com.productadda.entity.Wishlist;
import com.productadda.entity.WishlistItem;
import com.productadda.exception.ApiException;
import com.productadda.repository.UserRepository;
import com.productadda.repository.WishlistRepository;
import com.productadda.repository.WishlistItemRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistClearService {

    // TODO: Improve 5 layer architecture

    private final UserRepository userRepository;
    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;

    @Transactional
    public void clearUserWishlist() {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================

        // ==========================================
        // 1.2 CONTEXT AUTHENTICATION
        // ==========================================
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED,
                    "Security context validation failed. Missing authentication token");
        }

        String principalName = authentication.getName();
        if (principalName == null || principalName.trim().isEmpty()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid authentication principal extraction");
        }

        // ==========================================
        // 1.3 DATABASE LOOKUP VALIDATION
        // ==========================================
        User user = userRepository.findByEmail(principalName)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "Authenticated user account record could not be located"));

        if (!user.getIsActive()) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "Access denied. User account context is currently deactivated");
        }

        Wishlist wishlist = wishlistRepository.findByFkUser(user).orElse(null);

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        if (wishlist != null) {
            // Retrieve all items bound to this wishlist container
            List<WishlistItem> operationalItemsList = wishlistItemRepository.findByFkWishlist(wishlist);

            /*
             * ================================================================
             * 3. DB SAVING & DELETION SECTION (HARD CLEAR)
             * ================================================================
             */
            if (!operationalItemsList.isEmpty()) {
                wishlistItemRepository.deleteAll(operationalItemsList);
            }
        }
    }
}