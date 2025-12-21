package com.example.bookverseserver.dto.request.Authentication;

import lombok.*;
import lombok.experimental.FieldDefaults;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

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

    @NotBlank
    @Schema(
            description = "New password",
            example = "newPassword123"
    )
    String password;

    @NotBlank
    @Schema(
            description = "Confirm new password",
            example = "newPassword123"
    )
    String confirmPassword;
}
