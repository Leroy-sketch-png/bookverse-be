package com.example.bookverseserver.service;

import java.text.ParseException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;


import com.example.bookverseserver.dto.request.Authentication.*;
import com.example.bookverseserver.dto.response.Authentication.RefreshResponse;
import com.example.bookverseserver.dto.response.User.UserResponse;
import com.example.bookverseserver.entity.User.ForgotPasswordOtpStorage;
import com.example.bookverseserver.mapper.UserMapper;
import com.example.bookverseserver.repository.ForgotPasswordRepository;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.bookverseserver.dto.response.Authentication.AuthenticationResponse;
import com.example.bookverseserver.dto.response.Authentication.IntrospectResponse;
import com.example.bookverseserver.entity.User.InvalidatedToken;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.repository.InvalidatedTokenRepository;
import com.example.bookverseserver.repository.UserRepository;






import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;


@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)  // Default read-only, override for write methods
public class AuthenticationService {
    UserRepository userRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;
    PasswordEncoder passwordEncoder;
    UserMapper userMapper;
    SignupRequestService signupRequestService;
    ForgotPasswordRepository forgotPasswordRepository;
    EmailService emailService;
    NimbusJwtService jwtService;

    // P0 Security Fix #6: Brute-force protection constants
    private static final int MAX_FAILED_LOGIN_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 15;
    
    // P0 Security Fix #5: OTP brute-force protection
    private static final int MAX_OTP_ATTEMPTS = 5;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

    @NonFinal
    @Value("${app.otp.ttl-seconds:600}")
    private long otpTtlSeconds;

    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = (isRefresh)
                ? new Date(signedJWT
                .getJWTClaimsSet()
                .getIssueTime()
                .toInstant()
                .plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS)
                .toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        if (!(verified && expiryTime.after(new Date()))) throw new AppException(ErrorCode.UNAUTHENTICATED);

        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        return signedJWT;
    }

    @Transactional
    public RefreshResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
        var signedJWT = verifyToken(request.getToken(), true);

        var jit = signedJWT.getJWTClaimsSet().getJWTID();
        var expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        // Invalidate token cũ
        InvalidatedToken invalidatedToken =
                InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();

        invalidatedTokenRepository.save(invalidatedToken);

        // Sinh token mới
        var username = signedJWT.getJWTClaimsSet().getSubject();
        var user = userRepository.findById(Long.valueOf(username))
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        var newToken = jwtService.generateToken(user);

        // Trả token mới về cho client
        return RefreshResponse.builder()
                .token(newToken)
                .build();
    }


    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isValid = true;

        try {
            jwtService.introspectToken(token);
        } catch (AppException e) {
            isValid = false;
        }

        return IntrospectResponse.builder().valid(isValid).build();
    }

    @Transactional
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        String input = request.getEmailOrUsername();
        
        // P0 Security Fix #17: Use case-insensitive lookup to prevent duplicate accounts
        // P0 Security Fix #11: Use generic error to prevent account enumeration
        var userOptional = userRepository.findByEmailOrUsernameIgnoreCase(input, input);
        
        if (userOptional.isEmpty()) {
            // Don't reveal whether email/username exists
            log.warn("Login attempt for non-existent account: {}", input);
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }
        
        User user = userOptional.get();
        
        // P0 Security Fix #6: Check if account is locked
        if (user.getLockedUntil() != null && LocalDateTime.now().isBefore(user.getLockedUntil())) {
            log.warn("Login attempt on locked account: {}", input);
            throw new AppException(ErrorCode.ACCOUNT_LOCKED);
        }
        
        // Check password
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());

        if (!authenticated) {
            // P0 Security Fix #6: Increment failed login count
            int failedAttempts = user.getFailedLoginCount() + 1;
            user.setFailedLoginCount(failedAttempts);
            
            if (failedAttempts >= MAX_FAILED_LOGIN_ATTEMPTS) {
                // Lock the account
                user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCKOUT_DURATION_MINUTES));
                log.warn("Account locked after {} failed attempts: {}", failedAttempts, input);
            }
            
            userRepository.save(user);
            
            // P0 Security Fix #11: Generic error message
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }
        
        // P0 Security Fix: Verify email before allowing login
        if (!user.getEnabled()) {
            throw new AppException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        // Successful login - reset failed attempts
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        var token = jwtService.generateToken(user);

        UserResponse userResponse = userMapper.toUserResponse(user);

        return AuthenticationResponse.builder().token(token).authenticated(true).lastLogin(LocalDateTime.now()).user(userResponse).build();
    }

    @Transactional
    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        try {
            var signToken = jwtService.verifyToken(request.getToken(), true);

            String jit = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken =
                    InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();

            invalidatedTokenRepository.save(invalidatedToken);
        } catch (AppException exception) {
            log.info("Token already expired");
        }
    }

