package com.productadda.constants;

public class PublicRoutes {

    private PublicRoutes() {
    }

    public static final String[] PUBLIC_URLS = {

            // Helath
            "/api/health/app",
            "/api/health/db",

            // Auth APIs
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/forgot-password",
            "/api/auth/reset-password",

            // token
            "/api/token/verify-email",
            "/api/token/resend-verification-email",

            // Admin Auth APIs
            "/api/admin/auth/register",
            "/api/admin/auth/verify-email",
            "/api/admin/auth/login",

            // Swagger
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",

            // Payments heatlh
            "/api/payment/health"
    };
}
