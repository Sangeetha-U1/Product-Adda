package com.productadda.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import com.productadda.constants.PublicRoutes;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

        /*
         * ================================================================
         * SECURITY CONFIGURATION
         * ================================================================
         *
         * CURRENT PHASE:
         * - Stateless REST APIs
         * - Public routes allowed without authentication
         *
         * FUTURE PHASE:
         * - JWT Authentication Filter
         * - Role based authorization (SUPER_ADMIN / ADMIN / USER / etc)
         * ================================================================
         */

        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http)
                        throws Exception {

                http

                                /*
                                 * ============================================================
                                 * CSRF DISABLED
                                 * ============================================================
                                 * Reason: REST APIs are stateless (JWT will be used later)
                                 */
                                .csrf(csrf -> csrf.disable())

                                /*
                                 * ============================================================
                                 * SESSION MANAGEMENT
                                 * ============================================================
                                 * Stateless because authentication will be token-based (JWT)
                                 */
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                .exceptionHandling(exception -> exception
                                                .authenticationEntryPoint(customAuthenticationEntryPoint))

                                /*
                                 * ============================================================
                                 * AUTHORIZATION RULES
                                 * ============================================================
                                 */
                                .authorizeHttpRequests(auth -> auth

                                                // Public endpoints
                                                .requestMatchers(PublicRoutes.PUBLIC_URLS)
                                                .permitAll()

                                                // All other endpoints require authentication
                                                .anyRequest()
                                                .authenticated())

                                .addFilterBefore(
                                                jwtAuthenticationFilter,
                                                UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}