package com.productadda.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.entity.User;
import com.productadda.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

        private final UserRepository userRepository;

        @Override
        @Transactional(readOnly = true)
        public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

                // 1. Dynamic DB role check: only fetch role names where user_roles mapping is
                // active
                String[] authorities = user.getUserRoles().stream()
                                .filter(userRole -> Boolean.TRUE.equals(userRole.getIsActive()))
                                .map(userRole -> userRole.getFkRole().getRoleName().toUpperCase())
                                .toArray(String[]::new);

                boolean userIsActive = Boolean.TRUE.equals(user.getIsActive());

                // 2. Dynamic state matching without modifying your database schema
                return org.springframework.security.core.userdetails.User
                                .withUsername(user.getEmail())
                                .password(user.getPasswordHash())
                                .authorities(authorities)
                                .accountExpired(!userIsActive)
                                .accountLocked(!userIsActive)
                                .credentialsExpired(!userIsActive)
                                .disabled(!userIsActive)
                                .build();
        }
}