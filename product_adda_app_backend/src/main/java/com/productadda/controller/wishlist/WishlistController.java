package com.productadda.controller.wishlist;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.wishlist.WishlistDetailedResponseDto;
import com.productadda.dto.wishlist.WishlistItemRequestDto;
import com.productadda.dto.wishlist.WishlistWrapperDto;
import com.productadda.service.wishlist.WishlistAddService;
import com.productadda.service.wishlist.WishlistClearService;
import com.productadda.service.wishlist.WishlistInitializeService;
import com.productadda.service.wishlist.WishlistMoveToCartService;
import com.productadda.service.wishlist.WishlistRemoveItemService;
import com.productadda.service.wishlist.WishlistRetrievalService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users/wishlist")
@RequiredArgsConstructor
public class WishlistController {

        private final WishlistInitializeService wishlistInitializeService;
        private final WishlistAddService wishlistAddService;
        private final WishlistRetrievalService wishlistRetrievalService;
        private final WishlistMoveToCartService wishlistMoveToCartService;
        private final WishlistRemoveItemService wishlistRemoveItemService;
        private final WishlistClearService wishlistClearService;

        @PostMapping("/initialize")
        public ResponseEntity<ApiSuccessResponseDto<UUID>> initializeWorkspace() {

                // Explicitly provision a first-time wishlist data container for the
                // authenticated user
                UUID wishlistId = wishlistInitializeService.initializeWishlist();

                return ResponseEntity.status(HttpStatus.CREATED).body(
                                ApiSuccessResponseDto.<UUID>builder()
                                                .success(true)
                                                .message("Wishlist container has been successfully initialized for this user profile")
                                                .data(wishlistId)
                                                .build());
        }

        @PostMapping("/items")
        public ResponseEntity<ApiSuccessResponseDto<WishlistDetailedResponseDto>> addItem(
                        @Valid @RequestBody WishlistItemRequestDto requestDto) {

                // Pass the verified request DTO down to the self-contained business layer
                WishlistDetailedResponseDto data = wishlistAddService.addItemToWishlist(requestDto);

                return ResponseEntity.status(HttpStatus.CREATED).body(
                                ApiSuccessResponseDto.<WishlistDetailedResponseDto>builder()
                                                .success(true)
                                                .message("Item successfully added to wishlist catalog mapping matrix")
                                                .data(data)
                                                .build());
        }

        @GetMapping
        public ResponseEntity<ApiSuccessResponseDto<WishlistWrapperDto>> getMyWishlist(
                        @PageableDefault(size = 10, page = 0) Pageable pageable) {

                // Spring context handles query parameters (?page=0&size=10) and safely
                // translates them to Pageable instances
                WishlistWrapperDto data = wishlistRetrievalService.getUserWishlist(pageable);

                return ResponseEntity.status(HttpStatus.OK).body(
                                ApiSuccessResponseDto.<WishlistWrapperDto>builder()
                                                .success(true)
                                                .message("Wishlist catalog items fetched successfully")
                                                .data(data)
                                                .build());
        }

        @PostMapping("/items/{itemId}/move-to-cart")
        public ResponseEntity<ApiSuccessResponseDto<Void>> moveItemToCart(
                        @PathVariable("itemId") UUID wishlistItemId) {

                // Execute critical atomic inventory tracking transitions securely
                wishlistMoveToCartService.moveItemToCart(wishlistItemId);

                return ResponseEntity.status(HttpStatus.OK).body(
                                ApiSuccessResponseDto.<Void>builder()
                                                .success(true)
                                                .message("Wishlist catalog tracked item shifted to active cart context successfully")
                                                .data(null)
                                                .build());
        }

        @DeleteMapping("/items/{itemId}")
        public ResponseEntity<ApiSuccessResponseDto<Void>> removeProductFromWishlist(
                        @PathVariable("itemId") UUID wishlistItemId) {

                // Target individual items for absolute physical record expulsion from
                // persistence contexts
                wishlistRemoveItemService.removeItemFromWishlist(wishlistItemId);

                return ResponseEntity.status(HttpStatus.OK).body(
                                ApiSuccessResponseDto.<Void>builder()
                                                .success(true)
                                                .message("Target catalog entry physical expulsion executed successfully")
                                                .data(null)
                                                .build());
        }

        @DeleteMapping("/clear")
        public ResponseEntity<ApiSuccessResponseDto<Void>> purgeEntireWishlist() {

                // Empty the complete listing container via atomic batch drop runs cleanly
                wishlistClearService.clearUserWishlist();

                return ResponseEntity.status(HttpStatus.OK).body(
                                ApiSuccessResponseDto.<Void>builder()
                                                .success(true)
                                                .message("Complete user wishlist matrix purged successfully")
                                                .data(null)
                                                .build());
        }
}