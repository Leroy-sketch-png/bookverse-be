package com.example.bookverseserver.dto.request.Authentication;

import lombok.*;
import lombok.experimental.FieldDefaults;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Refresh access token request")
public class RefreshRequest {

    @NotBlank
    @Schema(
            description = "Refresh token",
            example = "eyJhbGciOiJIUzI1NiJ9..."
    )
    String token;
}
