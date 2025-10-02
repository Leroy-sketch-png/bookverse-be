package com.example.bookverseserver.dto.response.User;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ProfileResponse {
    private Long id;
    private Long userId;
    private String displayName;
    private String fullName;
    private String phoneNumber;
    private String avatarUrl;
    private String bio;
    private String location;
    private String accountType;
    private Double ratingAvg;
    private Integer ratingCount;
    private LocalDate sellerSince;
    private String preferences;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer version;
}
