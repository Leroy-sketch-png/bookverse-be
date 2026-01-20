// OtpController.java
package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.request.Authentication.EmailRequest;
import com.example.bookverseserver.dto.request.Authentication.EmailVerificationRequest;
import com.example.bookverseserver.dto.request.Authentication.OtpSendRequest;
import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.service.OtpService;
import com.example.bookverseserver.service.SignupRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "OTP Verification", description = "📧 OTP (One-Time Password) management APIs - Send and verify email OTP codes")
public class OtpController {
    private final OtpService otpService; // keep for other OTP flows (password reset, etc.)
    private final SignupRequestService signupRequestService;

    @PostMapping("/send-otp")
    @Operation(
        summary = "Send OTP to email",
        description = "Generate and send a 6-digit OTP code to user's email. " +
                     "**Use cases**: Email verification, password reset. " +
                     "**OTP validity**: 5 minutes. " +
                     "**Rate limit**: Max 3 requests per 10 minutes per email."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "OTP sent successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Invalid email or rate limit exceeded"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "429", 
            description = "Too many requests"
        )
    })
    public ApiResponse<String> sendOtp(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Email address to receive OTP",
            required = true,
            content = @Content(schema = @Schema(implementation = OtpSendRequest.class))
        )
        @RequestBody @Valid OtpSendRequest request
    ) { // <-- Sửa ở đây
        // Giờ đây request.getEmail() sẽ trả về một String duy nhất
        otpService.generateAndSendOtp(request.getEmail()); // <-- Sửa ở đây
        return ApiResponse.<String>builder()
                .result("OTP sent to " + request.getEmail())
                .build();
    }

    @PostMapping("/verify-otp")
    @Operation(
        summary = "Verify OTP and create account",
        description = "Validate OTP code and complete user registration. " +
                     "**On success**: Creates user account and returns authentication tokens. " +
                     "**OTP expires**: After 5 minutes or after 3 failed attempts."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "OTP verified, account created successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Invalid or expired OTP"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "No pending registration found for this email"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "429", 
            description = "Too many failed verification attempts"
        )
    })
    public ApiResponse<String> verifyOtp(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Email and OTP code",
            required = true,
            content = @Content(schema = @Schema(implementation = EmailVerificationRequest.class))
        )
        @Valid @RequestBody EmailVerificationRequest request
    ) {
        try {
            signupRequestService.verifyOtpAndCreateUser(request.getEmail(), request.getOtp());
            return ApiResponse.<String>builder()
                    .result("Email verified and account created")
                    .build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ApiResponse.<String>builder()
                    .code(400)
                    .message(e.getMessage())
                    .build();
        } catch (Exception e) {
            return ApiResponse.<String>builder()
                    .code(500)
                    .message("Verification failed: " + e.getMessage())
                    .build();
        }
    }
}
