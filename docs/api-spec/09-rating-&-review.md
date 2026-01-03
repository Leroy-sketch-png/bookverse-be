# Module 09: Reviews & Ratings System

**Status:** ❌ Missing (0% Complete)  
**Priority:** 🔴 CRITICAL  
**Sprint:** Sprint 1 (Weeks 1-2)  
**Assigned To:** _[To be assigned]_  
**Estimated Effort:** 3-4 days

---

## 📋 Overview

Reviews and ratings are essential for building trust in the marketplace. Users should be able to rate and review books they've purchased, and these reviews should influence book discovery and rankings.

**Key Features:**
- 5-star rating system
- Text reviews with comments
- One review per user per book
- Helpful votes on reviews
- Aggregate ratings displayed on book cards
- Review moderation capabilities

---

## 🎯 Business Rules

1. **Authorization:** Only authenticated users can create reviews
2. **Purchase Verification:** User should have purchased the book (optional enforcement)
3. **Uniqueness:** One review per user per book (enforced at DB level)
4. **Editing:** Users can edit/delete their own reviews
5. **Rating Range:** 1-5 stars (integers only)
6. **Helpful Votes:** Users can mark reviews as helpful (one vote per review)
7. **Moderation:** Admins can hide/delete inappropriate reviews

---

## 📡 API Endpoints

### Endpoint Summary Table

| Method | Endpoint | Description | Auth | Status |
|--------|----------|-------------|------|--------|
| POST | `/api/books/{bookId}/reviews` | Create review | Required | ❌ Not Implemented |
| GET | `/api/books/{bookId}/reviews` | List reviews | Public | ❌ Not Implemented |
| PUT | `/api/reviews/{id}` | Update review | Owner | ❌ Not Implemented |
| DELETE | `/api/reviews/{id}` | Delete review | Owner/Admin | ❌ Not Implemented |
| GET | `/api/books/{bookId}/rating` | Get aggregate rating | Public | ❌ Not Implemented |
| POST | `/api/reviews/{id}/helpful` | Mark helpful | Required | ❌ Not Implemented |
| GET | `/api/users/me/reviews` | My reviews | Required | ❌ Not Implemented |
| PATCH | `/api/admin/reviews/{id}/hide` | Hide review | Admin | ❌ Not Implemented |

---

## 🔧 Implementation Details

### 1. Create Review
**POST** `/api/books/{bookId}/reviews`

**Request Body:**
```json
{
  "rating": 5,
  "comment": "Excellent book! Highly recommend for beginners."
}
```

**Validation:**
- `rating`: Required, integer, 1-5
- `comment`: Optional, string, max 2000 chars

**Response:** `201 Created`
```json
{
  "success": true,
  "data": {
    "id": 123,
    "bookId": 456,
    "userId": 789,
    "user": {
      "id": 789,
      "username": "john_doe",
      "avatar": "https://cdn.example.com/avatars/john.jpg"
    },
    "rating": 5,
    "comment": "Excellent book! Highly recommend for beginners.",
    "helpfulCount": 0,
    "createdAt": "2026-01-01T10:00:00Z",
    "updatedAt": "2026-01-01T10:00:00Z"
  }
}
```

**Error Cases:**
- `400 Bad Request`: Invalid rating value or comment too long
- `401 Unauthorized`: User not authenticated
- `404 Not Found`: Book not found
- `409 Conflict`: User already reviewed this book

---

### 2. List Reviews
**GET** `/api/books/{bookId}/reviews`

**Query Parameters:**
- `page` (default: 0)
- `size` (default: 10)
- `sortBy` (options: `rating`, `createdAt`, `helpfulCount`, default: `createdAt`)
- `sortOrder` (options: `asc`, `desc`, default: `desc`)
- `rating` (filter by specific rating, e.g., `rating=5` for 5-star only)

**Response:** `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "id": 123,
      "user": {
        "id": 789,
        "username": "john_doe",
        "avatar": "https://cdn.example.com/avatars/john.jpg"
      },
      "rating": 5,
      "comment": "Excellent book!",
      "helpfulCount": 15,
      "createdAt": "2026-01-01T10:00:00Z",
      "isCurrentUserReview": false
    }
  ],
  "meta": {
    "page": 0,
    "totalPages": 5,
    "totalItems": 48,
    "itemsPerPage": 10
  }
}
```

