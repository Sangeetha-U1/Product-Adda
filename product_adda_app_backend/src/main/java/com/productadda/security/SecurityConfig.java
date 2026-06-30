package com.productadda.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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
@EnableWebSecurity
@EnableMethodSecurity
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
                                 * AUTHORIZATION PATH RULES (Dynamic Registry Loop)
                                 * ============================================================
                                 */
                                .authorizeHttpRequests(auth -> {
                                        // Dynamically unpacks and registers paths alongside their explicit HttpMethods
                                        for (PublicRoutes.PublicConfig route : PublicRoutes.CONFIGS) {
                                                if (route.allowsAllMethods()) {
                                                        auth.requestMatchers(route.getPattern()).permitAll();
                                                } else {
                                                        for (HttpMethod method : route.getMethods()) {
                                                                auth.requestMatchers(method, route.getPattern()).permitAll();
                                                        }
                                                }
                                        }
                                        // All non-registered method paths require valid credentials
                                        auth.anyRequest().authenticated();
                                })

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
         * ================================================================
         */
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();

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