package com.productadda.controller.vendor;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.vendor.VendorRegisterRequestDto;
import com.productadda.dto.vendor.VendorProfileResponseDto;
import com.productadda.service.vendor.VendorRegisterService;
import com.productadda.service.vendor.VendorProfileService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/vendors")
@RequiredArgsConstructor
public class VendorController {

        private final VendorRegisterService vendorRegisterService;
        private final VendorProfileService vendorProfileService;

        @PostMapping("/register")
        public ResponseEntity<ApiSuccessResponseDto<VendorProfileResponseDto>> registerVendor(
                        @Valid @RequestBody VendorRegisterRequestDto request, Principal principal) {

                VendorProfileResponseDto responseDataInstance = vendorRegisterService.registerVendor(request,
                                principal.getName());

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(ApiSuccessResponseDto.<VendorProfileResponseDto>builder()
                                                .success(true)
                                                .message("Vendor application processed and active role assigned successfully")
                                                .data(responseDataInstance)
                                                .build());
        }

        @GetMapping("/profile")
        public ResponseEntity<ApiSuccessResponseDto<VendorProfileResponseDto>> getVendorProfile(Principal principal) {

                VendorProfileResponseDto responseDataInstance = vendorProfileService
                                .getVendorProfile(principal.getName());

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(ApiSuccessResponseDto.<VendorProfileResponseDto>builder()
                                                .success(true)
                                                .message("Merchant profile fetched successfully")
                                                .data(responseDataInstance)
                                                .build());
        }

        @PutMapping("/profile")
        public ResponseEntity<ApiSuccessResponseDto<VendorProfileResponseDto>> updateVendorProfile(
                        @Valid @RequestBody VendorRegisterRequestDto request, Principal principal) {

                VendorProfileResponseDto responseDataInstance = vendorProfileService.updateVendorProfile(request,
                                principal.getName());

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(ApiSuccessResponseDto.<VendorProfileResponseDto>builder()
                                                .success(true)
                                                .message("Merchant profile updated successfully")
                                                .data(responseDataInstance)
                                                .build());
        }
}