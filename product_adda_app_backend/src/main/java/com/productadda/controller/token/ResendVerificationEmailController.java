package com.productadda.controller.token;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.token.ResendVerificationEmailRequestDto;
import com.productadda.dto.token.ResendVerificationEmailResponseDto;
import com.productadda.service.token.ResendVerificationEmailService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/token")
@RequiredArgsConstructor
public class ResendVerificationEmailController {

    private final ResendVerificationEmailService resendVerificationEmailService;

    @PostMapping("/resend-verification-email")
    public ResponseEntity<ApiSuccessResponseDto<ResendVerificationEmailResponseDto>> resendVerificationEmail(
            @Valid @RequestBody ResendVerificationEmailRequestDto requestDto) {

        ResendVerificationEmailResponseDto response = resendVerificationEmailService.resend(requestDto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        ApiSuccessResponseDto.<ResendVerificationEmailResponseDto>builder()
                                .success(true)
                                .message("Verification email sent successfully")
                                .data(response)
                                .build());

    }

}