package com.productadda.service.vendor;

import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
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
         * REGISTER BANK ACCOUNT
         * Description: Validates and configures a new primary financial settlement
         * container for a verified merchant profile context.
         * ================================================================
         */
        @Transactional
        public BankAccountResponseDto registerBankAccount(BankAccountRequestDto requestDto) {

                /*
                 * ================================================================
                 * 1. VALIDATION SECTION
                 * ================================================================
                 */

                // ==========================================
                // 1.1 REQUEST VALIDATION
                // ==========================================
                if (requestDto == null) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Bank account request body cannot be null");
                }
                if (requestDto.getIfscCode() == null || !IFSC_PATTERN.matcher(requestDto.getIfscCode()).matches()) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid IFSC code routing signature format");
                }
                if (requestDto.getAccountNumber() == null || requestDto.getAccountNumber().length() < 9
                                || requestDto.getAccountNumber().length() > 18) {
                        throw new ApiException(HttpStatus.BAD_REQUEST,
                                        "Account number layout violation: must span between 9 and 18 positions");
                }

                // ==========================================
                // 1.2 CONTEXT AUTHENTICATION
                // ==========================================
                if (SecurityContextHolder.getContext().getAuthentication() == null ||
                                SecurityContextHolder.getContext().getAuthentication().getName() == null ||
                                SecurityContextHolder.getContext().getAuthentication().getName().trim().isEmpty()) {
                        throw new ApiException(HttpStatus.UNAUTHORIZED, "Principal system execution identity missing");
                }
                String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

                // ==========================================
                // 1.3 DATABASE LOOKUP VALIDATION
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

                /*
                 * ================================================================
                 * 2. BUSINESS RULES & PROCESSING / WORKFLOW
                 * ================================================================
                 */
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

                /*
                 * ================================================================
                 * 3. DB SAVING SECTION
                 * ================================================================
                 */
                VendorBankDetail savedDetails = bankDetailRepository.save(bankDetail);

                /*
                 * ================================================================
                 * 4 POST-SAVING DATA SANITIZATION & MASKING
                 * ================================================================
                 */
                String rawAccount = savedDetails.getAccountNumber();
                String maskedAccount = rawAccount;

                if (rawAccount != null && rawAccount.length() >= 4) {
                        String visiblePortion = rawAccount.substring(rawAccount.length() - 4);
                        maskedAccount = "X".repeat(rawAccount.length() - 4) + visiblePortion;
                }

                /*
                 * ================================================================
                 * 5. RESPONSE MAPPING
                 * ================================================================
                 */
                return BankAccountResponseDto.builder()
                                .vendorBankDetailId(savedDetails.getPkVendorBankDetailId())
                                .vendorId(savedDetails.getFkVendor().getPkVendorId())
                                .accountHolderName(savedDetails.getAccountHolderName())
                                .bankName(savedDetails.getBankName())
                                .maskedAccountNumber(maskedAccount)
                                .ifscCode(savedDetails.getIfscCode())
                                .branchName(savedDetails.getBranchName())
                                .isActive(savedDetails.isActive())
                                .build();
        }

        /*
         * ================================================================
         * GET BANK ACCOUNT DETAILS
         * Description: Securely fetches individual active settlement settings for the
         * contextual merchant principal, applying response data masking filters.
         * ================================================================
         */
        @Transactional(readOnly = true)
        public BankAccountResponseDto getBankAccountDetails() {

                /*
                 * ================================================================
                 * 1. VALIDATION SECTION
                 * ================================================================
                 */

                // ==========================================
                // 1.1 REQUEST VALIDATION
                // ==========================================
                // No input payload coordinates; purely read-only structural query context.

                // ==========================================
                // 1.2 CONTEXT AUTHENTICATION
                // ==========================================
                if (SecurityContextHolder.getContext().getAuthentication() == null ||
                                SecurityContextHolder.getContext().getAuthentication().getName() == null ||
                                SecurityContextHolder.getContext().getAuthentication().getName().trim().isEmpty()) {
                        throw new ApiException(HttpStatus.UNAUTHORIZED, "Principal system execution identity missing");
                }
                String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

                // ==========================================
                // 1.3 DATABASE LOOKUP VALIDATION
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

                /*
                 * ================================================================
                 * 2. BUSINESS RULES & PROCESSING / WORKFLOW
                 * ================================================================
                 */
                String rawAccount = bankDetail.getAccountNumber();
                String maskedAccount = rawAccount;

                if (rawAccount != null && rawAccount.length() >= 4) {
                        String visiblePortion = rawAccount.substring(rawAccount.length() - 4);
                        maskedAccount = "X".repeat(rawAccount.length() - 4) + visiblePortion;
                }

                /*
                 * ================================================================
                 * 4. RESPONSE MAPPING
                 * ================================================================
                 */
                return BankAccountResponseDto.builder()
                                .vendorBankDetailId(bankDetail.getPkVendorBankDetailId())
                                .vendorId(bankDetail.getFkVendor().getPkVendorId())
                                .accountHolderName(bankDetail.getAccountHolderName())
                                .bankName(bankDetail.getBankName())
                                .maskedAccountNumber(maskedAccount)
                                .ifscCode(bankDetail.getIfscCode())
                                .branchName(bankDetail.getBranchName())
                                .isActive(bankDetail.isActive())
                                .build();
        }

        /*
         * ================================================================
         * UPDATE BANK ACCOUNT DETAILS
         * Description: Enforces systematic overrides on previous configurations while
         * triggering compliance-driven profile status modifications.
         * ================================================================
         */
        @Transactional
        public BankAccountResponseDto updateBankAccountDetails(BankAccountRequestDto requestDto) {

                /*
                 * ================================================================
                 * 1. VALIDATION SECTION
                 * ================================================================
                 */

                // ==========================================
                // 1.1 REQUEST VALIDATION
                // ==========================================
                if (requestDto == null) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Bank account request body cannot be null");
                }
                if (requestDto.getIfscCode() == null || !IFSC_PATTERN.matcher(requestDto.getIfscCode()).matches()) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid IFSC code routing signature format");
                }
                if (requestDto.getAccountNumber() == null || requestDto.getAccountNumber().length() < 9
                                || requestDto.getAccountNumber().length() > 18) {
                        throw new ApiException(HttpStatus.BAD_REQUEST,
                                        "Account number layout violation: must span between 9 and 18 positions");
                }

                // ==========================================
                // 1.2 CONTEXT AUTHENTICATION
                // ==========================================
                if (SecurityContextHolder.getContext().getAuthentication() == null ||
                                SecurityContextHolder.getContext().getAuthentication().getName() == null ||
                                SecurityContextHolder.getContext().getAuthentication().getName().trim().isEmpty()) {
                        throw new ApiException(HttpStatus.UNAUTHORIZED, "Principal system execution identity missing");
                }
                String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

                // ==========================================
                // 1.3 DATABASE LOOKUP VALIDATION
                // ==========================================
                User userInstance = userRepository.findByEmail(currentUserEmail)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                "Authenticated user profile not found"));

                Vendor targetedVendorProfile = vendorRepository.findByFkUser(userInstance)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                "No associated vendor profile found for this user account"));

                VendorBankDetail bankDetail = bankDetailRepository.findByFkVendor(targetedVendorProfile)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                "No pre-existing settlement record discovered to overwrite"));

                /*
                 * ================================================================
                 * 2. BUSINESS RULES & PROCESSING / WORKFLOW
                 * ================================================================
                 */
                bankDetail.setAccountHolderName(requestDto.getAccountHolderName());
                bankDetail.setBankName(requestDto.getBankName());
                bankDetail.setAccountNumber(requestDto.getAccountNumber());
                bankDetail.setIfscCode(requestDto.getIfscCode());
                bankDetail.setBranchName(requestDto.getBranchName());

                // RISK PROTOCOL: Reset confirmation flags back to false to trigger
                // re-verification steps
                bankDetail.setActive(false);

                /*
                 * ================================================================
                 * 3. DB SAVING SECTION
                 * ================================================================
                 */
                VendorBankDetail updatedBankDetail = bankDetailRepository.save(bankDetail);

                /*
                 * ================================================================
                 * 4 POST-SAVING DATA SANITIZATION & MASKING
                 * ================================================================
                 */
                String rawAccount = updatedBankDetail.getAccountNumber();
                String maskedAccount = rawAccount;

                if (rawAccount != null && rawAccount.length() >= 4) {
                        String visiblePortion = rawAccount.substring(rawAccount.length() - 4);
                        maskedAccount = "X".repeat(rawAccount.length() - 4) + visiblePortion;
                }

                /*
                 * ================================================================
                 * 5. RESPONSE MAPPING
                 * ================================================================
                 */
                return BankAccountResponseDto.builder()
                                .vendorBankDetailId(updatedBankDetail.getPkVendorBankDetailId())
                                .vendorId(updatedBankDetail.getFkVendor().getPkVendorId())
                                .accountHolderName(updatedBankDetail.getAccountHolderName())
                                .bankName(updatedBankDetail.getBankName())
                                .maskedAccountNumber(maskedAccount)
                                .ifscCode(updatedBankDetail.getIfscCode())
                                .branchName(updatedBankDetail.getBranchName())
                                .isActive(updatedBankDetail.isActive())
                                .build();
        }
}