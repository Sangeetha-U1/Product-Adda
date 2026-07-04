package com.productadda.dto.wishlist;

import lombok.Getter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistDetailedResponseDto {

    /*
     * =========================================================
     * WISHLIST ITEM SNAPSHOT DATA
     * =========================================================
     */
    private UUID wishlistItemId;
    
    private UUID productId;
    
    private String productName;
    
    private BigDecimal currentPrice;          
    
    private BigDecimal priceAtAdd;         
    
    private BigDecimal priceDeltaPercentage;
    
    private BigDecimal lowestPrice;
    
    private LocalDateTime expiresAtUtc;
    
    private String brandName;
    
    /*
     * =========================================================
     * NESTED PRICE HISTORICAL LISTINGS
     * =========================================================
     */
    private List<WishlistPriceHistoryDto> priceHistory;
}