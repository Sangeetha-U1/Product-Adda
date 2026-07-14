package com.productadda.dto.product;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminProductResubmissionResponseDto {

    private UUID productId;
    private String productName;
    private String status;
    private String resubmittedAtUtc;
    private String resubmittedByAdmin;
}