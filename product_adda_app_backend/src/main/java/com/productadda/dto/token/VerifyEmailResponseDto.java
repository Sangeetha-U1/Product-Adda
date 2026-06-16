package com.productadda.dto.token;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class VerifyEmailResponseDto {

    private UUID userId;

    private String email;

    private Boolean emailVerified;

    private Boolean isActive;
}