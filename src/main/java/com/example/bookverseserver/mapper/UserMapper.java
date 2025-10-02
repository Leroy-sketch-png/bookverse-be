package com.example.bookverseserver.mapper;

import com.example.bookverseserver.dto.request.User.UserCreationRequest;
import com.example.bookverseserver.dto.request.User.UserUpdateRequest;
import com.example.bookverseserver.dto.response.User.UserResponse;
import com.example.bookverseserver.entity.User.Role;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.enums.RoleName;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.repository.RoleRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {RoleMapper.class})
public abstract class UserMapper {

    @Autowired
    private RoleRepository roleRepository;

    public abstract User toUser(UserCreationRequest request);

    public Set<Role> toRoles(List<String> roles) {
        if (roles == null) {
            return Collections.emptySet();
        }
        return roles.stream()
                .map(roleName -> roleRepository.findByName(RoleName.valueOf(roleName))
                        .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND)))
                .collect(Collectors.toSet());
    }

    public abstract UserResponse toUserResponse(User user);

    @Mapping(target = "roles", expression = "java(toRoles(request.getRoles()))")
    public abstract void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
