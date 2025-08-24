package com.example.bookverseserver.controller;

import com.example.bookverseserver.dto.request.Authentication.EmailRequest;
import com.example.bookverseserver.dto.request.Authentication.EmailVerificationRequest;
import com.example.bookverseserver.dto.response.ApiResponse;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.repository.UserRepository;
import com.example.bookverseserver.service.OtpService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OtpController {
    private final OtpService otpService;
    private final UserRepository userRepository;

    @PostMapping("/send-otp")
    public ApiResponse<String> sendOtp(@RequestBody EmailRequest request) {
        otpService.generateAndSendOtp(request.getEmail());
        return ApiResponse.<String>builder()
                .result("OTP sent to email")
                .build();
    }

    @PostMapping("/verify-otp")
    public ApiResponse<String> verifyOtp(@RequestBody EmailVerificationRequest request) {
        boolean valid = otpService.verifyOtp(request.getEmail(), request.getOtp());
        if (valid) {
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            user.setEnabled(true);
            userRepository.save(user);
            return ApiResponse.<String>builder()
                    .result("Email verified successfully!")
                    .build();
        }
        return ApiResponse.<String>builder()
                .result("Invalid or expired OTP")
                .build();
    }
}
