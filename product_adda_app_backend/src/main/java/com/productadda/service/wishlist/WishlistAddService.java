package com.productadda.service.wishlist;

import com.productadda.dto.wishlist.WishlistDetailedResponseDto;
import com.productadda.dto.wishlist.WishlistItemRequestDto;
import com.productadda.dto.wishlist.WishlistPriceHistoryDto;

import com.productadda.entity.User;
import com.productadda.entity.Wishlist;
import com.productadda.entity.WishlistItem;
import com.productadda.entity.WishlistPriceHistory;
import com.productadda.entity.Product;

import com.productadda.exception.ApiException;

import com.productadda.repository.UserRepository;
import com.productadda.repository.WishlistRepository;
import com.productadda.repository.WishlistItemRepository;
import com.productadda.repository.WishlistPriceHistoryRepository;
import com.productadda.repository.ProductRepository;

import com.productadda.util.UuidUtil;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WishlistAddService {

        private final UuidUtil uuidUtil;
        private final UserRepository userRepository;
        private final WishlistRepository wishlistRepository;
        private final WishlistItemRepository wishlistItemRepository;
        private final WishlistPriceHistoryRepository wishlistPriceHistoryRepository;
        private final ProductRepository productRepository;

        @Transactional
        public WishlistDetailedResponseDto addItemToWishlist(WishlistItemRequestDto requestDto) {

                /*
                 * ================================================================
                 * 1. VALIDATION SECTION
                 * Description: Performs rigorous manual payload checks, security context
                 * verification, and entity lookup confirmations.
                 * ================================================================
                 */

                // ==========================================
                // 1.1 REQUEST VALIDATION
                // ==========================================
                if (requestDto == null || requestDto.getProductId() == null) {
                        throw new ApiException(HttpStatus.BAD_REQUEST,
                                        "Product tracking identifier cannot be empty or null");
                }

                UUID productId = requestDto.getProductId();

                // ==========================================
                // 1.2 CONTEXT AUTHENTICATION
                // ==========================================
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication == null || !authentication.isAuthenticated()) {
                        throw new ApiException(HttpStatus.UNAUTHORIZED,
                                        "Security context context validation failed. Missing authentication token");
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

                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                "Target product resource could not be found"));

                if (!product.getIsActive()) {
                        throw new ApiException(HttpStatus.NOT_FOUND, "Target product resource is no longer active");
                }

                Wishlist wishlist = wishlistRepository.findByFkUser(user)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                "Wishlist container has not been initialized for this user account"));

                List<WishlistItem> ongoingItems = wishlistItemRepository
                                .findByFkWishlistAndIsActiveTrueAndIsDeletedFalse(wishlist);
                boolean isItemDuplicated = ongoingItems.stream()
                                .anyMatch(item -> item.getFkProduct().getPkProductId().equals(productId));

                if (isItemDuplicated) {
                        throw new ApiException(HttpStatus.CONFLICT,
                                        "This product is already present in your active wishlist tracking matrix");
                }

                /*
                 * ================================================================
                 * 2. BUSINESS RULES & PROCESSING / WORKFLOW
                 * ================================================================
                 */
                BigDecimal activePrice = product.getPrice();
                LocalDateTime operationalUtcTimestamp = LocalDateTime.now(ZoneOffset.UTC);

                WishlistItem targetWishlistItem = WishlistItem.builder()
                                .pkWishlistItemId(uuidUtil.generateUuidV7())
                                .fkWishlist(wishlist)
                                .fkProduct(product)
                                .priceAtAdd(activePrice)
                                .isActive(true)
                                .isDeleted(false)
                                .expiresAtUtc(operationalUtcTimestamp.plusDays(90))
                                .build();

                WishlistPriceHistory historicSnapshot = WishlistPriceHistory.builder()
                                .pkHistoryId(uuidUtil.generateUuidV7())
                                .fkWishlistItem(targetWishlistItem)
                                .priceSnapshot(activePrice)
                                .priceDropPercentage(BigDecimal.ZERO)
                                .snapshotAtUtc(operationalUtcTimestamp)
                                .isActive(true)
                                .build();

                /*
                 * ================================================================
                 * 3. DB SAVING SECTION
                 * ================================================================
                 */
                WishlistItem recordedItem = wishlistItemRepository.save(targetWishlistItem);
                WishlistPriceHistory recordedHistory = wishlistPriceHistoryRepository.save(historicSnapshot);

                /*
                 * ================================================================
                 * 4. POST-SAVING DATA SANITIZATION & MASKING
                 * ================================================================
                 */
                WishlistPriceHistoryDto mappedHistoryDto = WishlistPriceHistoryDto.builder()
                                .historyId(recordedHistory.getPkHistoryId())
                                .priceSnapshot(recordedHistory.getPriceSnapshot())
                                .priceDropPercentage(recordedHistory.getPriceDropPercentage())
                                .snapshotAtUtc(recordedHistory.getSnapshotAtUtc())
                                .build();

                List<WishlistPriceHistoryDto> localizedHistoryCollection = Collections.singletonList(mappedHistoryDto);

                // Dynamic brand string validation extraction
                String dynamicBrandString = product.getFkBrand() != null ? product.getFkBrand().getBrandName() : null;

                /*
                 * ================================================================
                 * 5. RESPONSE MAPPING
                 * ================================================================
                 */
                return WishlistDetailedResponseDto.builder()
                                .wishlistItemId(recordedItem.getPkWishlistItemId())
                                .productId(product.getPkProductId())
                                .productName(product.getTitle())
                                .currentPrice(activePrice)
                                .priceAtAdd(activePrice)
                                .priceDeltaPercentage(BigDecimal.ZERO)
                                .lowestPrice(activePrice)
                                .expiresAtUtc(recordedItem.getExpiresAtUtc())
                                .brandName(dynamicBrandString)
                                .priceHistory(localizedHistoryCollection)
                                .build();
        }
}