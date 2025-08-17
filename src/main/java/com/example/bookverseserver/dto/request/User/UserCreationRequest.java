package com.example.bookverseserver.dto.request.User;
import lombok.*;
import jakarta.validation.constraints.Size;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {
    @Size(min = 4, message = "USERNAME_INVALID")
    String username;

    String email;

    @Size(min = 8, message = "Password must have at least 8 character")
    String password;
    Boolean enabled;
}
