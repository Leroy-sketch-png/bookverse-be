package com.example.bookverseserver.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.example.bookverseserver.dto.request.Authentication.ChangePasswordRequest;
import com.example.bookverseserver.dto.request.Authentication.UserStatusRequest;
import com.example.bookverseserver.utils.SecurityUtils;
import lombok.AccessLevel;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.bookverseserver.dto.request.User.UserCreationRequest;
import com.example.bookverseserver.dto.request.User.UserUpdateRequest;
import com.example.bookverseserver.dto.response.User.UserResponse;
import com.example.bookverseserver.entity.User.Role;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.enums.RoleName;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.mapper.UserMapper;
import com.example.bookverseserver.repository.RoleRepository;
import com.example.bookverseserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {
    EmailService emailService;
    UserRepository userRepository;
    RoleRepository roleRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    OtpService otpService;
    SecurityUtils securityUtils;

    public UserResponse createUser(UserCreationRequest request) {
        User user = userMapper.toUser(request);
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        // Set default role USER
        Role userRole = roleRepository.findByName(RoleName.USER)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        user.setEnabled(true);

        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException exception) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        otpService.generateAndSendOtp(request.getEmail());

        return userMapper.toUserResponse(user);
    }

    public UserResponse getMyInfo(Authentication authentication) {
        var context = SecurityContextHolder.getContext();
        Long userId = securityUtils.getCurrentUserId(authentication);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return userMapper.toUserResponse(user);
    }

    @PostAuthorize("returnObject.username == authentication.name")
    public UserResponse updateUser(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        userMapper.updateUser(user, request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        // Update roles - support multiple roles
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            Set<Role> roles = new HashSet<>();
            for (String roleStr : request.getRoles()) {
                RoleName roleName = RoleName.valueOf(roleStr);
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
                roles.add(role);
            }
            user.setRoles(roles);
        }

        return userMapper.toUserResponse(userRepository.save(user));
    }

    public void changePassword(ChangePasswordRequest request, Authentication authentication) {
        // 1. Lấy username/email từ SecurityContext (Token)
        Long userId = securityUtils.getCurrentUserId(authentication);

        // 2. Tìm User trong DB
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 3. KIỂM TRA MẬT KHẨU CŨ (Rất quan trọng)
        // Nếu mật khẩu cũ nhập vào KHÔNG khớp với Hash trong DB -> Báo lỗi
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.INVALID_PASSWORD); // "Mật khẩu cũ không đúng"
        }

        // 4. Mã hóa mật khẩu MỚI và lưu
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        
        // P1 Security Fix #H1: Update password change timestamp to invalidate old tokens
        user.setPasswordChangedAt(LocalDateTime.now());
        
        userRepository.save(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void updateUserStatus(Long userId, UserStatusRequest request) {
        // 1. Tìm user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // 2. Cập nhật trạng thái
        user.setEnabled(request.isEnabled());

        // 3. Lưu xuống DB
        userRepository.save(user);

        // (Nâng cao: Nếu tắt tài khoản, bạn có thể cần logic để invalidate token hiện tại
        // của user đó, nhưng với JWT cơ bản thì cập nhật DB là đủ chặn lần đăng nhập sau).
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getUsers() {
        log.info("In method get Users");
        return userRepository.findAll().stream().map(userMapper::toUserResponse).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getUser(Long id) {
        return userMapper.toUserResponse(
                userRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)));
    }

    public Optional<User> findByGoogleId(String googleId) {
        return userRepository.findByGoogleId(googleId);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }

    /**
     * Create or update a user identity for the Google provider.
     */
    public User createOrUpdateGoogleUser(String googleId, String email) {
        User user = findByGoogleId(googleId)
                .or(() -> findByEmail(email))
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setGoogleId(googleId);
                    newUser.setEmail(email);
                    newUser.setUsername(email);
                    newUser.setAuthProvider("GOOGLE");
                    newUser.setEnabled(true);

                    // Set default role USER for Google OAuth
                    Role userRole = roleRepository.findByName(RoleName.USER)
                            .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
                    Set<Role> roles = new HashSet<>();
                    roles.add(userRole);
                    newUser.setRoles(roles);
                    return newUser;
                });

        user.setLastLogin(LocalDateTime.now());
        return userRepository.save(user);
    }
}
