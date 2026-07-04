package com.productadda.service.wishlist;

import com.productadda.dto.wishlist.WishlistDetailedResponseDto;
import com.productadda.dto.wishlist.WishlistPriceHistoryDto;
import com.productadda.dto.wishlist.WishlistWrapperDto;

import com.productadda.entity.User;
import com.productadda.entity.Wishlist;
import com.productadda.entity.WishlistItem;
import com.productadda.entity.WishlistPriceHistory;

import com.productadda.exception.ApiException;

import com.productadda.repository.UserRepository;
import com.productadda.repository.WishlistRepository;
import com.productadda.repository.WishlistItemRepository;
import com.productadda.repository.WishlistPriceHistoryRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistRetrievalService {

        private final UserRepository userRepository;
        private final WishlistRepository wishlistRepository;
        private final WishlistItemRepository wishlistItemRepository;
        private final WishlistPriceHistoryRepository wishlistPriceHistoryRepository;

        @Transactional(readOnly = true)
        public WishlistWrapperDto getUserWishlist(Pageable pageable) {

                /*
                 * ================================================================
                 * 1. VALIDATION SECTION
                 * Description: Ensures context authentication constraints match active target
                 * entity bounds.
                 * ================================================================
                 */

                // ==========================================
                // 1.1 REQUEST VALIDATION
                // ==========================================
                if (pageable == null) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Pagination parameters cannot be null");
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
                // 1.3 DATABASE LOOKUP VALIDATION
                // ==========================================
                User user = userRepository.findByEmail(principalName)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                "Authenticated user account record could not be located"));

                if (!user.getIsActive()) {
                        throw new ApiException(HttpStatus.FORBIDDEN,
                                        "Access denied. User account context is currently deactivated");
                }

                // WISHLIST INITIALIZATION CHECK
                Wishlist wishlist = wishlistRepository.findByFkUser(user).orElse(null);
                if (wishlist == null) {
                        throw new ApiException(HttpStatus.NOT_FOUND,
                                        "Wishlist container has not been initialized for this user account");
                }

                /*
                 * ================================================================
                 * 2. BUSINESS RULES & PROCESSING / WORKFLOW
                 * ================================================================
                 */
                Page<WishlistItem> contextualItemsPage = wishlistItemRepository
                                .findByFkWishlistAndIsActiveTrueAndIsDeletedFalse(wishlist, pageable);
                List<WishlistDetailedResponseDto> sequentialProcessedItems = new ArrayList<>();

                for (WishlistItem item : contextualItemsPage.getContent()) {
                        BigDecimal immediateProductPrice = item.getFkProduct().getPrice();
                        BigDecimal thresholdPriceAtAdd = item.getPriceAtAdd();

                        BigDecimal calculatedPriceDelta = BigDecimal.ZERO;
                        if (thresholdPriceAtAdd.compareTo(BigDecimal.ZERO) > 0) {
                                calculatedPriceDelta = immediateProductPrice.subtract(thresholdPriceAtAdd)
                                                .divide(thresholdPriceAtAdd, 4, RoundingMode.HALF_UP)
                                                .multiply(BigDecimal.valueOf(100))
                                                .setScale(2, RoundingMode.HALF_UP);
                        }

                        List<WishlistPriceHistory> topHistoricalSnapshots = wishlistPriceHistoryRepository
                                        .findTop10ByWishlistItemOrderBySnapshotAtUtcDesc(item);

                        BigDecimal verifiedLowestPriceThreshold = immediateProductPrice;
                        for (WishlistPriceHistory singleHistoryRecord : topHistoricalSnapshots) {
                                if (singleHistoryRecord.getPriceSnapshot()
                                                .compareTo(verifiedLowestPriceThreshold) < 0) {
                                        verifiedLowestPriceThreshold = singleHistoryRecord.getPriceSnapshot();
                                }
                        }

                        List<WishlistPriceHistoryDto> integratedHistoryDtos = topHistoricalSnapshots.stream()
                                        .map(historyEntity -> WishlistPriceHistoryDto.builder()
                                                        .historyId(historyEntity.getPkHistoryId())
                                                        .priceSnapshot(historyEntity.getPriceSnapshot())
                                                        .priceDropPercentage(historyEntity.getPriceDropPercentage())
                                                        .snapshotAtUtc(historyEntity.getSnapshotAtUtc())
                                                        .build())
                                        .collect(Collectors.toList());

                        // Extract the validated brand name field matching your source schema
                        // definitions
                        String isolatedBrandName = item.getFkProduct().getFkBrand() != null
                                        ? item.getFkProduct().getFkBrand().getBrandName()
                                        : null;

                        sequentialProcessedItems.add(WishlistDetailedResponseDto.builder()
                                        .wishlistItemId(item.getPkWishlistItemId())
                                        .productId(item.getFkProduct().getPkProductId())
                                        .productName(item.getFkProduct().getTitle())
                                        .currentPrice(immediateProductPrice)
                                        .priceAtAdd(thresholdPriceAtAdd)
                                        .priceDeltaPercentage(calculatedPriceDelta)
                                        .lowestPrice(verifiedLowestPriceThreshold)
                                        .expiresAtUtc(item.getExpiresAtUtc())
                                        .brandName(isolatedBrandName)
                                        .priceHistory(integratedHistoryDtos)
                                        .build());
                }

                /*
                 * ================================================================
                 * 3. DB SAVING SECTION (Skip if Read-Only GET)
                 * Description: Skipped on read-only queries to guarantee system performance
                 * compliance.
                 * ================================================================
                 */

                /*
                 * ================================================================
                 * 4. POST-SAVING DATA SANITIZATION & MASKING
                 * Description: Data transformations completed inline over loops to enforce flat
                 * pipeline standards.
                 * ================================================================
                 */
                int totalCalculatedItemsCount = (int) contextualItemsPage.getTotalElements();
                UUID configuredWishlistId = wishlist.getPkWishlistId();

                /*
                 * ================================================================
                 * 5. RESPONSE MAPPING
                 * ================================================================
                 */
                return WishlistWrapperDto.builder()
                                .wishlistId(configuredWishlistId)
                                .itemCount(totalCalculatedItemsCount)
                                .items(sequentialProcessedItems)
                                .build();
        }
}