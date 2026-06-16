package com.productadda.dto.auth;

import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegisterResponseDto {

    private UUID userId;

    private String email;

    private String roleName;

}