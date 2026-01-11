package com.example.bookverseserver.configuration;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import javax.crypto.spec.SecretKeySpec;

import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Custom JWT decoder that adds password change validation.
 * 
 * P1 Security Fix #H1: Tokens issued before passwordChangedAt are rejected.
 * This ensures that when a user changes their password, all existing sessions are invalidated.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CustomJwtDecoder implements JwtDecoder {

    @Value("${jwt.signerKey}")
    private String signerKey;

    private final UserRepository userRepository;

    private NimbusJwtDecoder nimbusJwtDecoder;

    @Override
    public Jwt decode(String token) throws JwtException {

        if (Objects.isNull(nimbusJwtDecoder)) {
            SecretKeySpec secretKeySpec = new SecretKeySpec(signerKey.getBytes(), "HmacSHA512");
            nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(secretKeySpec)
                    .macAlgorithm(MacAlgorithm.HS512)
                    .build();
        }
        
        Jwt jwt = nimbusJwtDecoder.decode(token);
        
        // P1 Security Fix #H1: Check if token was issued before password change
        validatePasswordChangeTimestamp(jwt);
        
        return jwt;
    }
    
    /**
     * Validates that the token was issued after the user's last password change.
     * If the user changed their password after the token was issued, reject the token.
     */
    private void validatePasswordChangeTimestamp(Jwt jwt) {
        try {
            String userId = jwt.getSubject();
            if (userId == null) {
                return; // Let other validation handle this
            }
            
            User user = userRepository.findById(Long.valueOf(userId)).orElse(null);
            if (user == null || user.getPasswordChangedAt() == null) {
                return; // User not found or never changed password - allow token
            }
            
            // Get token issue time
            java.time.Instant issuedAt = jwt.getIssuedAt();
            if (issuedAt == null) {
                log.warn("JWT missing issued-at claim for user {}", userId);
                return; // Let other validation handle this
            }
            
            // Convert to LocalDateTime for comparison
            LocalDateTime tokenIssuedAt = LocalDateTime.ofInstant(issuedAt, ZoneId.systemDefault());
            
            // If password was changed after token was issued, reject the token
            if (user.getPasswordChangedAt().isAfter(tokenIssuedAt)) {
                log.info("Rejecting token for user {} - issued before password change", userId);
                throw new JwtException("Token invalidated due to password change");
            }
        } catch (NumberFormatException e) {
            log.warn("Invalid user ID in JWT subject: {}", jwt.getSubject());
            // Let other validation handle this
        }
    }
}
