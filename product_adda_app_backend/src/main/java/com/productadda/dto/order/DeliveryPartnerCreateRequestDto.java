package com.productadda.dto.order;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
 * ================================================================
 * REQUEST DTO
 * Description: Payload for admin-initiated delivery partner
 * onboarding. Phone pattern matches the exact regex already used in
 * RegisterRequestDto for consistency across the project (10 raw
 * digits, no country code or separators).
 * ================================================================
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryPartnerCreateRequestDto {

    @NotBlank(message = "partnerName is required")
    @Size(min = 1, max = 255, message = "partnerName must be between 1 and 255 characters")
    private String partnerName;

    @NotBlank(message = "firstName is required")
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "lastName is required")
    @Size(max = 100)
    private String lastName;

    @NotBlank(message = "email is required")
    @Email(message = "email must be a valid email address")
    @Size(max = 255, message = "email must not exceed 255 characters")
    private String email;

    @NotBlank(message = "password is required")
    private String password;

    @NotBlank(message = "phone is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "phone must be 10 digits")
    private String phone;

    @NotNull(message = "maxConcurrentDeliveries is required")
    @Min(value = 1, message = "maxConcurrentDeliveries must be greater than 0")
    @Max(value = 1000, message = "maxConcurrentDeliveries must not exceed 1000")
    private Integer maxConcurrentDeliveries;

    @Size(max = 500, message = "currentLocation must not exceed 500 characters")
    private String currentLocation;
}
