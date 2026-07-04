package com.productadda.service.wishlist;

import com.productadda.entity.User;
import com.productadda.entity.WishlistItem;
import com.productadda.exception.ApiException;
import com.productadda.repository.UserRepository;
import com.productadda.repository.WishlistItemRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WishlistRemoveItemService {

    private final UserRepository userRepository;
    private final WishlistItemRepository wishlistItemRepository;

    @Transactional
    public void removeItemFromWishlist(UUID wishlistItemId) {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        if (wishlistItemId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Wishlist item identifier cannot be empty or null");
        }

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
        // 1.3 DATABASE LOOKUP & OWNERSHIP VALIDATION
        // ==========================================
        User user = userRepository.findByEmail(principalName)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "Authenticated user account record could not be located"));

        if (!user.getIsActive()) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "Access denied. User account context is currently deactivated");
        }

        WishlistItem item = wishlistItemRepository.findById(wishlistItemId)
                .orElseThrow(
                        () -> new ApiException(HttpStatus.NOT_FOUND, "Target wishlist item record could not be found"));

        // Guard against horizontal privilege escalation attacks by cross-checking the
        // actual user ID mapping
        UUID ownerId = item.getFkWishlist().getFkUser().getPkUserId();
        if (!ownerId.equals(user.getPkUserId())) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "Access authorization denied. Cross-user data modification blocked");
        }

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW (HARD DELETE)
         * ================================================================
         */

        /*
         * ================================================================
         * 3. DB SAVING & DELETION SECTION
         * ================================================================
         */
        wishlistItemRepository.delete(item);
    }
}