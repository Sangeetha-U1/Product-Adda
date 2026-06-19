package com.productadda.dto.order;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemSummaryDto {
    private UUID orderItemId;
    private UUID productId;
    private String productNameSnapshot;
    private Integer quantity;
    private BigDecimal unitPrice;
}