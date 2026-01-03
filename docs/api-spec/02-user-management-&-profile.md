# Module 02: User Management & Profiles

**Status:** ⚠️ Needs Refinement (57% Complete)  
**Priority:** 🟡 High  
**Sprint:** Sprint 1 (Week 2)  
**Assigned To:** _[To be assigned]_  
**Estimated Effort:** 2 days

---

## 📋 Overview

User profile management including profile creation, updates, avatar uploads, and seller registration. This module handles both regular users and sellers.

**Key Features:**
- User profile CRUD operations
- Avatar image upload
- Seller registration (upgrade from user)
- User role management
- Profile visibility settings

---

## 🎯 Business Rules

1. **Profile Creation:** Required after successful registration
2. **Avatar Upload:** Max 5MB, formats: JPG, PNG, WebP
3. **Seller Upgrade:** Users can upgrade to seller role anytime
4. **Profile Completion:** Certain fields required for seller accounts
5. **Public Profile:** Username, avatar, bio are publicly visible

---

## 📡 API Endpoints

### Endpoint Summary Table

| Method | Endpoint | Description | Auth | Status |
|--------|----------|-------------|------|--------|
| GET | `/api/users/me` | Get current user | Required | ⚠️ Needs Update |
| POST | `/api/users/profile` | Create profile | Required | ✅ Complete |
| PUT | `/api/users/profile` | Update profile | Required | ✅ Complete |
| POST | `/api/users/avatar` | Upload avatar | Required | ✅ Complete |
| POST | `/api/users/upgrade-to-seller` | Upgrade to seller | Required | ⚠️ Needs Verification |
| GET | `/api/users/{id}/public` | Get public profile | Public | ❌ Missing |

---

## 🔧 Implementation Details

### 1. Get Current User ⚠️ NEEDS UPDATE
**GET** `/api/users/me`

**Current Response:**
```json
{
  "id": 123,
  "email": "user@example.com",
  "username": "john_doe",
  "fullName": "John Doe",
  "roles": ["ROLE_USER"]
}
```

**Required Response Format:**
```json
{
  "success": true,
  "data": {
    "id": 123,
    "email": "user@example.com",
    "username": "john_doe",
    "fullName": "John Doe",
    "avatar": "https://cdn.example.com/avatars/john.jpg",
    "bio": "Book enthusiast and collector",
    "phone": "+84123456789",
    "roles": ["ROLE_USER", "ROLE_SELLER"],
    "isSeller": true,
    "isProSeller": false,
    "emailVerified": true,
    "createdAt": "2025-12-01T10:00:00Z",
    "sellerProfile": {
      "businessName": "John's Bookstore",
      "rating": 4.5,
      "totalSales": 150,
      "memberSince": "2025-12-01T10:00:00Z"
    }
  }
}
```

**Required Changes:**
- Add `isSeller` boolean field (true if user has ROLE_SELLER)
- Add `isProSeller` boolean field (true if user has pro seller status)
- Include nested `sellerProfile` object if user is a seller
- Add `avatar`, `bio`, `phone` fields

**Backend Files:**
- `UserController.java` (UPDATE)
- `UserDto.java` (UPDATE - add new fields)
- `SellerProfileDto.java` (NEW)

---

### 2. Create Profile
**POST** `/api/users/profile`

**Request Body:**
```json
{
  "bio": "Passionate reader and book collector",
  "phone": "+84123456789",
  "dateOfBirth": "1990-05-15",
  "address": {
    "street": "123 Book Street",
    "city": "Ho Chi Minh City",
    "country": "Vietnam"
  }
}
```

**Response:** `201 Created`
```json
{
  "success": true,
  "data": {
    "userId": 123,
    "bio": "Passionate reader and book collector",
    "phone": "+84123456789",
    "dateOfBirth": "1990-05-15",
    "address": { ... },
    "createdAt": "2026-01-01T10:00:00Z"
  }
}
```

**Status:** ✅ Already implemented in `UserProfileController.java`

---

### 3. Update Profile
**PUT** `/api/users/profile`

**Request Body:** Same as Create Profile

**Response:** `200 OK` (same structure)

**Status:** ✅ Already implemented

---

### 4. Upload Avatar
**POST** `/api/users/avatar`

