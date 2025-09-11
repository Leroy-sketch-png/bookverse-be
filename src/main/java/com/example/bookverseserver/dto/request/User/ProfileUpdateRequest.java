package com.example.bookverseserver.dto.request.User;

import lombok.Data;

@Data
public class ProfileUpdateRequest {
    // all fields optional for partial update
    private String fullName;
    private String location;
    private String accountType;
    private String preferences;
}
