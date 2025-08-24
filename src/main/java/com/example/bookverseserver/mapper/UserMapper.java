package com.example.bookverseserver.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.example.bookverseserver.dto.request.User.UserCreationRequest;
import com.example.bookverseserver.dto.request.User.UserUpdateRequest;
import com.example.bookverseserver.dto.response.User.UserResponse;
import com.example.bookverseserver.entity.User.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
//    @Mapping(target = "id", ignore = true)
//    @Mapping(target = "lockedUntil", ignore = true)
//    @Mapping(target = "adminNote", ignore = true)
//    @Mapping(target = "roles", ignore = true)
//    @Mapping(target = "userProfile", ignore = true)
//    @Mapping(target = "enabled", constant = "true")
    User toUser(UserCreationRequest request);

    UserResponse toUserResponse(User user);

    //@Mapping(target = "roles", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}
