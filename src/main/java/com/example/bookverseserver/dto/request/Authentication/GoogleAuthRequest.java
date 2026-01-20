package com.example.bookverseserver.dto.request.Authentication;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Google authentication request.
 * Added validation for code field.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Google authentication request")
public class GoogleAuthRequest {

    @NotBlank(message = "GOOGLE_AUTH_CODE_REQUIRED")
    @Schema(
            description = "Authorization code returned from Google OAuth",
            example = "4/0AbUR2VExampleCode"
    )
    private String code;
}
