package com.example.bookverseserver.dto.response.User;

import com.example.bookverseserver.enums.MembershipType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileResponse {
    Long id;
    Long userId;
    String fullName;
    String phone;
    LocalDate dob;
    MembershipType mbsType;
    String avatarUrl;
    String bio;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
