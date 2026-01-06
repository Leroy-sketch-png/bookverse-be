package com.example.bookverseserver.dto.response.User;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Slim profile summary for embedding in UserResponse.
 * Matches Vision API_CONTRACTS.md user.profile structure.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserProfileSummary {
    String displayName;
    String phone;
    String avatar;
    String bio;
    String location;
    String joinedAt;  // ISO timestamp string
}
