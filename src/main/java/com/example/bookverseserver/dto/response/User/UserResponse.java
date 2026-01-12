package com.example.bookverseserver.dto.response.User;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

/**
 * User response DTO matching Vision API_CONTRACTS.md.
 * 
 * Includes nested profile object as required by Vision:
 * {
 *   "id": 123,
 *   "email": "...",
 *   "username": "...",
 *   "profile": {
 *     "displayName": "...",
 *     "avatar": "...",
 *     ...
 *   },
 *   "roles": ["USER", "SELLER"],
 *   "accountType": "CASUAL_SELLER",
 *   "isVerified": true
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    Long id;
    String username;
    String email;
    UserProfileSummary profile;     // Nested profile per Vision
    Set<String> roles;              // Array of role names (strings, not objects)
    String accountType;             // BUYER, CASUAL_SELLER, PRO_SELLER
    Boolean isVerified;             // Email verification status
    Boolean enabled;                // Account enabled/suspended status (admin can suspend)
    String createdAt;               // ISO timestamp of account creation
}
