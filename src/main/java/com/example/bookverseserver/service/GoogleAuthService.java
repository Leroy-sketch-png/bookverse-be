package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.request.Authentication.GoogleAuthRequest;
import com.example.bookverseserver.dto.response.Authentication.AuthenticationResponse;
import com.example.bookverseserver.dto.response.User.UserResponse;
import com.example.bookverseserver.entity.User.AuthProvider;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.mapper.UserMapper;
import com.example.bookverseserver.repository.AuthProviderRepository;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.json.jackson2.JacksonFactory;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GoogleAuthService {

    @Autowired
    final UserService userService;

    @Autowired
    final AuthenticationService authenticationService;

    @Autowired
    final AuthProviderRepository authProviderRepository;

    @Autowired
    final UserMapper userMapper;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientID;

    private GoogleIdTokenVerifier verifier;

    @PostConstruct
    public void init() throws GeneralSecurityException, IOException {
        this.verifier = new GoogleIdTokenVerifier.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance()
        ).setAudience(Collections.singletonList(clientID))
                .build();
    }

    /**
     * Authenticate or register a user using Google ID token.
     * @param request GoogleAuthRequest containing idToken (and optionally accessToken/refreshToken)
     * @return AuthenticationResponse containing your app JWT and user info
     */
    public AuthenticationResponse authenticateGoogleUser(GoogleAuthRequest request)
            throws GeneralSecurityException, IOException {

        if (request == null || request.getIdToken() == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        GoogleIdToken.Payload payload = verifyToken(request.getIdToken());

        String provider = "GOOGLE";
        String providerUserId = payload.getSubject(); // google sub
        String providerEmail = payload.getEmail();
        String displayName = (String) payload.get("name");
        String pictureUrl = (String) payload.get("picture"); // optional

        Optional<AuthProvider> existingProviderOpt = authProviderRepository
                .findByProviderAndProviderUserId(provider, providerUserId);

        User user;

        if (existingProviderOpt.isPresent()) {
            AuthProvider existingProvider = existingProviderOpt.get();
            user = existingProvider.getUser();

            boolean providerChanged = false;
            if (request.getAccessToken() != null) {
                existingProvider.setAccessToken(request.getAccessToken());
                providerChanged = true;
            }
            if (request.getRefreshToken() != null) {
                existingProvider.setRefreshToken(request.getRefreshToken());
                providerChanged = true;
            }
            if (providerChanged) {
                authProviderRepository.save(existingProvider);
            }

        } else {
            user = userService.findByGoogleId(providerUserId)
                    .orElseGet(() -> userService.findByEmail(providerEmail).orElse(null));

            if (user == null) {
                User newUser = new User();
                newUser.setGoogleId(providerUserId);
                newUser.setEmail(providerEmail);
                newUser.setUsername(providerEmail); // safe default
                newUser.setAuthProvider("GOOGLE");
                newUser.setEnabled(true);

                user = userService.saveUser(newUser);
            }

            AuthProvider authProvider = AuthProvider.builder()
                    .user(user)
                    .provider(provider)
                    .providerUserId(providerUserId)
                    .providerEmail(providerEmail)
                    .accessToken(request.getAccessToken())
                    .refreshToken(request.getRefreshToken())
                    .expiresAt(null)
                    .build();

            authProviderRepository.save(authProvider);
        }

        user.setEmail(providerEmail != null ? providerEmail : user.getEmail());
        user.setLastLogin(LocalDateTime.now());
        user = userService.updateUser(user);

        String jwt = authenticationService.generateToken(user);

        UserResponse userResponse = userMapper.toUserResponse(user);

        return AuthenticationResponse.builder()
                .token(jwt)
                .authenticated(true)
                .lastLogin(user.getLastLogin())
                .user(userResponse)
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
}
