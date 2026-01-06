package com.example.bookverseserver.dto.response.Authentication;

import com.example.bookverseserver.dto.response.User.UserResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * Authentication response matching Vision API_CONTRACTS.md.
 * 
 * Format:
 * {
 *   "token": "...",
 *   "refreshToken": "...",
 *   "user": { ... }
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationResponse {
    String token;
    String refreshToken;        // Added per Vision
    boolean authenticated;
    LocalDateTime lastLogin;
    UserResponse user;
}