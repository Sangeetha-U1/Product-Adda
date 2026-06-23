package com.productadda.controller.vendor;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.vendor.BankAccountRequestDto;
import com.productadda.dto.vendor.BankAccountResponseDto;
import com.productadda.service.vendor.VendorBankService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/vendors/bank-account")
@RequiredArgsConstructor
public class VendorBankController {

    private final VendorBankService vendorBankService;

    @PostMapping
    public ResponseEntity<ApiSuccessResponseDto<BankAccountResponseDto>> registerBankAccount(
            @Valid @RequestBody BankAccountRequestDto requestDto, Principal principal) {

        BankAccountResponseDto responseDataInstance = vendorBankService.registerBankAccount(
                requestDto, 
                principal.getName()
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiSuccessResponseDto.<BankAccountResponseDto>builder()
                        .success(true)
                        .message("Commercial settlement bank account linked completely")
                        .data(responseDataInstance)
                        .build());
    }

    @GetMapping
    public ResponseEntity<ApiSuccessResponseDto<BankAccountResponseDto>> getBankAccountDetails(Principal principal) {

        BankAccountResponseDto responseDataInstance = vendorBankService.getBankAccountDetails(principal.getName());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiSuccessResponseDto.<BankAccountResponseDto>builder()
                        .success(true)
                        .message("Secure data verification masked account lookup complete")
                        .data(responseDataInstance)
                        .build());
    }
}