package com.productadda.controller.admin;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.admin.AdminLoginRequestDto;
import com.productadda.dto.admin.AdminLoginResponseDto;
import com.productadda.service.admin.AdminLoginService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth/admin")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminLoginService adminLoginService;

    @PostMapping("/login")
    public ResponseEntity<ApiSuccessResponseDto<AdminLoginResponseDto>> login(
            @Valid @RequestBody AdminLoginRequestDto request) {

        AdminLoginResponseDto response = adminLoginService.login(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiSuccessResponseDto.<AdminLoginResponseDto>builder()
                        .success(true)
                        .message("Administrative context authenticated successfully")
                        .data(response)
                        .build());
    }
}