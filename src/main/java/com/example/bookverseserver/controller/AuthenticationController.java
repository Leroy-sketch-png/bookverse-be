package com.example.bookverseserver.controller;

import java.text.ParseException;

import com.example.bookverseserver.dto.request.Authentication.*;
import com.example.bookverseserver.dto.request.User.UserCreationRequest;
import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.response.User.UserResponse;
import com.example.bookverseserver.service.GoogleAuthService;
import com.example.bookverseserver.service.UserService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.bookverseserver.dto.response.Authentication.AuthenticationResponse;
import com.example.bookverseserver.dto.response.Authentication.IntrospectResponse;
import com.example.bookverseserver.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;
    GoogleAuthService googleAuthService;
    UserService userService;

    @PostMapping("/register")
    ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.createUser(request))
                .build();
    }

    @PostMapping("/login")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        var result = authenticationService.authenticate(request);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> authenticate(@RequestBody IntrospectRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder().result(result).build();
    }

    @PostMapping("/refresh")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody RefreshRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.refreshToken(request);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody LogoutRequest request) throws ParseException, JOSEException {
        authenticationService.logout(request);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/google")
    public ApiResponse<AuthenticationResponse> googleAuth(@RequestBody GoogleAuthRequest googleIdTokenString) {
        try {
            // Delegate the entire Google authentication process to the service
            AuthenticationResponse response = googleAuthService.authenticateGoogleUser(googleIdTokenString);

            return ApiResponse.<AuthenticationResponse>builder()
                    .result(response)
                    .build();

        } catch (Exception e) { // Catch all exceptions from the service for error handling
            // Log the detailed error for debugging purposes
            System.err.println("Google authentication failed: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for debugging

            // Return a consistent error response with an appropriate HTTP status code
            return ApiResponse.<AuthenticationResponse>builder()
                    .code(HttpStatus.UNAUTHORIZED.value()) // Use UNAUTHORIZED for authentication failures
                    .message("Authentication failed: " + e.getMessage()) // Provide specific error message
                    .build();
        }
    }
}