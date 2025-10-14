package com.example.bookverseserver.dto.response.User;
import com.example.bookverseserver.dto.response.Authentication.RoleResponse;
import com.example.bookverseserver.entity.User.Role;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    Long id;
    String username;
    String email;
    Boolean enabled;
    RoleResponse role;

}
