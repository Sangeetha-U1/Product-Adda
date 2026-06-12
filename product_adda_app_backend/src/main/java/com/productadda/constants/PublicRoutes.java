package com.productadda.constants;

public class PublicRoutes {

    private PublicRoutes() {
    }

    public static final String[] PUBLIC_URLS = {

            // Helath
            "/api/health/app",
            "/api/health/db",
            // TODO: Only for testing, delete later.
            "/api/health/repository",

            // Auth APIs
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/forgot-password",
            "/api/auth/reset-password",
            "/api/auth/logout",

            // token
            "/api/token/verify-email",
            "/api/token/refresh-token",
            "/api/token/resend-verification-email",

            // Admin Auth APIs
            "/api/admin/auth/register",
            "/api/admin/auth/verify-email",
            "/api/admin/auth/login",
            "/api/admin/auth/logout",

            "/api/admin/users",

            // Swagger
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**"
    };
}
