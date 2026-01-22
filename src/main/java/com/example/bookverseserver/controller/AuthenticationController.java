package com.example.bookverseserver.controller;

import java.text.ParseException;

import com.example.bookverseserver.dto.request.Authentication.*;
import com.example.bookverseserver.dto.request.User.UserCreationRequest;
import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.dto.response.Authentication.*;
import com.example.bookverseserver.dto.response.User.UserResponse;
import com.example.bookverseserver.service.AuthenticationService;
import com.example.bookverseserver.service.GoogleAuthService;
import com.example.bookverseserver.service.SignupRequestService;
import com.nimbusds.jose.JOSEException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(
        name = "Authentication",
        description = "APIs for authentication, authorization and account security"
)
public class AuthenticationController {

    AuthenticationService authenticationService;
    GoogleAuthService googleAuthService;
    SignupRequestService signupRequestService;
    PasswordEncoder passwordEncoder;

    // ================= REGISTER =================

    @PostMapping("/register")
    @Operation(
            summary = "Register new user",
            description = "Create a new account and send OTP to user's email"
    )
    public ApiResponse<String> createUser(
            @Valid @RequestBody UserCreationRequest request
    ) {
        String passwordHash = passwordEncoder.encode(request.getPassword());
        signupRequestService.createSignupRequest(
                request.getEmail(),
                request.getUsername(),
                passwordHash
        );
        return ApiResponse.<String>builder()
                .code(HttpStatus.OK.value())
                .result("OTP sent to email")
                .build();
    }

    // ================= LOGIN =================

    @PostMapping("/login")
    @Operation(
            summary = "Login",
            description = "Authenticate user using email or username and password"
    )
    public ApiResponse<AuthenticationResponse> authenticate(
            @Valid @RequestBody AuthenticationRequest request
    ) {
        log.info(">>> Login attempt: {}", request.getEmailOrUsername());
        AuthenticationResponse result = authenticationService.authenticate(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }

    // ================= GOOGLE LOGIN =================

    @PostMapping("/google")
    @Operation(
            summary = "Login with Google",
            description = "Authenticate user using Google OAuth authorization code"
    )
    public ApiResponse<AuthenticationResponse> googleAuth(
            @Valid @RequestBody GoogleAuthRequest request
    ) {
        try {
            AuthenticationResponse response =
                    googleAuthService.authenticateGoogleUser(request);
            return ApiResponse.<AuthenticationResponse>builder()
                    .result(response)
                    .build();
        } catch (Exception e) {
            log.error("Google authentication failed", e);
            return ApiResponse.<AuthenticationResponse>builder()
                    .code(HttpStatus.UNAUTHORIZED.value())
                    .message("Authentication failed: " + e.getMessage())
                    .build();
        }
    }

    // ================= INTROSPECT =================

    @PostMapping("/introspect")
    @Operation(
            summary = "Introspect token",
            description = "Check whether a token is valid or expired"
    )
    public ApiResponse<IntrospectResponse> introspect(
            @Valid @RequestBody IntrospectRequest request
    ) throws ParseException, JOSEException {
        IntrospectResponse result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder()
                .result(result)
                .build();
    }

    // ================= REFRESH TOKEN =================

    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh access token",
            description = "Generate a new access token using refresh token"
    )
    public ApiResponse<RefreshResponse> refresh(
            @Valid @RequestBody RefreshRequest request
    ) throws ParseException, JOSEException {
        RefreshResponse result = authenticationService.refreshToken(request);
        return ApiResponse.<RefreshResponse>builder()
                .result(result)
                .build();
    }

    // ================= LOGOUT =================

    @PostMapping("/logout")
    @Operation(
            summary = "Logout",
            description = "Invalidate current access or refresh token"
    )
    @SecurityRequirement(name = "bearer-key")
    public ApiResponse<Void> logout(
            @Valid @RequestBody LogoutRequest request
    ) throws ParseException, JOSEException {
        authenticationService.logout(request);
        return ApiResponse.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Logout successful")
                .build();
    }

    // ================= FORGOT PASSWORD =================

    @PostMapping("/forgot-password")
    @Operation(
            summary = "Request password reset OTP",
            description = "Send OTP to email for password reset. Email must exist in system."
    )
    public ApiResponse<String> requestForgotPasswordOtp(
            @Valid @RequestBody OtpSendRequest request
    ) {
        authenticationService.forgotPasswordOtp(request.getEmail());
        return ApiResponse.<String>builder()
                .code(HttpStatus.OK.value())
                .result("OTP sent to email")
                .build();
    }

    @PostMapping("/change-forgot-password")
    @Operation(
            summary = "Change forgotten password",
            description = "Verify OTP and update new password"
    )
    public ApiResponse<UserResponse> verifyOtpAndChangePassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        UserResponse response =
                authenticationService.verifyOtpAndChangePassword(request);
        return ApiResponse.<UserResponse>builder()
                .result(response)
                .build();
    }
}
