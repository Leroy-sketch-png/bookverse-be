package com.example.bookverseserver.mapper;

import com.example.bookverseserver.dto.request.User.UserCreationRequest;
import com.example.bookverseserver.dto.request.User.UserUpdateRequest;
import com.example.bookverseserver.dto.response.Authentication.RoleResponse;
import com.example.bookverseserver.dto.response.User.UserResponse;
import com.example.bookverseserver.entity.User.Role;
import com.example.bookverseserver.entity.User.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.Collections;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // keep MapStruct generated mappings you still want
    User toUser(UserCreationRequest request);

    // MANUAL mapping: avoids MapStruct compile issues
    default UserResponse toUserResponse(User user) {
        if (user == null) return null;

        UserResponse resp = new UserResponse();
        resp.setId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setEmail(user.getEmail());
        resp.setEnabled(user.getEnabled());

        Set<Role> roles = user.getRoles();
        if (roles == null) {
            resp.setRoles(Collections.emptySet());
        } else {
            Set<RoleResponse> roleResponses = roles.stream().map(r -> {
                RoleResponse rr = new RoleResponse();
                rr.setId(r.getId());
                // Adjust this if your Role has different fields (e.g., getRoleName(), getName(), or enum)
                rr.setName(r.getName());
                return rr;
            }).collect(Collectors.toSet());
            resp.setRoles(roleResponses);
        }

        return resp;
    }

    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
