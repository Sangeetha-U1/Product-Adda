package com.productadda.service.user;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.user.WishlistResponseDto;
import com.productadda.entity.Product;
import com.productadda.entity.User;
import com.productadda.entity.Wishlist;
import com.productadda.entity.WishlistItem;
import com.productadda.exception.ApiException;
import com.productadda.repository.UserRepository;
import com.productadda.repository.WishlistItemRepository;
import com.productadda.repository.WishlistRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final UserRepository userRepository;

    /*
     * ================================================================
     * GET CURRENT USER WISHLIST
     * Description: Retrieves the collection of active catalog products saved to
     * the wishlist of the currently logged-in context session user.
     * ================================================================
     */
    @Transactional(readOnly = true)
    public List<WishlistResponseDto> getMyWishlist() {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        // Structural context retrieval. No explicit body parameters are validated.

        // ==========================================
        // 1.2 CONTEXT AUTHENTICATION
        // ==========================================
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentication missing or invalid");
        }

        String email;
        if (authentication.getPrincipal() instanceof UserDetails userDetails) {
            email = userDetails.getUsername();
        } else {
            email = authentication.getName();
        }

        // ==========================================
        // 1.3 DATABASE LOOKUP VALIDATION
        // ==========================================
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Authenticated user no longer exists"));

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        Optional<Wishlist> wishlistOpt = wishlistRepository.findByFkUser(user);

        if (wishlistOpt.isEmpty()) {
            return Collections.emptyList();
        }

        List<WishlistItem> items = wishlistItemRepository.findByFkWishlist(wishlistOpt.get());

        /*
         * ================================================================
         * 4. RESPONSE MAPPING
         * ================================================================
         */
        List<WishlistResponseDto> responseList = new ArrayList<>();

        for (WishlistItem item : items) {
            Product product = item.getFkProduct();

            if (product == null) {
                throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Wishlist configuration is broken or missing data");
            }

            String brandName = null;
            if (product.getFkBrand() != null) {
                brandName = product.getFkBrand().getBrandName();
            }

            WishlistResponseDto dto = WishlistResponseDto.builder()
                    .wishlistItemId(item.getPkWishlistItemId())
                    .productId(product.getPkProductId())
                    .productName(product.getTitle())
                    .price(product.getPrice())
                    .brandName(brandName)
                    .build();

            responseList.add(dto);
        }

        return responseList;
    }
}