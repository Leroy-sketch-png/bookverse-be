package com.example.bookverseserver.dto.response.User;

import com.example.bookverseserver.enums.ApplicationStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProSellerApplicationResponse {
    Long applicationId;
    ApplicationStatus status;
    LocalDateTime submittedAt;
    String reviewNotes;
}
