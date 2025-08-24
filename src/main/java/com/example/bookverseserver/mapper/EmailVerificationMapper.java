package com.example.bookverseserver.mapper;

import com.example.bookverseserver.dto.request.Authentication.EmailVerificationRequest;
import com.example.bookverseserver.entity.User.EmailVerificationToken;
import org.mapstruct.Mapper;

import com.example.bookverseserver.dto.response.Authentication.PermissionResponse;
import com.example.bookverseserver.entity.User.Permission;

@Mapper(componentModel = "spring")
public interface EmailVerificationMapper {
    EmailVerificationToken toEmailVerification(EmailVerificationRequest request);

    PermissionResponse toPermissionResponse(Permission permission);
}