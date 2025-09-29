package com.example.bookverseserver.controller;

import java.text.ParseException;
import com.example.bookverseserver.dto.request.Authentication.*;
import com.example.bookverseserver.dto.request.User.UserCreationRequest;
import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.service.GoogleAuthService;
import com.example.bookverseserver.service.SignupRequestService;
import com.nimbusds.jose.JOSEException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.bookverseserver.dto.response.Authentication.AuthenticationResponse;
import com.example.bookverseserver.dto.response.Authentication.IntrospectResponse;
import com.example.bookverseserver.service.AuthenticationService;
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
    SignupRequestService signupRequestService;
    PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    ApiResponse<String> createUser(@RequestBody @Valid UserCreationRequest request) {
        String passwordHash = passwordEncoder.encode(request.getPassword());
        signupRequestService.createSignupRequest(
                request.getEmail(),
                request.getUsername(),
                passwordHash
        );
        return ApiResponse.<String>builder()
                .result("OTP sent to email")
                .build();
    }

    @PostMapping("/login")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        var result = authenticationService.authenticate(request);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder().result(result).build();
    }

    @PostMapping("/refresh")
    ApiResponse<AuthenticationResponse> refresh(@RequestBody RefreshRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.refreshToken(request);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody LogoutRequest request)
            throws ParseException, JOSEException {
        authenticationService.logout(request);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/google")
    ApiResponse<AuthenticationResponse> googleAuth(@RequestBody GoogleAuthRequest request) {
        try {
            AuthenticationResponse response = googleAuthService.authenticateGoogleUser(request);
            return ApiResponse.<AuthenticationResponse>builder()
                    .result(response)
                    .build();
        } catch (Exception e) {
            logError(e);
            return ApiResponse.<AuthenticationResponse>builder()
                    .code(HttpStatus.UNAUTHORIZED.value())
                    .message("Authentication failed: " + e.getMessage())
                    .build();
        }
    }

    private void logError(Exception e) {
        System.err.println("Google authentication failed: " + e.getMessage());
        e.printStackTrace();
    }
}
