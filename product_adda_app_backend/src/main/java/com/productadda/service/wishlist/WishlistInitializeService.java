package com.productadda.service.wishlist;

import com.productadda.entity.User;
import com.productadda.entity.Wishlist;
import com.productadda.exception.ApiException;
import com.productadda.repository.UserRepository;
import com.productadda.repository.WishlistRepository;
import com.productadda.util.UuidUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WishlistInitializeService {

    private final UuidUtil uuidUtil;
    private final UserRepository userRepository;
    private final WishlistRepository wishlistRepository;

    @Transactional
    public UUID initializeWishlist() {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        // ==========================================
        // 1.1 CONTEXT AUTHENTICATION
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
        // 1.2 DATABASE LOOKUP VALIDATION
        // ==========================================
        User user = userRepository.findByEmail(principalName)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "Authenticated user account record could not be located"));

        if (!user.getIsActive()) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "Access denied. User account context is currently deactivated");
        }

        // Check if a workspace allocation already belongs to this identity row context
        boolean wishlistExists = wishlistRepository.findByFkUser(user).isPresent();
        if (wishlistExists) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "A wishlist workspace container has already been initialized for this user profile");
        }

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        Wishlist initializedWishlistMatrix = Wishlist.builder()
                .pkWishlistId(uuidUtil.generateUuidV7())
                .fkUser(user)
                .isActive(true)
                .build();

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * ================================================================
         */
        Wishlist savedWishlist = wishlistRepository.save(initializedWishlistMatrix);

        /*
         * ================================================================
         * 4. RESPONSE MAPPING
         * ================================================================
         */
        // TODO: Make this object
        return savedWishlist.getPkWishlistId();
    }
}