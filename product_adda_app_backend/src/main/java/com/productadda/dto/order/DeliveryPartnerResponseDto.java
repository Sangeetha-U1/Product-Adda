package com.productadda.dto.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
 * ================================================================
 * RESPONSE DTO
 * Description: Sanitized, frontend-ready representation of a
 * delivery partner profile, returned after creation.
 * ================================================================
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryPartnerResponseDto {

    private UUID partnerId;

    private String partnerName;

    private String email;

    private String phone;

    private String currentLocation;

    private String statusName;

    private BigDecimal rating;

    private Integer totalDeliveries;

    private Integer activeDeliveries;

    private Integer maxConcurrentDeliveries;

    private LocalDateTime joinedAt;
}
