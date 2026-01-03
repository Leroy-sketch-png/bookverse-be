# Module 10: Wishlist System

**Status:** ❌ Missing (0% Complete)  
**Priority:** 🔴 CRITICAL  
**Sprint:** Sprint 1 (Weeks 1-2)  
**Assigned To:** _[To be assigned]_  
**Estimated Effort:** 1.5 days

---

## 📋 Overview

Wishlist allows users to save books they're interested in for future reference. This feature is essential for user engagement and conversion tracking.

**Key Features:**
- Add/remove books from wishlist
- View all wishlisted books
- Check if specific book is in wishlist
- Wishlist count in header
- Sync across devices (tied to user account)

---

## 🎯 Business Rules

1. **Authorization:** Only authenticated users can manage wishlist
2. **Uniqueness:** Each book can only be added once per user
3. **Persistence:** Wishlist persists across sessions
4. **Removal:** Removing from wishlist is idempotent
5. **No Limit:** Users can add unlimited books to wishlist
6. **Privacy:** Wishlists are private (not shared publicly)

---

## 📡 API Endpoints

### Endpoint Summary Table

| Method | Endpoint | Description | Auth | Status |
|--------|----------|-------------|------|--------|
| GET | `/api/wishlist` | Get user's wishlist | Required | ❌ Not Implemented |
| POST | `/api/wishlist` | Add book to wishlist | Required | ❌ Not Implemented |
| DELETE | `/api/wishlist/{bookId}` | Remove from wishlist | Required | ❌ Not Implemented |
| GET | `/api/wishlist/check/{bookId}` | Check if in wishlist | Required | ❌ Not Implemented |
| GET | `/api/wishlist/count` | Get wishlist item count | Required | ❌ Not Implemented |

---

## 🔧 Implementation Details

### 1. Get User's Wishlist
**GET** `/api/wishlist`

**Query Parameters:**
- `page` (default: 0)
- `size` (default: 20)

**Response:** `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "book": {
        "id": 456,
        "title": "Clean Code",
        "author": "Robert C. Martin",
        "coverImage": "https://cdn.example.com/books/clean-code.jpg",
        "price": 45.99,
        "condition": "NEW",
        "rating": 4.5,
        "totalReviews": 230,
        "availability": "IN_STOCK"
      },
      "addedAt": "2026-01-01T10:00:00Z"
    }
  ],
  "meta": {
    "page": 0,
    "totalPages": 3,
    "totalItems": 52,
    "itemsPerPage": 20
  }
}
```

**Notes:**
- Returns full book details for easy display
- Sorted by addedAt DESC (most recently added first)
- Includes availability and pricing for quick purchase decisions

---

### 2. Add to Wishlist
**POST** `/api/wishlist`

**Request Body:**
```json
{
  "bookId": 456
}
```

**Response:** `201 Created`
```json
{
  "success": true,
  "data": {
    "id": 1,
    "bookId": 456,
    "addedAt": "2026-01-01T10:00:00Z",
    "message": "Book added to wishlist"
  }
}
```

**Error Cases:**
- `400 Bad Request`: Invalid bookId
- `404 Not Found`: Book not found
- `409 Conflict`: Book already in wishlist (return 200 with existing entry instead)

**Idempotent Behavior:**
If book already in wishlist, return `200 OK` with existing entry instead of error.

---

### 3. Remove from Wishlist
**DELETE** `/api/wishlist/{bookId}`

**Response:** `204 No Content`

**Notes:**
- Idempotent: Returns 204 even if book wasn't in wishlist
- Uses bookId in path (not wishlist item id) for simpler FE logic

---

### 4. Check if Book in Wishlist
**GET** `/api/wishlist/check/{bookId}`

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "inWishlist": true,
    "wishlistItemId": 1,
    "addedAt": "2026-01-01T10:00:00Z"
  }
}
```

**Use Case:**
- Update heart icon state on book cards
- Determine button text (Add/Remove from wishlist)

---

### 5. Get Wishlist Count
**GET** `/api/wishlist/count`

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "count": 52
  }
}
```

**Use Case:** Display count badge in header navigation

---

## 🗄️ Database Schema

### Wishlist Entity

```java
@Entity
@Table(
    name = "wishlist",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "book_id"})
)
public class Wishlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private BookMeta book;
    
    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt;
    
    @PrePersist
    protected void onCreate() {
        addedAt = LocalDateTime.now();
    }
}
```

