package com.example.bookverseserver.dto.request.Authentication;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PasswordResetRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email;
    
    @NotBlank(message = "OTP is required")
    String otp;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    String password;
    
    @NotBlank(message = "Confirm password is required")
    String confirmPassword;
}
