package com.productadda.dto.admin;

import java.util.List;
import java.util.UUID;
import com.productadda.dto.token.TokenDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminLoginResponseDto {

    private UUID userId;
    private String email;
    private List<String> roles;
    private TokenDto token;
    private String message;
}