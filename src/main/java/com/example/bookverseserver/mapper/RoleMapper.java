package com.example.bookverseserver.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.bookverseserver.dto.request.Authentication.RoleRequest;
import com.example.bookverseserver.dto.response.Authentication.RoleResponse;
import com.example.bookverseserver.entity.User.Role;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    //@Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);
}