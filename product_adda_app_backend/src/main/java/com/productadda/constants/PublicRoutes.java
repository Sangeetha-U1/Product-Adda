package com.productadda.constants;

import org.springframework.http.HttpMethod;

public class PublicRoutes {

    private PublicRoutes() {
        // Prevents initialization of this structural constant utility class
    }

    /**
     * Immutable configuration mapping an exact route pattern
     * to its allowed HTTP methods using Java Varargs.
     */
    public static class PublicConfig {
        private final String pattern;
        private final HttpMethod[] methods;

        public PublicConfig(String pattern, HttpMethod... methods) {
            this.pattern = pattern;
            this.methods = methods;
        }

        public String getPattern() {
            return pattern;
        }

        public HttpMethod[] getMethods() {
            return methods;
        }

        public boolean allowsAllMethods() {
            return methods == null || methods.length == 0;
        }
    }

    /**
     * Unified Registry defining explicitly allowed actions per endpoint pattern
     * route.
     * To allow multiple methods, simply separate them with commas.
     */
    public static final PublicConfig[] CONFIGS = {
            // Health Diagnostics (GET Only)
            new PublicConfig("/api/health/app", HttpMethod.GET),
            new PublicConfig("/api/health/db", HttpMethod.GET),
            new PublicConfig("/api/payment/health", HttpMethod.GET),
            new PublicConfig("/api/notifications/health/live", HttpMethod.GET),

            // Authentication Lifecycle APIs (POST Only)
            new PublicConfig("/api/auth/register", HttpMethod.POST),
            new PublicConfig("/api/auth/login", HttpMethod.POST),
            new PublicConfig("/api/auth/google-login", HttpMethod.POST),
            new PublicConfig("/api/auth/forgot-password", HttpMethod.POST),

            // Token & Email Verification Lifecycle
            new PublicConfig("/api/token/verify-email", HttpMethod.POST),
            new PublicConfig("/api/token/resend-verification-email", HttpMethod.POST),
            new PublicConfig("/api/token/reset-password", HttpMethod.POST),
            new PublicConfig("/api/token/refresh-token", HttpMethod.POST),

            // Product & Inventory Catalog Lifecycle (GET Only)
            new PublicConfig("/api/products/categories", HttpMethod.GET),
            new PublicConfig("/api/products/brands", HttpMethod.GET),
            new PublicConfig("/api/products/search", HttpMethod.GET),
            new PublicConfig("/api/products/filter", HttpMethod.GET),
            new PublicConfig("/api/products/*/inventory", HttpMethod.GET),

            // Admin Management Lifecycle (Supports both POST and GET as an example)
            new PublicConfig("/api/auth/admin/auth/register", HttpMethod.POST),
            new PublicConfig("/api/auth/admin/login", HttpMethod.POST),

            // Coupon Public Verification Endpoint (POST Only)
            new PublicConfig("/api/coupons/validate", HttpMethod.POST),

            // OpenAPI v3 & Swagger UI Static Assets (Empty methods array allows all
            // methods)
            new PublicConfig("/swagger-ui.html"),
            new PublicConfig("/swagger-ui/index.html"),
            new PublicConfig("/swagger-ui/**"),
            new PublicConfig("/v3/api-docs/**"),
            new PublicConfig("/v3/api-docs.yaml"),
    };
}