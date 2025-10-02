package com.example.bookverseserver.dto.request.User;

import lombok.Data;

@Data
public class ProfileUpdateRequest {
    private String fullName;
    private String displayName;
    private String phoneNumber;
    private String location;
    private String bio;
    private String accountType;
    private String preferences;
}
