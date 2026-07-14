package com.productadda.service.cart;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.cart.CheckoutShippingMethodResponseDto;

import com.productadda.entity.ShippingMethod;

import com.productadda.exception.ApiException;

import com.productadda.repository.ShippingMethodRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CheckoutShippingMethodService {

    private final ShippingMethodRepository shippingMethodRepository;

    @Transactional(readOnly = true)
    public List<CheckoutShippingMethodResponseDto> getActiveShippingMethods() {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication missing or invalid");
        }

        // TODO: Add if further db role validations are necessary.

        /*
         * ================================================================
         * 2. DATABASE LOOKUP
         * ================================================================
         */

        List<ShippingMethod> shippingMethods = shippingMethodRepository.findByIsActiveTrue();

        /*
         * ================================================================
         * 3. RESPONSE MAPPING
         * ================================================================
         */

        return shippingMethods.stream()
                .map(method -> CheckoutShippingMethodResponseDto.builder()
                        .shippingMethodId(method.getPkShippingMethodId())
                        .shippingMethodName(method.getShippingMethodName())
                        .description(method.getDescription())
                        .baseCost(method.getBaseCost())
                        .costPerKg(method.getCostPerKg())
                        .estimatedDeliveryDays(method.getEstimatedDeliveryDays())
                        .build())
                .toList();
    }
}