package com.example.bookverseserver.dto.request.Authentication;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Token introspection request.
 * Added validation for token field.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Token introspection request")
public class IntrospectRequest {
    
    @NotBlank(message = "TOKEN_REQUIRED")
    @Schema(
            description = "JWT token to introspect",
            example = "eyJhbGciOiJIUzI1NiJ9..."
    )
    String token;
}