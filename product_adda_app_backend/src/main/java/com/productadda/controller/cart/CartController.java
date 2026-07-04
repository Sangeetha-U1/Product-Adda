package com.productadda.controller.cart;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.cart.CartInitializeResponseDto;
import com.productadda.dto.cart.CartItemAddRequestDto;
import com.productadda.dto.cart.CartItemAddResponseDto;
import com.productadda.dto.cart.CartResponseDto;
import com.productadda.service.cart.CartInitializeService;
import com.productadda.service.cart.CartItemAddService;
import com.productadda.service.cart.CartRetrievalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartInitializeService cartInitializeService;
    private final CartItemAddService cartItemAddService;
    private final CartRetrievalService cartRetrievalService;

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

        ApiSuccessResponseDto<CartItemAddResponseDto> wrapper = ApiSuccessResponseDto.<CartItemAddResponseDto>builder()
                .success(true)
                .message("Item added to cart")
                .data(response)
                .build();

        return new ResponseEntity<>(wrapper, HttpStatus.CREATED);
    }

    @GetMapping("/{cartId}")
    public ResponseEntity<ApiSuccessResponseDto<CartResponseDto>> getCart(@PathVariable UUID cartId) {
        CartResponseDto response = cartRetrievalService.getCart(cartId);

        ApiSuccessResponseDto<CartResponseDto> wrapper = ApiSuccessResponseDto.<CartResponseDto>builder()
                .success(true)
                .message("Cart retrieved successfully")
                .data(response)
                .build();

        return new ResponseEntity<>(wrapper, HttpStatus.OK);
    }
}