**Request:** `multipart/form-data`
```
Content-Type: multipart/form-data
---
file: [binary image data]
```

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "avatarUrl": "https://cdn.example.com/avatars/user-123.jpg"
  }
}
```

**Status:** ✅ Already implemented

**Validation:**
- Max file size: 5MB
- Allowed formats: JPG, PNG, WebP
- Image is resized to 400x400px

---

### 5. Upgrade to Seller ⚠️ NEEDS VERIFICATION
**POST** `/api/users/upgrade-to-seller`

**Request Body:**
```json
{
  "businessName": "John's Rare Books",
  "businessDescription": "Specializing in vintage and rare books",
  "businessEmail": "business@example.com",
  "businessPhone": "+84987654321"
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "userId": 123,
    "isSeller": true,
    "sellerProfile": {
      "businessName": "John's Rare Books",
      "businessDescription": "Specializing in vintage and rare books",
      "businessEmail": "business@example.com",
      "businessPhone": "+84987654321",
      "rating": 0.0,
      "totalSales": 0,
      "memberSince": "2026-01-01T10:00:00Z"
    }
  }
}
```

**Required Verification:**
- ✅ Assigns `ROLE_SELLER` to user
- ✅ Creates entry in `seller_profiles` table
- ⚠️ Verify response format matches above
- ⚠️ Ensure `GET /api/users/me` reflects seller status immediately

**Backend Files:**
- `UserController.java` (VERIFY)
- `SellerProfile.java` entity (VERIFY EXISTS)
- `SellerProfileService.java` (VERIFY)

---

### 6. Get Public Profile ❌ NEW ENDPOINT NEEDED
**GET** `/api/users/{id}/public`

**Description:** Get public information about any user (for profile pages)

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "id": 123,
    "username": "john_doe",
    "fullName": "John Doe",
    "avatar": "https://cdn.example.com/avatars/john.jpg",
    "bio": "Book enthusiast",
    "isSeller": true,
    "memberSince": "2025-12-01T10:00:00Z",
    "sellerProfile": {
      "businessName": "John's Bookstore",
      "rating": 4.5,
      "totalSales": 150,
      "totalReviews": 45
    }
  }
}
```

**Security Note:**
- DO NOT expose: email, phone, address, dateOfBirth
- Only public-safe fields

**Backend Files:**
- `UserController.java` (ADD NEW ENDPOINT)
- `PublicUserDto.java` (NEW DTO)

---

## 🗄️ Database Schema

### User Entity (Already Exists)
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String email;
    private String password;
    private String username;
    private String fullName;
    private String avatar;
    private String bio;
    private String phone;
    
    @ManyToMany
    private Set<Role> roles;
    
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserProfile profile;
    
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private SellerProfile sellerProfile;
}
```

### SellerProfile Entity (VERIFY EXISTS)
```java
@Entity
@Table(name = "seller_profiles")
public class SellerProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;
    
    private String businessName;
    
    @Column(columnDefinition = "TEXT")
    private String businessDescription;
    
    private String businessEmail;
    private String businessPhone;
    
    private Double rating = 0.0;
    private Integer totalSales = 0;
    private Integer totalReviews = 0;
    
    @Column(name = "is_pro_seller")
    private Boolean isProSeller = false;
    
    @Column(name = "member_since")
    private LocalDateTime memberSince;
    
    @PrePersist
    protected void onCreate() {
        memberSince = LocalDateTime.now();
    }
}
```

---

## 📦 DTOs

### UserDto (UPDATE REQUIRED)
```java
public class UserDto {
    private Long id;
    private String email;
    private String username;
    private String fullName;
    private String avatar;
    private String bio;
    private String phone;
    private Set<String> roles;
    
    // NEW FIELDS
    private Boolean isSeller;
    private Boolean isProSeller;
    private Boolean emailVerified;
    private LocalDateTime createdAt;
    
    // NESTED OBJECT
    private SellerProfileDto sellerProfile;
}
```

### SellerProfileDto (NEW)
```java
public class SellerProfileDto {
    private String businessName;
    private String businessDescription;
    private String businessEmail;
    private String businessPhone;
    private Double rating;
    private Integer totalSales;
    private Integer totalReviews;
    private LocalDateTime memberSince;
}
```

### PublicUserDto (NEW)
```java
public class PublicUserDto {
    private Long id;
    private String username;
    private String fullName;
    private String avatar;
    private String bio;
    private Boolean isSeller;
    private LocalDateTime memberSince;
    private SellerProfileDto sellerProfile; // Only public fields
}
```

### UpgradeToSellerRequest (VERIFY)
```java
public class UpgradeToSellerRequest {
    @NotBlank
    private String businessName;
    
