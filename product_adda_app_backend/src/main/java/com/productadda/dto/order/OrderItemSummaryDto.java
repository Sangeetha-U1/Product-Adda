package com.productadda.dto.order;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemSummaryDto {
    private UUID orderItemId;
    private UUID productId;
    private UUID vendorId;
    private String itemStatusName;
    private String productNameSnapshot;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
}