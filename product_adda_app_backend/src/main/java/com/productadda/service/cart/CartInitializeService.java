package com.productadda.service.cart;

import com.productadda.dto.cart.CartInitializeResponseDto;
import com.productadda.dto.cart.CartTotalsDto;
import com.productadda.entity.Cart;
import com.productadda.entity.CartStatus;
import com.productadda.entity.User;
import com.productadda.exception.ApiException;
import com.productadda.repository.CartRepository;
import com.productadda.repository.CartStatusRepository;
import com.productadda.repository.UserRepository;
import com.productadda.util.UuidUtil;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartInitializeService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final CartStatusRepository cartStatusRepository;
    private final UuidUtil uuidUtil;
    private final EntityManager entityManager;

    @Transactional
    public CartInitializeResponseDto initializeCart() {
        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * Description: Manual security verification and active database resource lookup
         * checks.
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        // Omitted: Method signature contains no incoming request payload arguments.

        // ==========================================
        // 1.2 CONTEXT AUTHENTICATION
        // ==========================================
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "JWT token is missing or invalid");
        }
        String currentUsername = authentication.getName();

        // ==========================================
        // 1.3 DATABASE LOOKUP VALIDATION
        // ==========================================
        User user = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User entity mapping missing"));

        CartStatus activeStatus = cartStatusRepository.findByStatusCode("ACTIVE")
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Lookup cart status 'ACTIVE' not found"));

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        Optional<Cart> existingActiveCart = cartRepository.findByFkUserAndFkCartStatus_StatusCodeAndIsActiveTrue(user,
                "ACTIVE");

        // Unified Temporal Alignment applied explicitly prior to constructor state
        // definition
        LocalDateTime executionTime = LocalDateTime.now(ZoneOffset.UTC);

        // Initialize targeting entity wrapper dynamically without block interruption
        Cart targetCart = existingActiveCart.orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setPkCartId(uuidUtil.generateUuidV7());
            newCart.setFkUser(user);
            newCart.setFkCartStatus(activeStatus);
            newCart.setIsActive(true);
            newCart.setExpiresAtUtc(executionTime.plusDays(90));
            return newCart;
        });

        /*
         * ================================================================
         * 3. DB SAVING SECTION (Skip if Read-Only GET)
         * ================================================================
         */
        // Strict linear saving orchestration layer tracking transient states
        if (targetCart.getCreatedAtUtc() == null) {
            targetCart = cartRepository.save(targetCart);
            cartRepository.flush();
            entityManager.refresh(targetCart);
        }

        /*
         * ================================================================
         * 4. POST-SAVING DATA SANITIZATION & MASKING
         * ================================================================
         */
        // Omitted: Active operational values do not require transformation vectors.

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * ================================================================
         */
        CartTotalsDto emptyTotals = CartTotalsDto.builder()
                .itemCount(0)
                .subtotal(BigDecimal.ZERO)
                .couponDiscount(BigDecimal.ZERO)
                .shippingCost(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .total(BigDecimal.ZERO)
                .build();
        // TODO: Refactor timestamp serialization to enforce strict UTC ISO-8601 format
        // with 'Z' suffix (e.g., 2026-07-04T14:43:17Z) to match API specifications.
        return CartInitializeResponseDto.builder()
                .cartId(targetCart.getPkCartId())
                .status(targetCart.getFkCartStatus().getStatusCode())
                .createdAt(targetCart.getCreatedAtUtc())
                .expiresAt(targetCart.getExpiresAtUtc())
                .items(new ArrayList<>())
                .totals(emptyTotals)
                .lastUpdated(targetCart.getUpdatedAtUtc())
                .build();
    }
}