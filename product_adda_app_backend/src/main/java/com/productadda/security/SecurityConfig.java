package com.productadda.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.productadda.constants.PublicRoutes;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity // Explicitly enables the web security filter chain mapping globally
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

        @Value("${app.frontend.base-url}")
        private String frontendBaseUrl;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                /*
                                 * ============================================================
                                 * CORS & CSRF CONFIGURATION
                                 * ============================================================
                                 */
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .csrf(csrf -> csrf.disable())

                                /*
                                 * ============================================================
                                 * EXCEPTION HANDLING & SESSION MANAGEMENT
                                 * ============================================================
                                 */
                                .exceptionHandling(exception -> exception
                                                .authenticationEntryPoint(customAuthenticationEntryPoint))
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                /*
                                 * ============================================================
                                 * AUTHORIZATION PATH RULES
                                 * ============================================================
                                 */
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(PublicRoutes.PUBLIC_URLS).permitAll()
                                                .anyRequest().authenticated())

                                /*
                                 * ============================================================
                                 * JWT FILTER INTEGRATION
                                 * ============================================================
                                 */
                                .addFilterBefore(jwtAuthenticationFilter,
                                                UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        /*
         * ================================================================
         * CORS CONFIGURATION SOURCE
         * Description: Prevents browser blocks by letting your specific frontend
         * base-url
         * send cross-origin authorization headers and payloads safely.
         * ================================================================
         */
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();

                // Maps security permissions to match your verified frontend source deployment
                // domain
                configuration.setAllowedOrigins(List.of(frontendBaseUrl, "http://localhost:5173"));
                configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Cache-Control"));
                configuration.setExposedHeaders(List.of("Authorization"));
                configuration.setAllowCredentials(true);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }
}