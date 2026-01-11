package com.example.bookverseserver.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * P0 Security Fix #18: Add security headers to all responses.
 * 
 * These headers protect against various attacks:
 * - X-Frame-Options: Prevents clickjacking by disallowing page embedding in iframes
 * - X-Content-Type-Options: Prevents MIME-type sniffing attacks
 * - X-XSS-Protection: Legacy XSS filter (for older browsers)
 * - Strict-Transport-Security: Enforces HTTPS connections (HSTS)
 * - Content-Security-Policy: Restricts sources for scripts, styles, etc.
 * - Referrer-Policy: Controls referrer information sent with requests
 * - Permissions-Policy: Restricts browser features
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // Prevent clickjacking - page cannot be embedded in iframes
        response.setHeader("X-Frame-Options", "DENY");
        
        // Prevent MIME-type sniffing
        response.setHeader("X-Content-Type-Options", "nosniff");
        
        // Legacy XSS protection for older browsers
        response.setHeader("X-XSS-Protection", "1; mode=block");
        
        // Control referrer information
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        
        // Restrict browser features
        response.setHeader("Permissions-Policy", "geolocation=(), microphone=(), camera=()");
        
        // HSTS - only enable for production over HTTPS
        if (isProductionProfile()) {
            // max-age = 1 year, include subdomains
            response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        }
        
        // Content Security Policy
        // Note: CSP is complex and needs to be tuned for the specific frontend
        // This is a reasonable starting point that allows:
        // - Self-hosted scripts and styles
        // - Inline styles (needed for many UI libraries)
        // - Data URIs for images (needed for base64 images)
        // - External fonts from Google Fonts
        // - External images from common CDNs
        String csp = buildContentSecurityPolicy();
        response.setHeader("Content-Security-Policy", csp);
        
        filterChain.doFilter(request, response);
    }

    private boolean isProductionProfile() {
        return "prod".equalsIgnoreCase(activeProfile) || 
               "production".equalsIgnoreCase(activeProfile) ||
               "staging".equalsIgnoreCase(activeProfile);
    }

    private String buildContentSecurityPolicy() {
        // CSP directives:
        // - default-src 'self': Only allow resources from same origin by default
        // - script-src 'self': Only allow scripts from same origin (no inline scripts)
        // - style-src 'self' 'unsafe-inline': Allow styles from same origin + inline styles
        // - img-src 'self' data: https:: Allow images from same origin, data URIs, and HTTPS
        // - font-src 'self' https://fonts.gstatic.com: Allow fonts from same origin and Google Fonts
        // - connect-src 'self': Only allow XHR/fetch to same origin
        // - frame-ancestors 'none': Don't allow page to be framed (redundant with X-Frame-Options)
        // - base-uri 'self': Restrict <base> element to same origin
        // - form-action 'self': Restrict form submissions to same origin
        
        if (isProductionProfile()) {
            // Strict CSP for production
            return "default-src 'self'; " +
                   "script-src 'self'; " +
                   "style-src 'self' 'unsafe-inline'; " +
                   "img-src 'self' data: https:; " +
                   "font-src 'self' https://fonts.gstatic.com; " +
                   "connect-src 'self'; " +
                   "frame-ancestors 'none'; " +
                   "base-uri 'self'; " +
                   "form-action 'self'";
        } else {
            // Relaxed CSP for development (allows more for debugging)
            // Report-only mode could be used here for testing
            return "default-src 'self'; " +
                   "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                   "style-src 'self' 'unsafe-inline'; " +
                   "img-src 'self' data: https: http:; " +
                   "font-src 'self' https://fonts.gstatic.com data:; " +
                   "connect-src 'self' http://localhost:* ws://localhost:*; " +
                   "frame-ancestors 'none'; " +
                   "base-uri 'self'; " +
                   "form-action 'self'";
        }
    }
}
