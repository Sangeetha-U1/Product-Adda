package com.productadda.dto.payment;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodResponseDto {

    private String id;

    private String methodType;

    // e.g. "Card ending in 4242", "UPI payment method"
    private String displayName;

    // Only populated for card methods
    private String brand;

    // "MM/YYYY", only populated for card methods
    private String expiry;

    private Boolean isPrimary;

    private LocalDateTime createdAtUtc;
}
