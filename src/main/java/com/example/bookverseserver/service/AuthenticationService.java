package com.example.bookverseserver.service;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

import com.example.bookverseserver.dto.request.Authentication.*;
import com.example.bookverseserver.dto.response.User.UserResponse;
import com.example.bookverseserver.entity.User.ForgotPasswordOtpStorage;
import com.example.bookverseserver.entity.User.SignupRequest;
import com.example.bookverseserver.mapper.UserMapper;
import com.example.bookverseserver.repository.ForgotPasswordRepository;
import com.example.bookverseserver.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.bookverseserver.dto.response.Authentication.AuthenticationResponse;
import com.example.bookverseserver.dto.response.Authentication.IntrospectResponse;
import com.example.bookverseserver.entity.User.InvalidatedToken;
import com.example.bookverseserver.entity.User.User;
import com.example.bookverseserver.exception.AppException;
import com.example.bookverseserver.exception.ErrorCode;
import com.example.bookverseserver.repository.InvalidatedTokenRepository;
import com.example.bookverseserver.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;
    PasswordEncoder passwordEncoder;
    UserMapper userMapper;
    SignupRequestService signupRequestService;
    ForgotPasswordRepository forgotPasswordRepository;
    EmailService emailService;

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

    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isValid = true;

        try {
            verifyToken(token, false);
        } catch (AppException e) {
            isValid = false;
        }

        return IntrospectResponse.builder().valid(isValid).build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        String input = request.getEmailOrUsername();
        var user = userRepository
                .findByEmailOrUsername(input, input)
                .orElseThrow(() -> {
                    if (input.contains("@")) {
                        return new AppException(ErrorCode.EMAIL_NOT_EXISTED);
                    } else {
                        return new AppException(ErrorCode.USERNAME_NOT_EXISTED);
                    }
                });
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());

        if (!authenticated) throw new AppException(ErrorCode.UNAUTHENTICATED);

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        var token = generateToken(user);

        UserResponse userResponse = userMapper.toUserResponse(user);

        return AuthenticationResponse.builder().token(token).authenticated(true).lastLogin(LocalDateTime.now()).user(userResponse).build();
    }

    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        try {
            var signToken = verifyToken(request.getToken(), true);

            String jit = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken =
                    InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();

            invalidatedTokenRepository.save(invalidatedToken);
        } catch (AppException exception) {
            log.info("Token already expired");
        }
    }

    public AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
        var signedJWT = verifyToken(request.getToken(), true);

        var jit = signedJWT.getJWTClaimsSet().getJWTID();
        var expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        InvalidatedToken invalidatedToken =
                InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();

        invalidatedTokenRepository.save(invalidatedToken);

        var username = signedJWT.getJWTClaimsSet().getSubject();

        var user =
                userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        var token = generateToken(user);

        return AuthenticationResponse.builder().token(token).authenticated(true).build();
    }

    protected String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("bookverse.com")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }

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

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");

        if (!CollectionUtils.isEmpty(user.getRoles()))
            user.getRoles().forEach(role -> {
                stringJoiner.add("ROLE_" + role.getName());
            });

        return stringJoiner.toString();
    }

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

            // 3. Xác minh tính hợp lệ của OTP và thời gian hết hạn
            // Kiểm tra OTP đã nhập có khớp với OTP đã hash trong database không
            String inputOtpHash = signupRequestService.hmacOtp(forgotPasswordRequest.getOtp());

            if (!inputOtpHash.equals(request.getOtpHash())) {
                throw new AppException(ErrorCode.INVALID_OTP);
            }

            // Kiểm tra OTP đã hết hạn chưa
            if (request.getExpiresAt().isBefore(Instant.now())) {
                // Sau khi hết hạn, ta nên xóa bản ghi OTP cũ
                forgotPasswordRepository.delete(request);
                throw new AppException(ErrorCode.OTP_EXPIRED);
            }

            // 4. Cập nhật Mật khẩu
            // Tìm kiếm thông tin người dùng dựa trên email đã lưu
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            // Cập nhật mật khẩu mới (cần dùng passwordEncoder để mã hóa mật khẩu)
            user.setPasswordHash(passwordEncoder.encode(forgotPasswordRequest.getPassword()));
            userRepository.save(user);

            // 5. Dọn dẹp
            // Xóa bản ghi OTP sau khi đã sử dụng thành công
            forgotPasswordRepository.delete(request);

            log.info("Password successfully changed for user {}", request.getEmail());

            return userMapper.toUserResponse(user);
        }
}