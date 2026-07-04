package com.productadda.dto.wishlist;

import lombok.Getter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistWrapperDto {

    /*
     * =========================================================
     * PARENT WISHLIST WRAPPER METADATA
     * =========================================================
     */
    private UUID wishlistId;
    
    private Integer itemCount;
    
    private List<WishlistDetailedResponseDto> items;
}