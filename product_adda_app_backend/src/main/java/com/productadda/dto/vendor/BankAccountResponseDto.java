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
public class BankAccountResponseDto {

    private UUID vendorBankDetailId;
    private UUID vendorId;
    private String accountHolderName;
    private String bankName;
    private String maskedAccountNumber;
    private String ifscCode;
    private String branchName;
    private Boolean isActive;
}