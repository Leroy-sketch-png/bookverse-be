package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.request.Authentication.GoogleAuthRequest;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.dto.response.Authentication.AuthenticationResponse;
import com.example.bookverseserver.dto.response.User.UserResponse;
import com.example.bookverseserver.mapper.UserMapper; // Import UserMapper
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Service
public class GoogleAuthService {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientID;

    private GoogleIdTokenVerifier verifier;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserMapper userMapper; // Inject UserMapper here to convert User entity to UserResponse

    @PostConstruct
    public void init() throws GeneralSecurityException, IOException {
        this.verifier = new GoogleIdTokenVerifier.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance()
        ).setAudience(Collections.singletonList(clientID))
                .build();
    }

    /**
     * Verifies a Google ID token and returns its payload.
     * This method focuses purely on token verification.
     *
     * @param idTokenString The Google ID token string.
     * @return GoogleIdToken.Payload containing user information if the token is valid.
     * @throws GeneralSecurityException if there's a security issue during verification.
     * @throws IOException if there's an I/O error during certificate fetching.
     * @throws RuntimeException if the token is invalid after initial checks.
     */
    public GoogleIdToken.Payload verifyToken(String idTokenString) throws GeneralSecurityException, IOException {
        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken != null) {
            return idToken.getPayload();
        }
        throw new RuntimeException("Invalid Google ID Token");
    }

    /**
     * Handles the complete Google authentication flow: verifies the ID token,
     * finds or creates a user, and generates an application-specific JWT using AuthenticationService.
     *
     * @param googleIdTokenString The Google ID token string received from the client.
     * @return AuthenticationResponse containing the generated JWT and the authenticated user's details.
     * @throws GeneralSecurityException if there's a security issue during token verification.
     * @throws IOException if there's an I/O error during token verification.
     * @throws RuntimeException if token is invalid or any other authentication failure.
     */
    public AuthenticationResponse authenticateGoogleUser(GoogleAuthRequest googleIdTokenString)
            throws GeneralSecurityException, IOException {

        // 1. Verify the Google ID token
        GoogleIdToken.Payload payload = verifyToken(googleIdTokenString.getIdToken());

        // Extract user information from the verified Google ID token payload
        String googleId = payload.getSubject(); // Unique Google user ID (immutable)
        String email = payload.getEmail();
        String name = (String) payload.get("name"); // 'name' from Google payload
        String pictureUrl = (String) payload.get("picture"); // Optional: profile picture URL

        // 2. Find or create the user in your application's database
        // Use Optional.orElseGet() for cleaner handling
        User userEntity = userService.findByGoogleId(googleId)
                .orElseGet(() -> {
                    // If user does not exist, create a new one
                    User newUser = new User();
                    newUser.setGoogleId(googleId);
                    newUser.setEmail(email);
                    newUser.setUsername(email); // Use email as username for Google users for consistency
                    newUser.setDisplayName(name); // Assuming User has a 'displayName' field
                    newUser.setAuthProvider("GOOGLE");
                    // You might need to set default roles here if your UserService.saveUser doesn't
                    // Example:
                    // Role defaultRole = roleRepository.findByName(RoleName.CASUAL_ROLE).orElseThrow(() -> new RuntimeException("Default role not found"));
                    // newUser.setRoles(Collections.singleton(defaultRole));

                    return userService.saveUser(newUser); // Save and return the new user
                });

        // 3. If user already existed (or was newly created), update necessary information.
        userEntity.setDisplayName(name); // Keep display name updated from Google payload
        userEntity.setEmail(email);     // Keep email updated
        userEntity = userService.updateUser(userEntity); // Save updated user details

        // 4. Generate your application's own JWT for the authenticated/registered user
        String jwt = authenticationService.generateToken(userEntity);

        // 5. Map the internal User entity to the external UserResponse DTO
        UserResponse userResponse = userMapper.toUserResponse(userEntity);

        // 6. Return the complete AuthenticationResponse
        return AuthenticationResponse.builder()
                .token(jwt)
                .authenticated(true)
                .lastLogin(LocalDateTime.now())
                .build();
    }
}