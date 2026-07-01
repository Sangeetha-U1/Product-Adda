package com.productadda.dto.product;

import java.util.UUID;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminProductApprovalResponseDto {
    private UUID productId;
    private String productName;
    private String status;
    private String approvedAtUtc;
    private String approvedByAdmin;
    private String rejectionReason;
    private String rejectedAtUtc;
    private String rejectedByAdmin;
}