---

### 3. Update Review
**PUT** `/api/reviews/{id}`

**Authorization:** Must be review owner

**Request Body:**
```json
{
  "rating": 4,
  "comment": "Updated: Good book, but a bit challenging."
}
```

**Response:** `200 OK` (same structure as Create Review)

**Error Cases:**
- `403 Forbidden`: User is not the review owner
- `404 Not Found`: Review not found

---

### 4. Delete Review
**DELETE** `/api/reviews/{id}`

**Authorization:** Must be review owner or admin

**Response:** `204 No Content`

**Error Cases:**
- `403 Forbidden`: User is not authorized
- `404 Not Found`: Review not found

---

### 5. Get Aggregate Rating
**GET** `/api/books/{bookId}/rating`

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "bookId": 456,
    "averageRating": 4.3,
    "totalReviews": 127,
    "ratingDistribution": {
      "5": 68,
      "4": 35,
      "3": 15,
      "2": 7,
      "1": 2
    }
  }
}
```

---

### 6. Mark Review as Helpful
**POST** `/api/reviews/{id}/helpful`

**Authorization:** Required (can only vote once per review)

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "reviewId": 123,
    "helpfulCount": 16,
    "userHasVoted": true
  }
}
```

**Error Cases:**
- `409 Conflict`: User already marked this review as helpful

---

### 7. Get My Reviews
**GET** `/api/users/me/reviews`

**Query Parameters:** `page`, `size`

**Response:** `200 OK` (paginated list of user's reviews with book details)

---

### 8. Hide Review (Admin)
**PATCH** `/api/admin/reviews/{id}/hide`

**Authorization:** Admin only

**Request Body:**
```json
{
  "hidden": true,
  "reason": "Inappropriate content"
}
```

**Response:** `200 OK`

---

## 🗄️ Database Schema

### Review Entity

```java
@Entity
@Table(
    name = "reviews",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "book_id"})
)
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private BookMeta book;
    
    @Column(nullable = false)
    @Min(1)
    @Max(5)
    private Integer rating;
    
    @Column(columnDefinition = "TEXT")
    @Size(max = 2000)
    private String comment;
    
    @Column(name = "helpful_count", nullable = false)
    private Integer helpfulCount = 0;
    
    @Column(name = "is_hidden")
    private Boolean isHidden = false;
    
    @Column(name = "hidden_reason")
    private String hiddenReason;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

### ReviewHelpful Entity (Track helpful votes)

```java
@Entity
@Table(
    name = "review_helpful",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "review_id"})
)
public class ReviewHelpful {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

### Database Migration

```sql
-- Create reviews table
CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    book_id BIGINT NOT NULL REFERENCES book_meta(id) ON DELETE CASCADE,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    helpful_count INTEGER NOT NULL DEFAULT 0,
    is_hidden BOOLEAN DEFAULT FALSE,
    hidden_reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_user_book_review UNIQUE (user_id, book_id)
);

-- Create review_helpful table
CREATE TABLE review_helpful (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    review_id BIGINT NOT NULL REFERENCES reviews(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_user_review_helpful UNIQUE (user_id, review_id)
);

-- Create indexes for performance
CREATE INDEX idx_reviews_book_id ON reviews(book_id);
CREATE INDEX idx_reviews_user_id ON reviews(user_id);
CREATE INDEX idx_reviews_rating ON reviews(rating);
CREATE INDEX idx_reviews_created_at ON reviews(created_at DESC);
CREATE INDEX idx_reviews_helpful_count ON reviews(helpful_count DESC);
CREATE INDEX idx_review_helpful_review_id ON review_helpful(review_id);
```

---

## 📦 DTOs

### ReviewDto
```java
public class ReviewDto {
    private Long id;
    private Long bookId;
    private UserSummaryDto user; // Nested: id, username, avatar
    private Integer rating;
    private String comment;
    private Integer helpfulCount;
    private Boolean isCurrentUserReview; // True if review belongs to current user
    private Boolean userHasVotedHelpful; // True if current user voted helpful
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### CreateReviewRequest
```java
public class CreateReviewRequest {
    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;
    
