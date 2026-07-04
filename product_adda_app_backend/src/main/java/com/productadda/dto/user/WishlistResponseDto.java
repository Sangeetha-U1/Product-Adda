package com.productadda.dto.user;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistResponseDto {
    private UUID wishlistItemId;
    private UUID productId;
    private String productName;
    private BigDecimal price;          
    private BigDecimal priceAtAdd;     
    private LocalDateTime expiresAtUtc;
    private String brandName;
}