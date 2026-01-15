package com.example.bookverseserver.service;

import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NimbusJwtService {

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

    public String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(String.valueOf(user.getId()))  // user.id làm subject
                .issuer("bookverse.com")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("username", user.getUsername())
                .claim("email", user.getEmail())
                .claim("scope", buildScope(user))
                .claim("roles", buildRolesArray(user)) // Add roles array for FE
                .build();

        JWSObject jwsObject = new JWSObject(header, new Payload(jwtClaimsSet.toJSONObject()));

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }

    public SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = (isRefresh)
                ? new Date(signedJWT
                .getJWTClaimsSet()
                .getIssueTime()
                .toInstant()
                .plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS)
                .toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        if (!(verified && expiryTime.after(new Date())))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        return signedJWT;
    }

    public boolean introspectToken(String token) throws JOSEException, ParseException {
        try {
            verifyToken(token, false);
            return true;
        } catch (AppException e) {
            return false;
        }
    }

    /**
     * Validate a token and return true if valid, false otherwise.
     * Does not throw exceptions - safe for use in WebSocket authentication.
     */
    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        try {
            verifyToken(token, false);
            return true;
        } catch (Exception e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extract user ID from JWT token.
     * Token subject contains the user ID as a string.
     */
    public Long extractUserId(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            String subject = signedJWT.getJWTClaimsSet().getSubject();
            return Long.valueOf(subject);
        } catch (Exception e) {
            log.error("Failed to extract user ID from token", e);
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    /**
     * Extract username from JWT token.
     * Username is stored as a custom claim.
     */
    public String extractUsername(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getStringClaim("username");
        } catch (Exception e) {
            log.error("Failed to extract username from token", e);
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }

    private String buildScope(User user) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) return "";
        return user.getRoles().stream()
                .map(role -> "ROLE_" + role.getName())
                .collect(Collectors.joining(" "));
    }

    private java.util.List<String> buildRolesArray(User user) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) return java.util.Collections.emptyList();
        return user.getRoles().stream()
                .map(role -> role.getName().toString())
                .collect(Collectors.toList());
    }
}
