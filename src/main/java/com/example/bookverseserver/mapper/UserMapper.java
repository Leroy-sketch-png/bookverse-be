package com.example.bookverseserver.mapper;

import com.example.bookverseserver.dto.request.User.UserCreationRequest;
import com.example.bookverseserver.dto.request.User.UserUpdateRequest;
import com.example.bookverseserver.dto.response.Authentication.RoleResponse;
import com.example.bookverseserver.dto.response.User.UserResponse;
import com.example.bookverseserver.entity.User.Role;
import com.example.bookverseserver.entity.User.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

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

        Role role = user.getRole();
        if (role != null) {
            RoleResponse rr = new RoleResponse();
            rr.setId(role.getId());
            rr.setName(role.getName());
            resp.setRole(rr);
        }

        return resp;
    }

    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
