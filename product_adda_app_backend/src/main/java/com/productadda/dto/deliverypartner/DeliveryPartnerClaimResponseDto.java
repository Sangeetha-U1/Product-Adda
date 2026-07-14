package com.productadda.dto.deliverypartner;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryPartnerClaimResponseDto {

    private UUID orderId;

    private UUID partnerId;

    private String partnerName;

    private String claimStatus;

    private LocalDateTime claimedAt;
}