package com.example.bookverseserver.dto.request.Authentication;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChangePasswordRequest {

    @NotBlank
    String oldPassword;

    @NotBlank
    @Size(min = 6, message = "Password must be at least 6 characters")
    String newPassword;

}
