package com.productadda.controller.user;

import java.util.List;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.user.WishlistResponseDto;
import com.productadda.service.user.WishlistService;

@RestController
@RequestMapping("/api/users/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public ResponseEntity<ApiSuccessResponseDto<List<WishlistResponseDto>>> getMyWishlist() {
        List<WishlistResponseDto> data = wishlistService.getMyWishlist();

        return ResponseEntity.status(HttpStatus.OK).body(
                ApiSuccessResponseDto.<List<WishlistResponseDto>>builder()
                        .success(true)
                        .message("Wishlist catalog items fetched successfully")
                        .data(data)
                        .build());
    }
}