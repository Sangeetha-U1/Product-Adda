package com.productadda.controller.token;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.token.RefreshTokenRequestDto;
import com.productadda.dto.token.RefreshTokenResponseDto;
import com.productadda.service.token.RefreshTokenService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/token")
@RequiredArgsConstructor
public class RefreshTokenController {

        private final RefreshTokenService refreshTokenService;

        // TODO: refresh token should be generated only if it got expired or revoked, if
        // not just serve the same refresh token
        @PostMapping("/refresh-token")
        public ResponseEntity<ApiSuccessResponseDto<RefreshTokenResponseDto>> refreshToken(
                        @RequestBody RefreshTokenRequestDto request) {

                RefreshTokenResponseDto response = refreshTokenService.rotateRefreshToken(request);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(
                                                ApiSuccessResponseDto.<RefreshTokenResponseDto>builder()
                                                                .success(true)
                                                                .message("Token refreshed successfully")
                                                                .data(response)
                                                                .build());
        }
}