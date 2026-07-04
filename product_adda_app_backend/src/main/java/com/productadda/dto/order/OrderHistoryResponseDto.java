package com.productadda.dto.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderHistoryResponseDto {
    private UUID orderId;
    private String orderNumber;
    private String statusName;
    private BigDecimal subtotal; 
    private BigDecimal couponDiscount;
    private BigDecimal shippingCost;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private LocalDateTime createdAtUtc;
    private List<OrderItemSummaryDto> items;
}