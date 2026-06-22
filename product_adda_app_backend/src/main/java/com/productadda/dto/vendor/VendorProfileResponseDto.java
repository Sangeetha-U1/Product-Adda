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
public class VendorProfileResponseDto {

    private UUID vendorId;
    private UUID userId;
    private String businessName;
    private String storeName;
    private String gstNumber;
    private String businessDescription;
    private Boolean isActive;
}