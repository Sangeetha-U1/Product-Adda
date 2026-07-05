package com.productadda.controller.cart;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.cart.CartResponseDto;
import com.productadda.dto.cart.CouponApplyRequestDto;
import com.productadda.dto.cart.CouponValidationResponseDto;

import com.productadda.service.cart.CouponApplyService;
import com.productadda.service.cart.CouponRemoveService;
import com.productadda.service.cart.CouponValidationService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponApplyService couponApplyService;
    private final CouponRemoveService couponRemoveService;
    private final CouponValidationService couponValidationService;

    @PostMapping("/apply")
    public ResponseEntity<ApiSuccessResponseDto<CartResponseDto>> applyCoupon(
            @Valid @RequestBody CouponApplyRequestDto requestDto) {

        CartResponseDto response = couponApplyService.applyCoupon(requestDto);

        ApiSuccessResponseDto<CartResponseDto> wrapper = ApiSuccessResponseDto.<CartResponseDto>builder()
                .success(true)
                .message("Coupon applied successfully")
                .data(response)
                .build();

        return new ResponseEntity<>(wrapper, HttpStatus.OK);
    }

    @DeleteMapping("/remove")
    public ResponseEntity<ApiSuccessResponseDto<CartResponseDto>> removeCoupon() {

        CartResponseDto response = couponRemoveService.removeCoupon();

        ApiSuccessResponseDto<CartResponseDto> wrapper = ApiSuccessResponseDto.<CartResponseDto>builder()
                .success(true)
                .message("Coupon removed successfully")
                .data(response)
                .build();

        return new ResponseEntity<>(wrapper, HttpStatus.OK);
    }

    // Coupon Public Verification Endpoint (POST Only)
    @PostMapping("/validate")
    public ResponseEntity<ApiSuccessResponseDto<CouponValidationResponseDto>> validateCoupon(
            @Valid @RequestBody CouponApplyRequestDto requestDto) {

        CouponValidationResponseDto response = couponValidationService.validateCoupon(requestDto);

        ApiSuccessResponseDto<CouponValidationResponseDto> wrapper = ApiSuccessResponseDto
                .<CouponValidationResponseDto>builder()
                .success(true)
                .message("Coupon validation step completed")
                .data(response)
                .build();

        return new ResponseEntity<>(wrapper, HttpStatus.OK);
    }
}