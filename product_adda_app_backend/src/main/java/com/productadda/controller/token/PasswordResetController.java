package com.productadda.controller.token;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.token.ResetPasswordRequestDto;
import com.productadda.dto.token.ResetPasswordResponseDto;
import com.productadda.service.token.PasswordResetService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/token")
@RequiredArgsConstructor
public class PasswordResetController {

        private final PasswordResetService passwordResetService;

        /*
         * =========================================================
         * RESET PASSWORD API
         * =========================================================
         */
        @PostMapping("/reset-password")
        public ResponseEntity<ApiSuccessResponseDto<ResetPasswordResponseDto>> resetPassword(
                        @Valid @RequestBody ResetPasswordRequestDto request) {

                ResetPasswordResponseDto response = passwordResetService.resetPassword(request);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(
                                                ApiSuccessResponseDto.<ResetPasswordResponseDto>builder()
                                                                .success(true)
                                                                .message(
                                                                                "Password reset successful")
                                                                .data(response)
                                                                .build());
        }

}