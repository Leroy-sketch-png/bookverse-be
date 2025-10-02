package com.example.bookverseserver.dto.request.User;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProfileCreationRequest {
    @NotBlank
    private String fullName;
    private String avatarUrl;
    private String displayName;
    private String phoneNumber;
    private String location;

    /**
     * accountType could be an enum; accept String for flexibility and map/validate in service if needed.
     */
    private String accountType;

    /**
     * preferences stored as JSON string. If you want typed preferences use a nested DTO or Map<String,Object>.
     */
    private String preferences;
    private String bio;
}