    @Size(max = 2000)
    private String comment;
}
```

### BookRatingDto
```java
public class BookRatingDto {
    private Long bookId;
    private Double averageRating; // Rounded to 1 decimal place
    private Long totalReviews;
    private Map<Integer, Long> ratingDistribution; // {5: 68, 4: 35, ...}
}
```

---

## 🧪 Testing Requirements

### Unit Tests (ReviewService)

```java
@Test
void createReview_Success() {
    // Given: Valid user, book, and review data
    // When: createReview() is called
    // Then: Review is saved with correct data
}

@Test
void createReview_DuplicateReview_ThrowsException() {
    // Given: User already reviewed this book
    // When: createReview() is called again
    // Then: Throw ConflictException
}

@Test
void createReview_InvalidRating_ThrowsException() {
    // Given: Rating outside 1-5 range
    // When: createReview() is called
    // Then: Throw ValidationException
}

@Test
void updateReview_NotOwner_ThrowsException() {
    // Given: Different user tries to update review
    // When: updateReview() is called
    // Then: Throw ForbiddenException
}

@Test
void getAggregateRating_CalculatesCorrectly() {
    // Given: Multiple reviews with different ratings
    // When: getAggregateRating() is called
    // Then: Return correct average and distribution
}

@Test
void markHelpful_Idempotent() {
    // Given: User already marked review helpful
    // When: markHelpful() is called again
    // Then: Throw ConflictException or ignore
}
```

### Integration Tests (ReviewController)

```java
@Test
@WithMockUser
void createReview_ValidRequest_Returns201() throws Exception {
    mockMvc.perform(post("/api/books/1/reviews")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(validRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.rating").value(5));
}

@Test
void listReviews_WithPagination_ReturnsCorrectPage() throws Exception {
    mockMvc.perform(get("/api/books/1/reviews")
        .param("page", "0")
        .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.meta.page").value(0))
        .andExpect(jsonPath("$.data").isArray());
}

@Test
@WithMockUser
void updateReview_NotOwner_Returns403() throws Exception {
    mockMvc.perform(put("/api/reviews/999")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isForbidden());
}
```

---

## 🔗 Dependencies

### Related Modules
- **Module 03:** Books Catalog (must include `averageRating`, `totalReviews`)
- **Module 02:** User Management (user data for reviews)
- **Module 07:** Orders (optional: verify purchase before review)

### External Services
- None

### Internal Services
- Email Service: Send notification when seller receives review
- Notification Service: Notify user when their review receives helpful votes

---

## ✅ Acceptance Criteria

- [ ] Users can create reviews with 1-5 star rating and text comment
- [ ] System enforces one review per user per book
- [ ] Reviews are displayed on book detail pages with pagination
- [ ] Aggregate rating is calculated correctly and displayed on book cards
- [ ] Users can edit and delete their own reviews
- [ ] Users can mark reviews as helpful (one vote per review)
- [ ] Review list supports sorting by rating, date, helpful count
- [ ] Review list supports filtering by specific rating (e.g., only 5-star)
- [ ] Admins can hide inappropriate reviews
- [ ] All endpoints return standardized response format
- [ ] Unit test coverage ≥ 80%
- [ ] Integration tests pass for all endpoints

---

## 📅 Timeline

| Task | Estimated Time | Assignee | Status |
|------|----------------|----------|--------|
| Create Entity & Repository | 2 hours | - | ❌ |
| Implement Service Layer | 4 hours | - | ❌ |
| Create Controller & DTOs | 3 hours | - | ❌ |
| Database Migration | 1 hour | - | ❌ |
| Write Unit Tests | 4 hours | - | ❌ |
| Write Integration Tests | 3 hours | - | ❌ |
| Update Books API (add ratings) | 2 hours | - | ❌ |
| Documentation | 1 hour | - | ❌ |
| **Total** | **20 hours (2.5 days)** | | |

**Start Date:** TBD  
**Target Completion:** Sprint 1, Week 1  
**Blockers:** None

---

## 📝 Notes

- Consider implementing spam detection for reviews (rate limiting, duplicate content check)
- Future enhancement: Image uploads in reviews
- Future enhancement: Seller responses to reviews
- Consider caching aggregate ratings (Redis) for performance
- Review moderation queue for admins to review flagged content

---

## ✔️ Sign-off

**Developer:** _________________ Date: _______  
**Reviewer:** _________________ Date: _______  
**QA:** _________________ Date: _______