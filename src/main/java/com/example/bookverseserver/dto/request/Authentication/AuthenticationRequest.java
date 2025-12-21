package com.example.bookverseserver.dto.request.Authentication;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Login request using email or username")
public class AuthenticationRequest {

    @NotBlank
    @Schema(
            description = "Email or username of the user",
            example = "tinvo@gmail.com"
    )
    String emailOrUsername;

    @NotBlank
    @Schema(
            description = "User password",
            example = "123456"
    )
    String password;
}
