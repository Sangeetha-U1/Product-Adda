package com.productadda.dto.deliverypartner;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryPartnerAvailableOrderListDto {

    private UUID orderId;
    private String orderNumber;
    private LocalDateTime createdAtUtc;
    private BigDecimal totalAmount;
    private Integer totalItems;
    private Integer estimatedPackageCount;
    private String itemSummary;
    private String city;
    private String state;
    private String postalCode;
    private Double approximateDistanceKm;
}