package com.productadda.dto.token;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ResendVerificationEmailResponseDto {

    private UUID userId;

    private String email;

    private Boolean emailVerificationRequired;

    private String message;
}