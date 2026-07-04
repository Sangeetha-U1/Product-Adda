package com.productadda.controller.cart;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.cart.CartInitializeResponseDto;
import com.productadda.dto.cart.CartItemAddRequestDto;
import com.productadda.dto.cart.CartItemAddResponseDto;
import com.productadda.dto.cart.CartResponseDto;
import com.productadda.service.cart.CartInitializeService;
import com.productadda.service.cart.CartItemAddService;
import com.productadda.service.cart.CartRetrievalService;
import com.productadda.service.cart.CartItemUpdateService;
import com.productadda.service.cart.CartItemRemoveService;
import com.productadda.service.cart.CartClearService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
import java.util.Map;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartInitializeService cartInitializeService;
    private final CartItemAddService cartItemAddService;
    private final CartRetrievalService cartRetrievalService;
    private final CartItemUpdateService cartItemUpdateService;
    private final CartItemRemoveService cartItemRemoveService;
    private final CartClearService cartClearService;

    @PostMapping("/initialize")
    public ResponseEntity<ApiSuccessResponseDto<CartInitializeResponseDto>> initializeCart() {
        CartInitializeResponseDto response = cartInitializeService.initializeCart();

        ApiSuccessResponseDto<CartInitializeResponseDto> wrapper = ApiSuccessResponseDto
                .<CartInitializeResponseDto>builder()
                .success(true)
                .message("Cart initialized successfully")
                .data(response)
                .build();

        return new ResponseEntity<>(wrapper, HttpStatus.CREATED);
    }

    @PostMapping("/{cartId}/items")
    public ResponseEntity<ApiSuccessResponseDto<CartItemAddResponseDto>> addItemToCart(
            @PathVariable UUID cartId,
            @RequestBody CartItemAddRequestDto requestDto) {

        CartItemAddResponseDto response = cartItemAddService.addItemToCart(cartId, requestDto);

        ApiSuccessResponseDto<CartItemAddResponseDto> wrapper = ApiSuccessResponseDto
                .<CartItemAddResponseDto>builder()
                .success(true)
                .message("Item added to cart")
                .data(response)
                .build();

        return new ResponseEntity<>(wrapper, HttpStatus.CREATED);
    }

    @GetMapping("/{cartId}")
    public ResponseEntity<ApiSuccessResponseDto<CartResponseDto>> getCart(@PathVariable UUID cartId) {
        CartResponseDto response = cartRetrievalService.getCart(cartId);

        ApiSuccessResponseDto<CartResponseDto> wrapper = ApiSuccessResponseDto
                .<CartResponseDto>builder()
                .success(true)
                .message("Cart retrieved successfully")
                .data(response)
                .build();

        return new ResponseEntity<>(wrapper, HttpStatus.OK);
    }

    @PutMapping("/{cartId}/items/{itemId}")
    public ResponseEntity<ApiSuccessResponseDto<CartResponseDto>> updateItemQuantity(
            @PathVariable UUID cartId,
            @PathVariable UUID itemId,
            @RequestBody Map<String, Integer> payload) {

        Integer quantity = payload.get("quantity");
        CartResponseDto dynamicCart = cartItemUpdateService.updateQuantity(cartId, itemId, quantity);

        ApiSuccessResponseDto<CartResponseDto> wrapper = ApiSuccessResponseDto
                .<CartResponseDto>builder()
                .success(true)
                .message("Item quantity updated")
                .data(dynamicCart)
                .build();

        return new ResponseEntity<>(wrapper, HttpStatus.OK);
    }

    @DeleteMapping("/{cartId}/items/{itemId}")
    public ResponseEntity<ApiSuccessResponseDto<CartResponseDto>> removeItem(
            @PathVariable UUID cartId,
            @PathVariable UUID itemId) {

        CartResponseDto dynamicCart = cartItemRemoveService.removeItem(cartId, itemId);

        ApiSuccessResponseDto<CartResponseDto> wrapper = ApiSuccessResponseDto
                .<CartResponseDto>builder()
                .success(true)
                .message("Item removed from cart successfully")
                .data(dynamicCart)
                .build();

        return new ResponseEntity<>(wrapper, HttpStatus.OK);
    }

    @DeleteMapping("/{cartId}")
    public ResponseEntity<ApiSuccessResponseDto<Void>> clearCart(@PathVariable UUID cartId) {

        cartClearService.clearCart(cartId);

        ApiSuccessResponseDto<Void> wrapper = ApiSuccessResponseDto
                .<Void>builder()
                .success(true)
                .message("Cart cleared successfully")
                .data(null)
                .build();

        return new ResponseEntity<>(wrapper, HttpStatus.OK);
    }
}