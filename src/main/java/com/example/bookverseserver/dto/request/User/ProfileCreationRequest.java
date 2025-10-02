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
    private String accountType;
    private String preferences;
    private String bio;
}
