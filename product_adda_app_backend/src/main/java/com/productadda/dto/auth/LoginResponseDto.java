package com.productadda.dto.auth;

import java.util.UUID;

import com.productadda.dto.token.TokenDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDto {

    private UUID userId;

    private String email;

    private String roleName;

    private TokenDto token;

    private String message;

}