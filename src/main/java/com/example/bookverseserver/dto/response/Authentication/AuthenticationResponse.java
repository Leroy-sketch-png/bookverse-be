package com.example.bookverseserver.dto.response.Authentication;

import com.example.bookverseserver.dto.response.User.UserResponse;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationResponse {
    String token;
    boolean authenticated;
    LocalDateTime lastLogin;
    UserResponse user;

}