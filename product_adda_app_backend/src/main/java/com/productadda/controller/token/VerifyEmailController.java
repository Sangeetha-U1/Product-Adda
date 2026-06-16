package com.productadda.controller.token;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.token.VerifyEmailRequestDto;
import com.productadda.dto.token.VerifyEmailResponseDto;
import com.productadda.service.token.EmailVerificationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/token")
@RequiredArgsConstructor
public class VerifyEmailController {

    private final EmailVerificationService emailVerificationService;

    @PostMapping("/verify-email")
    public ResponseEntity<ApiSuccessResponseDto<VerifyEmailResponseDto>> verifyEmail(
            @Valid @RequestBody VerifyEmailRequestDto requestDto) {

        VerifyEmailResponseDto response = emailVerificationService.verifyEmail(requestDto);

        return ResponseEntity.ok(
                ApiSuccessResponseDto.<VerifyEmailResponseDto>builder()
                        .success(true)
                        .message("Email verified successfully")
                        .data(response)
                        .build());
    }

}