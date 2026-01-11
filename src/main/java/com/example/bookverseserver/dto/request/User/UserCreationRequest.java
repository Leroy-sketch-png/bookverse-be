package com.example.bookverseserver.dto.request.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    String email;

    /**
     * P0 Security Fix #9: Username validation with strict regex.
     * Only alphanumeric characters and underscores allowed.
     * Prevents XSS payloads, SQL injection patterns, and path traversal in usernames.
     */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(
        regexp = "^[a-zA-Z0-9_]+$",
        message = "Username can only contain letters, numbers, and underscores"
    )
    String username;

    /**
     * P0 Security Fix #4: Strong password requirements.
     * - Minimum 8 characters
     * - At least 1 uppercase letter
     * - At least 1 lowercase letter  
     * - At least 1 number
     * - At least 1 special character (!@#$%^&*()_+-=[]{}|;:,.<>?)
     */
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?]).{8,}$",
        message = "Password must contain at least 1 uppercase letter, 1 lowercase letter, 1 number, and 1 special character"
    )
    String password;

//    @Size(max = 100, message = "Display name cannot exceed 100 characters")
//    String displayName;
}
