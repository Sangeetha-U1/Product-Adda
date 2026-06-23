package com.productadda.service.vendor;

import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.vendor.BankAccountRequestDto;
import com.productadda.dto.vendor.BankAccountResponseDto;
import com.productadda.entity.User;
import com.productadda.entity.Vendor;
import com.productadda.entity.VendorBankDetail;
import com.productadda.exception.ApiException;
import com.productadda.repository.UserRepository;
import com.productadda.repository.VendorBankDetailRepository;
import com.productadda.repository.VendorRepository;
import com.productadda.util.UuidUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VendorBankService {

        private final VendorRepository vendorRepository;
        private final VendorBankDetailRepository bankDetailRepository;
        private final UserRepository userRepository;
        private final UuidUtil uuidUtil;

        private static final Pattern IFSC_PATTERN = Pattern.compile("^[A-Z]{4}0[A-Z0-9]{6}$");

        /*
         * ================================================================
         * 1. BANK ACCOUNT REGISTRATION LINKAGE
         * Description: Attaches commercial settlement bank data to a vendor
         * ================================================================
         */
        @Transactional
        public BankAccountResponseDto registerBankAccount(BankAccountRequestDto requestDto, String currentUserEmail) {

                // ==========================================
                // 1.1 PRINCIPAL AND IDENTITY VERIFICATION
                // ==========================================
                User userInstance = userRepository.findByEmail(currentUserEmail)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                "Authenticated user profile not found"));

                Vendor targetedVendorProfile = vendorRepository.findByFkUser(userInstance)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                "No associated vendor profile found for this user account"));

                if (bankDetailRepository.existsByFkVendor(targetedVendorProfile)) {
                        throw new ApiException(HttpStatus.BAD_REQUEST,
                                        "Settlement bank account details already registered for this vendor profile");
                }

                // ==========================================
                // 1.2 ROUTING STRUCTURAL VALIDATIONS
                // ==========================================
                if (!IFSC_PATTERN.matcher(requestDto.getIfscCode()).matches()) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid IFSC code routing signature format");
                }

                if (requestDto.getAccountNumber().length() < 9 || requestDto.getAccountNumber().length() > 18) {
                        throw new ApiException(HttpStatus.BAD_REQUEST,
                                        "Account number layout violation: must span between 9 and 18 positions");
                }

                // ==========================================
                // 1.3 ATOMIC PERSISTENCE LAYER CONVERSION
                // ==========================================
                VendorBankDetail bankDetail = VendorBankDetail.builder()
                                .pkVendorBankDetailId(uuidUtil.generateUuidV7())
                                .fkVendor(targetedVendorProfile)
                                .accountHolderName(requestDto.getAccountHolderName())
                                .bankName(requestDto.getBankName())
                                .accountNumber(requestDto.getAccountNumber())
                                .ifscCode(requestDto.getIfscCode())
                                .branchName(requestDto.getBranchName())
                                .isActive(true)
                                .build();

                VendorBankDetail savedDetails = bankDetailRepository.save(bankDetail);

                return mapToResponseDto(savedDetails);
        }

        /*
         * ================================================================
         * 2. PROTECTED ACCOUNT LEDGER DISCOVERY
         * Description: Reads active financial ledger details securely with masking
         * ================================================================
         */
        @Transactional(readOnly = true)
        public BankAccountResponseDto getBankAccountDetails(String currentUserEmail) {

                // ==========================================
                // 2.1 SECURITY PARSING LOOKUPS
                // ==========================================
                User userInstance = userRepository.findByEmail(currentUserEmail)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                "Authenticated user profile not found"));

                Vendor targetedVendorProfile = vendorRepository.findByFkUser(userInstance)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                "No associated vendor profile found for this user account"));

                VendorBankDetail bankDetail = bankDetailRepository.findByFkVendor(targetedVendorProfile)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                "No bank settlement parameters linked to this merchant container"));

                return mapToResponseDto(bankDetail);
        }

        // ==========================================
        // PRIVATE COMPLIANCE MASKING HELPERS
        // ==========================================
        private BankAccountResponseDto mapToResponseDto(VendorBankDetail details) {
                String rawAccount = details.getAccountNumber();
                String maskedAccount = rawAccount;

                if (rawAccount != null && rawAccount.length() >= 4) {
                        String visiblePortion = rawAccount.substring(rawAccount.length() - 4);
                        maskedAccount = "X".repeat(rawAccount.length() - 4) + visiblePortion;
                }

                return BankAccountResponseDto.builder()
                                .vendorBankDetailId(details.getPkVendorBankDetailId())
                                .vendorId(details.getFkVendor().getPkVendorId())
                                .accountHolderName(details.getAccountHolderName())
                                .bankName(details.getBankName())
                                .maskedAccountNumber(maskedAccount)
                                .ifscCode(details.getIfscCode())
                                .branchName(details.getBranchName())
                                .isActive(details.isActive())
                                .build();
        }
}