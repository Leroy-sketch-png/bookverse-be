package com.example.bookverseserver.dto.request.Authentication;

import lombok.*;
import lombok.experimental.FieldDefaults;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Verify OTP and change forgotten password")
public class ForgotPasswordRequest {

    @NotBlank
    @Schema(
            description = "User email",
            example = "tinvo@gmail.com"
    )
    String email;

    @NotBlank
    @Schema(
            description = "OTP code sent to email",
            example = "123456"
    )
    String otp;

    /**
     * P0 Security Fix #4: Strong password requirements on password reset.
     */
    @NotBlank
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?]).{8,}$",
        message = "Password must contain at least 1 uppercase letter, 1 lowercase letter, 1 number, and 1 special character"
    )
    @Schema(
            description = "New password (min 8 chars, requires uppercase, lowercase, number, special char)",
            example = "SecurePass123!"
    )
    String password;

    @NotBlank
    @Schema(
            description = "Confirm new password",
            example = "SecurePass123!"
    )
    String confirmPassword;
}
