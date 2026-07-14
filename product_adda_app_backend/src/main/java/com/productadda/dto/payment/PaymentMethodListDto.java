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
public class PaymentMethodListDto {

    private String id;

    private String methodType;

    // e.g. "Card ending in 4242"
    private String displayName;

    private Boolean isPrimary;

    private Boolean isActive;

    private LocalDateTime createdAtUtc;
}