//    public AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
//        var signedJWT = jwtService.verifyToken(request.getToken(), true);
//
//        var jit = signedJWT.getJWTClaimsSet().getJWTID();
//        var expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
//
//        InvalidatedToken invalidatedToken =
//                InvalidatedToken.builder()
//                        .id(jit)
//                        .expiryTime(expiryTime)
//                        .build();
//
//        invalidatedTokenRepository.save(invalidatedToken);
//
//        var username = signedJWT.getJWTClaimsSet().getSubject();
//
//        var user =
//                userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
//
//        var token = jwtService.generateToken(user);
//
//        return AuthenticationResponse.builder().token(token).authenticated(true).build();
//    }







    @Transactional
    public String forgotPasswordOtp(String email) {
        String otp = signupRequestService.generateOtpCode();
        String otpHash = signupRequestService.hmacOtp(otp);
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(otpTtlSeconds);

        forgotPasswordRepository.findByEmail(email).ifPresent(existing -> {
            forgotPasswordRepository.delete(existing);
            log.info("Replaced existing signup request for {}", email);
        });

        ForgotPasswordOtpStorage request =  new ForgotPasswordOtpStorage();
        request.setOtpHash(otpHash);
        request.setExpiresAt(expiresAt);
        request.setEmail(email);
        request.setCreatedAt(Instant.now());

        forgotPasswordRepository.save(request);
        emailService.sendOtpEmail(email, otp);

        return otp;
    }

    @Transactional
    public UserResponse  verifyOtpAndChangePassword(ForgotPasswordRequest forgotPasswordRequest) {
            // 1. Xác thực Mật khẩu
            if (!forgotPasswordRequest.getPassword().equals(forgotPasswordRequest.getConfirmPassword())) {
                throw new AppException(ErrorCode.PASSWORDS_MISMATCH);
            }

            // 2. Tìm kiếm và Xác minh OTP
            // Phương thức findByEmail là hợp lý nhất, vì người dùng gửi yêu cầu quên mật khẩu bằng email
            // và ta cần tìm bản ghi OTP dựa trên email đó.
            ForgotPasswordOtpStorage request = forgotPasswordRepository.findByEmail(forgotPasswordRequest.getEmail())
                    .orElseThrow(() -> new AppException(ErrorCode.OTP_NOT_FOUND));

            // P0 Security Fix #5: Check if too many OTP attempts
            if (request.getAttempts() != null && request.getAttempts() >= MAX_OTP_ATTEMPTS) {
                forgotPasswordRepository.delete(request);
                log.warn("OTP brute-force attempt detected for email: {}", forgotPasswordRequest.getEmail());
                throw new AppException(ErrorCode.TOO_MANY_OTP_ATTEMPTS);
            }

            // 3. Xác minh tính hợp lệ của OTP và thời gian hết hạn
            // Kiểm tra OTP đã hết hạn chưa
            if (request.getExpiresAt().isBefore(Instant.now())) {
                // Sau khi hết hạn, ta nên xóa bản ghi OTP cũ
                forgotPasswordRepository.delete(request);
                throw new AppException(ErrorCode.OTP_EXPIRED);
            }

            // Kiểm tra OTP đã nhập có khớp với OTP đã hash trong database không
            String inputOtpHash = signupRequestService.hmacOtp(forgotPasswordRequest.getOtp());

            if (!inputOtpHash.equals(request.getOtpHash())) {
                // P0 Security Fix #5: Increment attempt count
                request.setAttempts((request.getAttempts() == null ? 0 : request.getAttempts()) + 1);
                forgotPasswordRepository.save(request);
                log.warn("Invalid OTP attempt {} for email: {}", request.getAttempts(), forgotPasswordRequest.getEmail());
                throw new AppException(ErrorCode.INVALID_OTP);
            }

            // 4. Cập nhật Mật khẩu
            // Tìm kiếm thông tin người dùng dựa trên email đã lưu
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            // Cập nhật mật khẩu mới (cần dùng passwordEncoder để mã hóa mật khẩu)
            user.setPasswordHash(passwordEncoder.encode(forgotPasswordRequest.getPassword()));
            
            // P1 Security Fix #H1: Update password change timestamp to invalidate old tokens
            user.setPasswordChangedAt(LocalDateTime.now());
            
            userRepository.save(user);

            // 5. Dọn dẹp
            // Xóa bản ghi OTP sau khi đã sử dụng thành công
            forgotPasswordRepository.delete(request);

            log.info("Password successfully changed for user {}", request.getEmail());

            return userMapper.toUserResponse(user);
        }
}