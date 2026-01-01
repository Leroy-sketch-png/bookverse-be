# Module 01: Authentication & Authorization

**Status:** ✅ Completed (100%)  
**Priority:** 🔴 CRITICAL  
**Sprint:** Pre-Sprint (Foundation)  
**Assigned To:** Backend Team  
**Completed Date:** December 2025

---

## 📋 Overview

Complete authentication and authorization system including JWT-based authentication, OTP email verification, OAuth integration, and token management.

**Key Features:**
- Email/password registration with OTP verification
- JWT token-based authentication
- Google OAuth integration
- Token refresh mechanism
- Token introspection
- Secure logout

---

## 🎯 Business Rules

1. **Registration:** Email must be verified via OTP before account activation
2. **OTP Validity:** OTP expires after 10 minutes
3. **Token Expiry:** Access tokens expire after 1 hour, refresh tokens after 7 days
4. **Password Requirements:** Minimum 8 characters, must include uppercase, lowercase, number
5. **Rate Limiting:** Max 5 login attempts per 15 minutes per IP
6. **OAuth:** Google authentication creates account automatically if not exists

---

## 📡 API Endpoints

### Endpoint Summary Table

| Method | Endpoint | Description | Auth | Status |
|--------|----------|-------------|------|--------|
| POST | `/api/auth/register` | Register new user | None | ✅ Complete |
| POST | `/api/auth/verify-otp` | Verify email OTP | None | ✅ Complete |
| POST | `/api/auth/login` | Login with credentials | None | ✅ Complete |
| POST | `/api/auth/refresh` | Refresh access token | Refresh Token | ✅ Complete |
| POST | `/api/auth/logout` | Invalidate tokens | Required | ✅ Complete |
| POST | `/api/auth/introspect` | Validate token | Required | ✅ Complete |
| POST | `/api/auth/google` | Google OAuth login | None | ✅ Complete |

---

## 🔧 Implementation Details

### 1. Register User
**POST** `/api/auth/register`

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "SecurePass123",
  "username": "john_doe",
  "fullName": "John Doe"
}
```

**Response:** `201 Created`
```json
{
  "success": true,
  "data": {
    "userId": 123,
    "email": "user@example.com",
    "message": "OTP sent to email. Please verify to complete registration."
  }
}
```

**Backend Files:**
- `AuthenticationController.java`
- `AuthenticationService.java`
- `User.java` entity

---

### 2. Verify OTP
**POST** `/api/auth/verify-otp`

**Request Body:**
```json
{
  "email": "user@example.com",
  "otp": "123456"
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "verified": true,
    "message": "Email verified successfully. You can now login."
  }
}
```

**Backend Files:**
- `OtpController.java`
- `OtpService.java`

---

### 3. Login
**POST** `/api/auth/login`

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "SecurePass123"
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": {
      "id": 123,
      "email": "user@example.com",
      "username": "john_doe",
      "roles": ["ROLE_USER"]
    }
  }
}
```

---

### 4. Refresh Token
**POST** `/api/auth/refresh`

**Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

**Response:** `200 OK` (returns new access token)

---

### 5. Logout
**POST** `/api/auth/logout`

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Logged out successfully"
}
```

---

### 6. Introspect Token
**POST** `/api/auth/introspect`

**Request Body:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs..."
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "valid": true,
    "userId": 123,
    "email": "user@example.com",
    "roles": ["ROLE_USER"],
    "expiresAt": "2026-01-01T12:00:00Z"
  }
}
```

---

### 7. Google OAuth
**POST** `/api/auth/google`

**Request Body:**
```json
{
  "idToken": "google_id_token_here"
}
```

**Response:** `200 OK` (same structure as login)

---

## 🗄️ Database Schema

### User Entity (Already Implemented)

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Column(unique = true)
    private String username;
    
    private String fullName;
    
    @Column(nullable = false)
    private Boolean emailVerified = false;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### Token Entity

```java
@Entity
@Table(name = "tokens")
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String token;
    
    @Enumerated(EnumType.STRING)
    private TokenType tokenType; // ACCESS, REFRESH
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    private Boolean revoked = false;
    private Boolean expired = false;
    
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}
```

---

## 📦 DTOs

All DTOs already implemented in:
- `AuthenticationRequest.java`
- `AuthenticationResponse.java`
- `RegisterRequest.java`
- `OtpVerificationRequest.java`
- `TokenRefreshRequest.java`

---

## 🧪 Testing Status

### Unit Tests
- ✅ AuthenticationService tests complete
- ✅ OtpService tests complete
- ✅ JwtService tests complete

### Integration Tests
- ✅ All controller endpoints tested
- ✅ Security configuration tested
- ✅ OAuth flow tested

**Test Coverage:** 85%

---

## 🔗 Dependencies

### Related Modules
- **Module 02:** User Management (user profile creation after registration)
- **All Modules:** All protected endpoints depend on this module

### External Services
- JWT library (io.jsonwebtoken)
- Google OAuth library
- Email service (for OTP)

---

## ✅ Verification Checklist

- ✅ User can register with email/password
- ✅ OTP is sent to email and can be verified
- ✅ User can login and receive JWT tokens
- ✅ Access token expires after 1 hour
- ✅ Refresh token can generate new access token
- ✅ Logout invalidates tokens
- ✅ Token introspection validates tokens correctly
- ✅ Google OAuth creates/authenticates users
- ✅ Password is hashed with BCrypt
- ✅ Security configurations prevent unauthorized access
- ✅ Rate limiting prevents brute force attacks
- ✅ All endpoints return standardized response format
- ✅ Unit test coverage ≥ 80%
- ✅ Integration tests pass

---

## 📝 Notes

- JWT secret should be stored in environment variables
- Implement token rotation for enhanced security
- Consider adding 2FA for enhanced security (future)
- Monitor failed login attempts for security alerts
- Implement "Remember Me" functionality (future)

---

## ✔️ Sign-off

**Developer:** Backend Team - Date: Dec 2025  
**Reviewer:** Tech Lead - Date: Dec 2025  
**QA:** QA Team - Date: Dec 2025  
**Status:** ✅ Production Ready