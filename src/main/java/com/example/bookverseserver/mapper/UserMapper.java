package com.example.bookverseserver.mapper;

import com.example.bookverseserver.dto.request.User.UserCreationRequest;
import com.example.bookverseserver.dto.request.User.UserUpdateRequest;
import com.example.bookverseserver.dto.response.Authentication.RoleResponse;
import com.example.bookverseserver.dto.response.User.UserResponse;
import com.example.bookverseserver.entity.User.Role;
import com.example.bookverseserver.entity.User.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toUser(UserCreationRequest request);

    default UserResponse toUserResponse(User user) {
        if (user == null) return null;

        UserResponse resp = new UserResponse();
        resp.setId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setEmail(user.getEmail());
        resp.setEnabled(user.getEnabled());

        // Map Set<Role> to Set<RoleResponse>
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            Set<RoleResponse> roleResponses = user.getRoles().stream()
                    .map(role -> RoleResponse.builder()
                            .id(role.getId())
                            .name(role.getName())
                            .build())
                    .collect(Collectors.toSet());
            resp.setRoles(roleResponses);
        }

        return resp;
    }

    @Mapping(target = "roles", ignore = true) // Roles handled manually in service
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
