package com.productadda.constants;

public class PublicRoutes {

    private PublicRoutes() {
        // Prevents initialization of this structural constant utility class
    }

    public static final String[] PUBLIC_URLS = {

            // Health Diagnostics
            "/api/health/app",
            "/api/health/db",
            "/api/payment/health",

            // Authentication Lifecycle APIs
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/google-login",
            "/api/auth/forgot-password",

            // Token & Email Verification Lifecycle
            "/api/token/verify-email",
            "/api/token/resend-verification-email",
            "/api/token/reset-password",
            "/api/token/refresh-token",

            // Product & Inventory Catalog Lifecycle
            "/api/products/*/inventory",

            // Admin Management Lifecycle
            "/api/auth/admin/auth/register",
            "/api/auth/admin/login",

            // OpenAPI v3 & Swagger UI Static Assets
            "/swagger-ui.html",
            "/swagger-ui/index.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/v3/api-docs.yaml"
    };
}