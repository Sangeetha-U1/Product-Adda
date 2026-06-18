package com.productadda.dto.token;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForgotPasswordRequestDto {

    /*
     * =========================================================
     * EMAIL
     * =========================================================
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

}