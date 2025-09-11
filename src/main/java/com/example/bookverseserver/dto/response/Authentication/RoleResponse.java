package com.example.bookverseserver.dto.response.Authentication;

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
    Long id;
    RoleName name;
}