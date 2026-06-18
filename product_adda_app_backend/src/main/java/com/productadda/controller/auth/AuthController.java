package com.productadda.controller.auth;

import com.productadda.dto.auth.RegisterRequestDto;
import com.productadda.dto.auth.RegisterResponseDto;
import com.productadda.service.auth.AuthServiceRegister;

import jakarta.validation.Valid;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.auth.LoginRequestDto;
import com.productadda.dto.auth.LoginResponseDto;
import com.productadda.service.auth.AuthServiceForgotPassword;
import com.productadda.service.auth.AuthServiceLogin;
import com.productadda.dto.auth.LogoutRequestDto;
import com.productadda.dto.auth.LogoutResponseDto;
import com.productadda.dto.auth.UserProfileResponseDto;
import com.productadda.dto.token.ForgotPasswordRequestDto;
import com.productadda.dto.token.ForgotPasswordResponsetDto;
import com.productadda.service.auth.AuthServiceLogout;
import com.productadda.service.auth.AuthServiceMe;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

        private final AuthServiceRegister authServiceRegister;
        private final AuthServiceLogin authServiceLogin;
        private final AuthServiceLogout authServiceLogout;
        private final AuthServiceMe authServiceMe;
        private final AuthServiceForgotPassword authServiceForgotPassword;

        @PostMapping("/register")
        public ResponseEntity<ApiSuccessResponseDto<RegisterResponseDto>> register(
                        @RequestBody RegisterRequestDto request) {

                RegisterResponseDto response = authServiceRegister.register(request);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(
                                                ApiSuccessResponseDto.<RegisterResponseDto>builder()
                                                                .success(true)
                                                                .message("User registered successfully")
                                                                .data(response)
                                                                .build());
        }

        @PostMapping("/login")
        public ResponseEntity<ApiSuccessResponseDto<LoginResponseDto>> login(
                        @RequestBody LoginRequestDto request) {

                LoginResponseDto response = authServiceLogin.login(request);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(
                                                ApiSuccessResponseDto.<LoginResponseDto>builder()
                                                                .success(true)
                                                                .message("User authenticated successfully")
                                                                .data(response)
                                                                .build());
        }

        @GetMapping("/me")
        public ResponseEntity<ApiSuccessResponseDto<UserProfileResponseDto>> me() {

                UserProfileResponseDto response = authServiceMe.getMe();

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(
                                                ApiSuccessResponseDto.<UserProfileResponseDto>builder()
                                                                .success(true)
                                                                .message("Profile fetched successfully")
                                                                .data(response)
                                                                .build());
        }

        @PostMapping("/logout")
        public ResponseEntity<ApiSuccessResponseDto<LogoutResponseDto>> logout(
                        @RequestBody LogoutRequestDto request) {

                LogoutResponseDto response = authServiceLogout.logout(request);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(
                                                ApiSuccessResponseDto.<LogoutResponseDto>builder()
                                                                .success(true)
                                                                .message("Logout successful")
                                                                .data(response)
                                                                .build());
        }

        @PostMapping("/forgot-password")
        public ResponseEntity<ApiSuccessResponseDto<ForgotPasswordResponsetDto>> forgotPassword(
                        @Valid @RequestBody ForgotPasswordRequestDto request) {

                ForgotPasswordResponsetDto response = authServiceForgotPassword.forgotPassword(request);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(
                                                ApiSuccessResponseDto.<ForgotPasswordResponsetDto>builder()
                                                                .success(true)
                                                                .message(
                                                                                "Password reset email sent")
                                                                .data(response)
                                                                .build());
        }

}