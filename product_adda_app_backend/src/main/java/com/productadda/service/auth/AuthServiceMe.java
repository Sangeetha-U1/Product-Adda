package com.productadda.service.auth;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.auth.UserProfileResponseDto;
import com.productadda.entity.User;
import com.productadda.exception.ApiException;
import com.productadda.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceMe {

    private final UserRepository userRepository;

    @Transactional
    public UserProfileResponseDto getMe() {

        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        String email;

        if (authentication.getPrincipal() instanceof UserDetails userDetails) {
            email = userDetails.getUsername();
        } else {
            email = authentication.getName();
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Authenticated user no longer exists"));

        return UserProfileResponseDto.builder()
                .userId(user.getPkUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .isEmailVerified(user.getEmailVerified())
                .isActive(user.getIsActive())
                .roleName(
                        user.getUserRoles()
                                .stream()
                                .findFirst()
                                .map(userRole -> userRole.getFkRole().getRoleName())
                                .orElse(null))
                .build();
    }
}