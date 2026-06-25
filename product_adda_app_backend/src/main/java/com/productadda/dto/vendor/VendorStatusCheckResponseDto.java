package com.productadda.dto.vendor;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorStatusCheckResponseDto {

    /*
     * ================================================================
     * VENDOR IDENTITY
     * ================================================================
     */
    private UUID vendorId;

    /*
     * ================================================================
     * STATUS FLAGS
     * ================================================================
     */
    private Boolean isVendor;
    private Boolean isActive;
}