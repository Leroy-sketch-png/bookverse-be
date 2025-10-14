package com.example.bookverseserver.dto.request.User;

import lombok.Data;

/**
 * DTO dùng cho cập nhật hồ sơ người dùng.
 * Tất cả field đều optional -> chỉ field nào có giá trị khác null mới update.
 */
@Data
public class ProfileUpdateRequest {
    private String fullName;
    private String displayName;
    private String avatarUrl;
    private String phoneNumber;
    private String location;
    private String bio;
    private String preferences;
}
