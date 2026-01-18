package com.example.bookverseserver.dto.response.User;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    private String preferences;
    private Boolean isProSeller; // Added to differentiate seller tiers
    private String responseTime; // Average response time metric
    private Double fulfillmentRate; // Percentage of successful deliveries

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate sellerSince;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private Integer version;
}