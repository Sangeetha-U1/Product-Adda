package com.productadda.dto.vendor;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
 * ================================================================
 * NESTED ITEM DTO
 * Description: Sanitized, vendor-scoped line item detail nested
 * inside a VendorOrderItemListDto. Represents ONLY this vendor's
 * items within a potentially multi-vendor order.
 * NOTE: This class was not explicitly named in the
 * file tree, but is required by the Nested Item DTO Rule since
 * standalone helper mapping methods are prohibited.
 * ================================================================
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorOrderItemDetailDto {

    private UUID orderItemId;

    private UUID productId;

    private String productNameSnapshot;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal lineTotal;

    private String itemStatusName;
}
