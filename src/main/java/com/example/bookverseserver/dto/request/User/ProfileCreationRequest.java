package com.example.bookverseserver.dto.request.User;

import com.example.bookverseserver.enums.MembershipType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileCreationRequest {
    String fullName;
    String phone;
    LocalDate dob;
    MembershipType mbsType;
    String avatarUrl;
    String bio;
}
