package com.productadda.dto.wishlist;

import lombok.Getter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistPriceHistoryDto {

    /*
     * =========================================================
     * CORE FIELDS
     * =========================================================
     */
    private UUID historyId;
    
    private BigDecimal priceSnapshot;
    
    private BigDecimal priceDropPercentage;
    
    private LocalDateTime snapshotAtUtc;
}