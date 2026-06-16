package com.productadda.controller.auth;

import com.productadda.dto.auth.RegisterRequestDto;
import com.productadda.dto.auth.RegisterResponseDto;
import com.productadda.service.auth.AuthServiceRegister;
import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.auth.LoginRequestDto;
import com.productadda.dto.auth.LoginResponseDto;
import com.productadda.service.auth.AuthServiceLogin;
import com.productadda.dto.auth.LogoutRequestDto;
import com.productadda.dto.auth.UserProfileResponseDto;
import com.productadda.service.auth.AuthServiceLogout;
import com.productadda.service.auth.AuthServiceMe;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

        /*
         * ================================================================
         * AUTH APIS
         * ================================================================
         *
         * POST /api/auth/register
         * - public endpoint
         * - create new account
         *
         * POST /api/auth/login
         * - public endpoint
         * - authenticate user
         *
         * FUTURE
         *
         * POST /api/auth/logout
         * POST /api/auth/forgot-password
         * POST /api/auth/reset-password
         *
         * ================================================================
         */

        private final AuthServiceRegister authServiceRegister;
        private final AuthServiceLogin authServiceLogin;
        private final AuthServiceLogout authServiceLogout;
        private final AuthServiceMe authServiceMe;

        @PostMapping("/register")
        public ResponseEntity<ApiSuccessResponseDto<RegisterResponseDto>> register(
                        @RequestBody RegisterRequestDto request) {

                RegisterResponseDto response = authServiceRegister.register(request);

                return ResponseEntity.ok(
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

                return ResponseEntity.ok(
                                ApiSuccessResponseDto.<LoginResponseDto>builder()
                                                .success(true)
                                                .message("User authenticated successfully")
                                                .data(response)
                                                .build());
        }

        @GetMapping("/me")
        public ResponseEntity<ApiSuccessResponseDto<UserProfileResponseDto>> me() {

                UserProfileResponseDto response = authServiceMe.getMe();

                return ResponseEntity.ok(
                                ApiSuccessResponseDto.<UserProfileResponseDto>builder()
                                                .success(true)
                                                .message("Profile fetched successfully")
                                                .data(response)
                                                .build());
        }

        @PostMapping("/logout")
        public ResponseEntity<ApiSuccessResponseDto<Void>> logout(
                        @RequestBody LogoutRequestDto request) {

                authServiceLogout.logout(request);

                return ResponseEntity.ok(
                                ApiSuccessResponseDto.<Void>builder()
                                                .success(true)
                                                .message("Logout successful")
                                                .build());
        }

}