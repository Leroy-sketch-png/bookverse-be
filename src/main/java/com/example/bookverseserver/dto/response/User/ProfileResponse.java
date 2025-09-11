package com.example.bookverseserver.dto.response.User;

import lombok.Data;

@Data
public class ProfileResponse {
    private Long id;
    private Long userId;
    private String fullName;
    private String location;
    private String accountType;
    private String preferences;
}