    @Size(max = 1000)
    private String businessDescription;
    
    @Email
    private String businessEmail;
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$")
    private String businessPhone;
}
```

---

## 🧪 Testing Requirements

### Unit Tests

```java
@Test
void getUserMe_WithSellerRole_ReturnsSellerProfile() {
    // Given: User with ROLE_SELLER
    // When: GET /api/users/me
    // Then: Response includes isSeller=true and sellerProfile
}

@Test
void getUserMe_RegularUser_ReturnsNoSellerProfile() {
    // Given: User without ROLE_SELLER
    // When: GET /api/users/me
    // Then: Response has isSeller=false, sellerProfile=null
}

@Test
void upgradeToSeller_ValidRequest_CreatesSellerProfile() {
    // Given: Regular user with valid business info
    // When: POST /api/users/upgrade-to-seller
    // Then: User has ROLE_SELLER and seller profile is created
}

@Test
void upgradeToSeller_AlreadySeller_ReturnsConflict() {
    // Given: User already has ROLE_SELLER
    // When: POST /api/users/upgrade-to-seller
    // Then: Throw ConflictException
}

@Test
void getPublicProfile_ExcludesPrivateData() {
    // Given: User ID
    // When: GET /api/users/{id}/public
    // Then: Response does not include email, phone, address
}
```

### Integration Tests

```java
@Test
@WithMockUser
void getUserMe_ReturnsExtendedUserInfo() throws Exception {
    mockMvc.perform(get("/api/users/me"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.isSeller").exists())
        .andExpect(jsonPath("$.data.isProSeller").exists());
}

@Test
@WithMockUser
void upgradeToSeller_Success_Returns200() throws Exception {
    mockMvc.perform(post("/api/users/upgrade-to-seller")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(validRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.isSeller").value(true));
}

@Test
void getPublicProfile_ExistingUser_Returns200() throws Exception {
    mockMvc.perform(get("/api/users/123/public"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.email").doesNotExist());
}
```

---

## 🔗 Dependencies

### Related Modules
- **Module 01:** Authentication (user entity)
- **Module 11:** Seller Dashboard (needs seller profile data)
- **Module 14:** Pro Seller Application (updates isProSeller flag)
- **Module 15:** Public Seller Profiles (uses public profile endpoint)

### External Services
- File storage service (S3/Cloudinary for avatar uploads)
- Image processing service (resize avatars)

---

## ✅ Acceptance Criteria

- [ ] `GET /api/users/me` returns extended user info with seller flags
- [ ] `GET /api/users/me` includes nested sellerProfile when user is seller
- [ ] Users can create and update their profiles
- [ ] Users can upload avatars (max 5MB, JPG/PNG/WebP)
- [ ] Avatars are automatically resized to 400x400px
- [ ] Users can upgrade to seller role with business information
- [ ] Seller upgrade creates seller_profiles entry
- [ ] `POST /api/users/upgrade-to-seller` assigns ROLE_SELLER
- [ ] Public profile endpoint excludes private data
- [ ] All endpoints return standardized response format
- [ ] Unit test coverage ≥ 80%
- [ ] Integration tests pass

---

## 📅 Timeline

| Task | Estimated Time | Assignee | Status |
|------|----------------|----------|--------|
| Update UserDto with new fields | 1 hour | - | ❌ |
| Create SellerProfileDto | 1 hour | - | ❌ |
| Update GET /api/users/me | 2 hours | - | ❌ |
| Verify upgrade-to-seller logic | 2 hours | - | ❌ |
| Create PublicUserDto | 1 hour | - | ❌ |
| Implement GET /api/users/{id}/public | 2 hours | - | ❌ |
| Write unit tests | 3 hours | - | ❌ |
| Write integration tests | 2 hours | - | ❌ |
| Update documentation | 1 hour | - | ❌ |
| **Total** | **15 hours (2 days)** | | |

**Start Date:** Sprint 1, Week 2  
**Target Completion:** Sprint 1, Week 2  
**Blockers:** None

---

## 📝 Notes

- Avatar storage location should be configurable (env variable)
- Consider implementing profile completion percentage (for gamification)
- Future: Add profile badges/achievements
- Future: Add user verification status (verified badge)
- Ensure seller profile data is cached for performance

---

## ✔️ Sign-off

**Developer:** _________________ Date: _______  
**Reviewer:** _________________ Date: _______  
**QA:** _________________ Date: _______