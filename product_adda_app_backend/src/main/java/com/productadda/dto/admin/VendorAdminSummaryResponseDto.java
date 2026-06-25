package com.productadda.dto.admin;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorAdminSummaryResponseDto {

    private UUID vendorId;
    private UUID userId;
    private String businessName;
    private String storeName;
    private String gstNumber;
    private String businessDescription;
    private String ownerEmail;
    private String ownerName;
    private Boolean isActive;
    private LocalDateTime createdAtUtc;
}