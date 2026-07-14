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
public class DeliveryPartnerCompleteResponseDto {

    private UUID orderId;
    private UUID partnerId;
    private String partnerName;
    private String status;
    private LocalDateTime deliveredAtUtc;
    private Integer activeDeliveries;
    private Integer totalDeliveries;
}
