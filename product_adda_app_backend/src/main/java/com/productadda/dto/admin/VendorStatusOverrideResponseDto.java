package com.productadda.dto.admin;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorStatusOverrideResponseDto {

    private UUID vendorId;
    private Boolean isActive;
}