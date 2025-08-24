package com.example.bookverseserver.service;

import com.example.bookverseserver.dto.request.User.ProfileCreationRequest;
import com.example.bookverseserver.dto.response.User.ProfileResponse;
import com.example.bookverseserver.entity.User.UserProfile;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.mapper.UserProfileMapper;
import com.example.bookverseserver.repository.UserProfileRepository;
import com.example.bookverseserver.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserProfileService {
    UserRepository userRepository;
    UserProfileRepository userProfileRepository;
    UserProfileMapper userProfileMapper;

    public ProfileResponse createUserProfile(ProfileCreationRequest request) {
        UserProfile userProfile = userProfileMapper.toProfile(request);
        try {
            userProfile = userProfileRepository.save(userProfile);
        } catch (DataIntegrityViolationException exception) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        return userProfileMapper.toprofileResponse(userProfile);
    }

}
