package com.productadda.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import com.productadda.service.token.TokenProvider;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider.JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        /*
         * ============================================================
         * 1. AUTHORIZATION HEADER EXTRACTION
         * ============================================================
         */
        final String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        /*
         * ============================================================
         * 2. TOKEN EXTRACTION
         * ============================================================
         */
        String jwtToken = authorizationHeader.substring(7);

        /*
         * ============================================================
         * 3. TOKEN VALIDATION
         * ============================================================
         */
        if (!jwtService.isTokenValid(jwtToken)) {

            filterChain.doFilter(request, response);
            return;
        }

        /*
         * ============================================================
         * 4. USER EXTRACTION
         * ============================================================
         */
        String email = jwtService.extractEmailFromToken(jwtToken);

        /*
         * ============================================================
         * 5. SECURITY CONTEXT AUTHENTICATION
         * ============================================================
         */
        if (email != null
                && SecurityContextHolder.getContext()
                        .getAuthentication() == null) {

            UserDetails userDetails = customUserDetailsService
                    .loadUserByUsername(email);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities());

            authentication.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request));

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);
        }

        /*
         * ============================================================
         * 6. CONTINUE FILTER CHAIN
         * ============================================================
         */
        filterChain.doFilter(request, response);
    }
}