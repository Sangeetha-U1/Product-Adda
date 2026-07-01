package com.productadda.dto.product;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminProductRejectionRequestDto {

    @NotBlank(message = "Rejection reason is required and cannot be blank")
    private String rejectionReason;
}