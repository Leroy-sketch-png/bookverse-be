package com.example.bookverseserver.configuration;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * P0 Security Fix #7: Validates critical security configuration at startup.
 * 
 * This prevents the application from starting with insecure default values.
 * All security-critical environment variables must be explicitly set.
 */
@Configuration
@Slf4j
public class SecurityConfigValidator {

    @Value("${jwt.signerKey:}")
    private String jwtSignerKey;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    // Known insecure default values that should trigger startup failure
    private static final String[] INSECURE_JWT_KEYS = {
        "",
        "dev-secret-key",
        "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef",
        "your-secret-key",
        "change-me",
        "secret"
    };

    private static final int MIN_JWT_KEY_LENGTH = 64;

    @PostConstruct
    public void validateSecurityConfiguration() {
        log.info("Validating security configuration for profile: {}", activeProfile);

        // In production or staging, enforce strict security requirements
        if (isProductionProfile()) {
            validateProductionJwtKey();
        } else {
            // Development: warn but don't fail
            validateDevelopmentJwtKey();
        }

        log.info("Security configuration validation completed successfully");
    }

    private boolean isProductionProfile() {
        return "prod".equalsIgnoreCase(activeProfile) || 
               "production".equalsIgnoreCase(activeProfile) ||
               "staging".equalsIgnoreCase(activeProfile);
    }

    private void validateProductionJwtKey() {
        // Check if JWT key is set
        if (jwtSignerKey == null || jwtSignerKey.isBlank()) {
            throw new SecurityConfigurationException(
                "CRITICAL: JWT_SIGNER_KEY environment variable is not set. " +
                "Application cannot start in production without a secure JWT signing key. " +
                "Generate a secure key with: openssl rand -hex 64"
            );
        }

        // Check if using known insecure defaults
        for (String insecureKey : INSECURE_JWT_KEYS) {
            if (insecureKey.equals(jwtSignerKey)) {
                throw new SecurityConfigurationException(
                    "CRITICAL: JWT_SIGNER_KEY is using an insecure default value. " +
                    "Application cannot start in production with a hardcoded key. " +
                    "Generate a secure key with: openssl rand -hex 64"
                );
            }
        }

        // Check minimum key length (256 bits = 64 hex characters for HMAC-SHA256)
        if (jwtSignerKey.length() < MIN_JWT_KEY_LENGTH) {
            throw new SecurityConfigurationException(
                "CRITICAL: JWT_SIGNER_KEY is too short (" + jwtSignerKey.length() + " chars). " +
                "Minimum required length is " + MIN_JWT_KEY_LENGTH + " characters. " +
                "Generate a secure key with: openssl rand -hex 64"
            );
        }

        log.info("JWT signing key validated for production use");
    }

    private void validateDevelopmentJwtKey() {
        if (jwtSignerKey == null || jwtSignerKey.isBlank()) {
            log.warn("⚠️  WARNING: JWT_SIGNER_KEY is not set. Using insecure default for development only.");
            log.warn("⚠️  DO NOT deploy to production without setting JWT_SIGNER_KEY environment variable!");
            return;
        }

        for (String insecureKey : INSECURE_JWT_KEYS) {
            if (insecureKey.equals(jwtSignerKey)) {
                log.warn("⚠️  WARNING: JWT_SIGNER_KEY is using an insecure default value.");
                log.warn("⚠️  This is acceptable for local development only.");
                log.warn("⚠️  Generate a production key with: openssl rand -hex 64");
                return;
            }
        }

        log.info("JWT signing key configured for development");
    }

    /**
     * Exception thrown when security configuration is invalid.
     * Extends RuntimeException to prevent application startup.
     */
    public static class SecurityConfigurationException extends RuntimeException {
        public SecurityConfigurationException(String message) {
            super(message);
        }
    }
}
