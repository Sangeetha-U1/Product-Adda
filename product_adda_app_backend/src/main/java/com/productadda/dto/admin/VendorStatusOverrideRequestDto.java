package com.productadda.dto.admin;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorStatusOverrideRequestDto {

    @NotNull(message = "Operational status visibility flag must not be null")
    private Boolean isActive;
}