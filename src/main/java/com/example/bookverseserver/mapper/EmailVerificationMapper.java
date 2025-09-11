package com.example.bookverseserver.mapper;

import com.example.bookverseserver.dto.request.Authentication.EmailVerificationRequest;
import com.example.bookverseserver.entity.User.EmailVerificationToken;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EmailVerificationMapper {
    EmailVerificationToken toEmailVerification(EmailVerificationRequest request);
}