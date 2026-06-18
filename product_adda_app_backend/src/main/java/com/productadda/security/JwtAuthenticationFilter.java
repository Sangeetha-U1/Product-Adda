package com.productadda.security;

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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

        private final JwtService jwtService;
        private final UserDetailsService userDetailsService;

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
                        userEmail = jwtService.extractEmailFromToken(jwt); // Throws exception if expired/invalid

                        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                                if (jwtService.isTokenValid(jwt)) {
                                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                                        userDetails, null, userDetails.getAuthorities());
                                        authToken.setDetails(
                                                        new WebAuthenticationDetailsSource().buildDetails(request));
                                        SecurityContextHolder.getContext().setAuthentication(authToken);
                                }
                        }
                } catch (ExpiredJwtException e) {
                        // Set attributes so the EntryPoint can pick them up dynamically
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