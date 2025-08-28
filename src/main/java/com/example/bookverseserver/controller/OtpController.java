// OtpController.java
package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.request.Authentication.EmailRequest;
import com.example.bookverseserver.dto.request.Authentication.EmailVerificationRequest;
import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.service.OtpService;
import com.example.bookverseserver.service.SignupRequestService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OtpController {
    private final OtpService otpService; // keep for other OTP flows (password reset, etc.)
    private final SignupRequestService signupRequestService;

    @PostMapping("/send-otp")
    public ApiResponse<String> sendOtp(@RequestBody EmailRequest request) {
        otpService.generateAndSendOtp(request.getEmail());
        return ApiResponse.<String>builder()
                .result("OTP sent to email")
                .build();
    }

    @PostMapping("/verify-otp")
    public ApiResponse<String> verifyOtp(@RequestBody EmailVerificationRequest request) {
        try {
            User created = signupRequestService.verifyOtpAndCreateUser(request.getEmail(), request.getOtp());
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