### Database Migration

```sql
-- Create wishlist table
CREATE TABLE wishlist (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    book_id BIGINT NOT NULL REFERENCES book_meta(id) ON DELETE CASCADE,
    added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_user_book_wishlist UNIQUE (user_id, book_id)
);

-- Create indexes for performance
CREATE INDEX idx_wishlist_user_id ON wishlist(user_id);
CREATE INDEX idx_wishlist_book_id ON wishlist(book_id);
CREATE INDEX idx_wishlist_added_at ON wishlist(added_at DESC);

-- Index for count queries
CREATE INDEX idx_wishlist_user_count ON wishlist(user_id, id);
```

---

## 📦 DTOs

### WishlistItemDto
```java
public class WishlistItemDto {
    private Long id;
    private BookDto book; // Nested book details
    private LocalDateTime addedAt;
}
```

### AddToWishlistRequest
```java
public class AddToWishlistRequest {
    @NotNull(message = "Book ID is required")
    @Positive
    private Long bookId;
}
```

### WishlistCheckDto
```java
public class WishlistCheckDto {
    private Boolean inWishlist;
    private Long wishlistItemId;
    private LocalDateTime addedAt;
}
```

### WishlistCountDto
```java
public class WishlistCountDto {
    private Integer count;
}
```

---

## 🔧 Service Layer

### WishlistService

```java
@Service
@RequiredArgsConstructor
public class WishlistService {
    private final WishlistRepository wishlistRepository;
    private final BookRepository bookRepository;
    private final WishlistMapper wishlistMapper;
    
    public WishlistItemDto addToWishlist(Long userId, Long bookId) {
        // Check if book exists
        BookMeta book = bookRepository.findById(bookId)
            .orElseThrow(() -> new ResourceNotFoundException("Book not found"));
        
        // Check if already in wishlist (idempotent)
        Optional<Wishlist> existing = wishlistRepository
            .findByUserIdAndBookId(userId, bookId);
        
        if (existing.isPresent()) {
            return wishlistMapper.toDto(existing.get());
        }
        
        // Create new wishlist entry
        Wishlist wishlist = new Wishlist();
        wishlist.setUser(userRepository.findById(userId).orElseThrow());
        wishlist.setBook(book);
        
        Wishlist saved = wishlistRepository.save(wishlist);
        return wishlistMapper.toDto(saved);
    }
    
    public Page<WishlistItemDto> getUserWishlist(Long userId, Pageable pageable) {
        Page<Wishlist> wishlistItems = wishlistRepository
            .findByUserIdOrderByAddedAtDesc(userId, pageable);
        return wishlistItems.map(wishlistMapper::toDto);
    }
    
    public void removeFromWishlist(Long userId, Long bookId) {
        wishlistRepository.deleteByUserIdAndBookId(userId, bookId);
        // Note: deleteBy is idempotent, no error if not exists
    }
    
    public WishlistCheckDto checkIfInWishlist(Long userId, Long bookId) {
        Optional<Wishlist> wishlist = wishlistRepository
            .findByUserIdAndBookId(userId, bookId);
        
        if (wishlist.isPresent()) {
            Wishlist item = wishlist.get();
            return new WishlistCheckDto(true, item.getId(), item.getAddedAt());
        }
        
        return new WishlistCheckDto(false, null, null);
    }
    
    public Integer getWishlistCount(Long userId) {
        return wishlistRepository.countByUserId(userId);
    }
}
```

---

## 🧪 Testing Requirements

### Unit Tests (WishlistService)

```java
@Test
void addToWishlist_NewBook_Success() {
    // Given: Valid userId and bookId
    // When: addToWishlist() is called
    // Then: Wishlist entry is created
}

@Test
void addToWishlist_AlreadyExists_ReturnsExisting() {
    // Given: Book already in wishlist
    // When: addToWishlist() is called again
    // Then: Return existing entry (idempotent)
}

@Test
void addToWishlist_BookNotFound_ThrowsException() {
    // Given: Invalid bookId
    // When: addToWishlist() is called
    // Then: Throw ResourceNotFoundException
}

@Test
void getUserWishlist_ReturnsPagedResults() {
    // Given: User with 25 wishlist items
    // When: getUserWishlist(page=0, size=10)
    // Then: Return first 10 items, sorted by addedAt DESC
}

@Test
void removeFromWishlist_Exists_Success() {
    // Given: Book in wishlist
    // When: removeFromWishlist() is called
    // Then: Entry is deleted
}

@Test
void removeFromWishlist_NotExists_NoError() {
    // Given: Book not in wishlist
    // When: removeFromWishlist() is called
    // Then: No error (idempotent)
}

@Test
void checkIfInWishlist_Exists_ReturnsTrue() {
    // Given: Book in wishlist
    // When: checkIfInWishlist() is called
    // Then: Return { inWishlist: true, wishlistItemId: X }
}

@Test
void checkIfInWishlist_NotExists_ReturnsFalse() {
    // Given: Book not in wishlist
    // When: checkIfInWishlist() is called
    // Then: Return { inWishlist: false, wishlistItemId: null }
}

@Test
void getWishlistCount_ReturnsCorrectCount() {
    // Given: User with 15 wishlist items
    // When: getWishlistCount() is called
    // Then: Return 15
}
```

