package com.productadda.dto.auth;

import java.util.UUID;

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
public class UserProfileResponseDto {

    private UUID userId;

    private String firstName;

    private String lastName;

    private String email;

    private String mobile;

    private Boolean isEmailVerified;

    private Boolean isActive;

    private String roleName;
}