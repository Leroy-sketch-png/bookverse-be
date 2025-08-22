package com.example.bookverseserver.dto.request.Authentication;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmailAuthRequest {
    String email;
    String passwordHarsh;
}