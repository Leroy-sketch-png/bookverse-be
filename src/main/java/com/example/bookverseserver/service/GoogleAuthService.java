package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.request.Authentication.GoogleAuthRequest;
import com.example.bookverseserver.dto.response.Authentication.AuthenticationResponse;
import com.example.bookverseserver.entity.User.AuthProvider;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.mapper.UserMapper;
import com.example.bookverseserver.repository.AuthProviderRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.json.jackson2.JacksonFactory;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
@Transactional(readOnly = true)  // Default read-only, override for write methods
public class GoogleAuthService {
    final UserService userService;
    final AuthProviderRepository authProviderRepository;
    final UserMapper userMapper;
    final NoOpTokenEncryptionService tokenEncryptionService;
    final NimbusJwtService jwtService;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    String clientID;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    String clientSecret;

    GoogleIdTokenVerifier verifier;

    @PostConstruct
    public void init() throws GeneralSecurityException, IOException {
        this.verifier = new GoogleIdTokenVerifier.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance()
        ).setAudience(Collections.singletonList(clientID))
                .build();
    }

    /**
     * Orchestrate code exchange, token verification, user linking and token persistence.
     */
    public AuthenticationResponse authenticateGoogleUser(GoogleAuthRequest request)
            throws GeneralSecurityException, IOException {

        if (request == null || request.getCode() == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        // Exchange authorization code for tokens
        GoogleTokenResponse tokenResponse = exchangeCodeForTokens(request.getCode());
        String idTokenString = tokenResponse.getIdToken();
        String accessToken = tokenResponse.getAccessToken();
        String refreshToken = tokenResponse.getRefreshToken();

        // Verify ID token
        GoogleIdToken.Payload payload = verifyToken(idTokenString);
        String provider = "GOOGLE";
        String providerUserId = payload.getSubject();
        String providerEmail = payload.getEmail();

        // Create or update user identity (UserService handles identity only)
        User user = userService.createOrUpdateGoogleUser(providerUserId, providerEmail);

        // Persist or update AuthProvider (store tokens here; encrypt refresh token)
        Optional<AuthProvider> opt = authProviderRepository.findByProviderAndProviderUserId(provider, providerUserId);
        AuthProvider authProvider = opt.orElseGet(() -> AuthProvider.builder()
                .user(user)
                .provider(provider)
                .providerUserId(providerUserId)
                .providerEmail(providerEmail)
                .build());

        if (accessToken != null) {
            authProvider.setAccessToken(accessToken);
        }
        if (refreshToken != null) {
            String cipher = tokenEncryptionService.encrypt(refreshToken);
            authProvider.setRefreshToken(cipher);
        }
        authProvider.setExpiresAt(null); // optionally set using tokenResponse.getExpiresInSeconds()
        authProviderRepository.save(authProvider);

        // Issue application JWT (short-lived) and build response
        String jwt = jwtService.generateToken(user);

        return AuthenticationResponse.builder()
                .token(jwt)
                .authenticated(true)
                .lastLogin(user.getLastLogin())
                .user(userMapper.toUserResponse(user))
                .build();
    }

    private GoogleIdToken.Payload verifyToken(String idTokenString) throws GeneralSecurityException, IOException {
        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken == null) {
            log.warn("Google ID token verification failed (null) for token starting: {}", safePreview(idTokenString));
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return idToken.getPayload();
    }

    private String safePreview(String token) {
        if (token == null) return "null";
        int keep = Math.min(10, token.length());
        return token.substring(0, keep) + "...";
    }

    private GoogleTokenResponse exchangeCodeForTokens(String code) {
        try {
            return new GoogleAuthorizationCodeTokenRequest(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance(),
                    "https://oauth2.googleapis.com/token",
                    clientID,
                    clientSecret,
                    code,
                    "postmessage"
            ).execute();
        } catch (Exception e) {
            log.error("Failed to exchange code for tokens with Google", e);
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }
}