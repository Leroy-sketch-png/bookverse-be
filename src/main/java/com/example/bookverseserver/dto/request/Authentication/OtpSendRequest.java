package com.example.bookverseserver.dto.request.Authentication;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Send OTP to email request")
public class OtpSendRequest {

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email cannot be blank")
    @Schema(
            description = "User email to receive OTP",
            example = "tinvo@gmail.com"
    )
    private String email;
}