### Integration Tests (WishlistController)

```java
@Test
@WithMockUser(userId = "123")
void addToWishlist_ValidRequest_Returns201() throws Exception {
    mockMvc.perform(post("/api/wishlist")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"bookId\": 456}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.bookId").value(456));
}

@Test
@WithMockUser(userId = "123")
void getUserWishlist_WithPagination_Returns200() throws Exception {
    mockMvc.perform(get("/api/wishlist")
        .param("page", "0")
        .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.meta.page").value(0));
}

@Test
@WithMockUser(userId = "123")
void removeFromWishlist_Returns204() throws Exception {
    mockMvc.perform(delete("/api/wishlist/456"))
        .andExpect(status().isNoContent());
}

@Test
@WithMockUser(userId = "123")
void checkIfInWishlist_Returns200() throws Exception {
    mockMvc.perform(get("/api/wishlist/check/456"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.inWishlist").exists());
}

@Test
void addToWishlist_Unauthorized_Returns401() throws Exception {
    mockMvc.perform(post("/api/wishlist")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"bookId\": 456}"))
        .andExpect(status().isUnauthorized());
}
```

---

## 🔗 Dependencies

### Related Modules
- **Module 01:** Authentication (requires authenticated user)
- **Module 03:** Books Catalog (depends on BookMeta entity)
- Frontend: All book browsing pages (heart icon functionality)

### External Services
- None (purely internal database operations)

---

## ✅ Acceptance Criteria

- [ ] Users can add books to wishlist
- [ ] System prevents duplicate wishlist entries (unique constraint enforced)
- [ ] Users can view their complete wishlist with pagination
- [ ] Wishlist displays full book details (title, author, price, cover)
- [ ] Users can remove books from wishlist
- [ ] Remove operation is idempotent (no error if book not in wishlist)
- [ ] Users can check if specific book is in their wishlist
- [ ] Wishlist count is available for header badge
- [ ] Wishlist persists across sessions
- [ ] All endpoints return standardized response format
- [ ] Database unique constraint prevents duplicates
- [ ] Unit test coverage ≥ 80%
- [ ] Integration tests pass for all endpoints
- [ ] Performance: GET /api/wishlist returns in < 200ms for 100 items

---

## 📅 Timeline

| Task | Estimated Time | Assignee | Status |
|------|----------------|----------|--------|
| Create Wishlist entity | 1 hour | - | ❌ |
| Create WishlistRepository | 1 hour | - | ❌ |
| Implement WishlistService | 3 hours | - | ❌ |
| Create WishlistController | 2 hours | - | ❌ |
| Create DTOs and Mappers | 2 hours | - | ❌ |
| Database migration | 1 hour | - | ❌ |
| Write unit tests | 3 hours | - | ❌ |
| Write integration tests | 2 hours | - | ❌ |
| Documentation | 30 min | - | ❌ |
| **Total** | **15.5 hours (2 days)** | | |

**Start Date:** Sprint 1, Week 1  
**Target Completion:** Sprint 1, Week 1  
**Blockers:** None

---

## 📝 Notes

- Consider adding wishlist sharing feature (future)
- Consider adding "move to cart" bulk operation (future)
- Consider adding price drop notifications for wishlisted items (future)
- Monitor database size; add cleanup for inactive users (future)
- Consider caching wishlist count for performance
- Frontend should optimistically update UI before API response

---

## ✔️ Sign-off

**Developer:** _________________ Date: _______  
**Reviewer:** _________________ Date: _______  
**QA:** _________________ Date: _______