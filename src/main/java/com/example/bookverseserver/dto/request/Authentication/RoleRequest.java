package com.example.bookverseserver.dto.request.Authentication;

import com.example.bookverseserver.entity.User.Permission;
import com.example.bookverseserver.enums.RoleName;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleRequest {
    RoleName name;
    String description;
    Set<String> permissions;
}