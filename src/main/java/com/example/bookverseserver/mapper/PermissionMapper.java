package com.example.bookverseserver.mapper;

import org.mapstruct.Mapper;

import com.example.bookverseserver.dto.request.Authentication.PermissionRequest;
import com.example.bookverseserver.dto.response.Authentication.PermissionResponse;
import com.example.bookverseserver.entity.User.Permission;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(Permission permission);
}