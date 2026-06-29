package com.productadda.security;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.productadda.service.token.TokenProvider.JwtService;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

        private final JwtService jwtService;

        @Override
        protected void doFilterInternal(
                        HttpServletRequest request,
                        HttpServletResponse response,
                        FilterChain filterChain) throws ServletException, IOException {

                final String authHeader = request.getHeader("Authorization");
                final String jwt;
                final String userEmail;

                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                        filterChain.doFilter(request, response);
                        return;
                }

                try {
                        jwt = authHeader.substring(7);
                        userEmail = jwtService.extractEmailFromToken(jwt);

                        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                                if (jwtService.isTokenValid(jwt)) {
                                        // 1. Extract multi-roles array from verified token claims
                                        List<String> roles = jwtService.extractRolesFromToken(jwt);

                                        // 2. Map cleanly to GrantedAuthorities for your guard clauses
                                        List<SimpleGrantedAuthority> authorities = roles.stream()
                                                        .map(role -> new SimpleGrantedAuthority(role.toUpperCase()))
                                                        .collect(Collectors.toList());

                                        // 3. Populate Context Authentication with your claims data
                                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                                        userEmail, null, authorities);

                                        authToken.setDetails(
                                                        new WebAuthenticationDetailsSource().buildDetails(request));

                                        SecurityContextHolder.getContext().setAuthentication(authToken);
                                }
                        }
                } catch (ExpiredJwtException e) {
                        request.setAttribute("jwt_error_type", "TOKEN_EXPIRED");
                        request.setAttribute("jwt_error_message",
                                        "The provided access token has expired. Please use a refresh token.");
                } catch (MalformedJwtException | SignatureException e) {
                        request.setAttribute("jwt_error_type", "INVALID_TOKEN");
                        request.setAttribute("jwt_error_message",
                                        "The provided token is malformed or signature validation failed.");
                } catch (Exception e) {
                        request.setAttribute("jwt_error_type", "UNAUTHORIZED");
                        request.setAttribute("jwt_error_message", "Authentication failed.");
                }

                filterChain.doFilter(request, response);
        }
}