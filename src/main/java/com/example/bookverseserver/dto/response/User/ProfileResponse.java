package com.example.bookverseserver.dto.response.User;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
    private Integer version;
    private String accountType;
    private String location;

    private Double ratingAvg;
    private Integer ratingCount;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate sellerSince;

    private String preferences;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
