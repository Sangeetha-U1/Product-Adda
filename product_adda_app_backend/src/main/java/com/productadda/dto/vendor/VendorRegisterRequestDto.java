package com.productadda.dto.vendor;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorRegisterRequestDto {

    @NotBlank(message = "Business name is required")
    private String businessName;

    @NotBlank(message = "Store name is required")
    private String storeName;

    @NotBlank(message = "GST number is required")
    private String gstNumber;

    @NotBlank(message = "Business description is required")
    private String businessDescription;
}