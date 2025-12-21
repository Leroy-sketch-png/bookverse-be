package com.example.bookverseserver.dto.request.Authentication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Google authentication request")
public class GoogleAuthRequest {

    @Schema(
            description = "Authorization code returned from Google OAuth",
            example = "4/0AbUR2VExampleCode"
    )
    private String code;
}
