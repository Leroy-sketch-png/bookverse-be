package com.example.bookverseserver.dto.response.Authentication;

import com.example.bookverseserver.enums.PermissionName;
import com.example.bookverseserver.enums.RoleName;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleResponse {
    RoleName name;
    Set<PermissionResponse> permissions;
}