package com.productadda.dto.token;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequestDto {

    /*
     * =========================================================
     * RESET TOKEN
     * =========================================================
     */
    @NotBlank(message = "Reset token is required")
    private String token;

    /*
     * =========================================================
     * NEW PASSWORD
     * =========================================================
     */
    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "Password must contain minimum 8 characters")
    private String newPassword;

}