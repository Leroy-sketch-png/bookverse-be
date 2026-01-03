# Module 03: Books Catalog

**Status:** ⚠️ Needs Refinement (20% Complete)  
**Priority:** 🔴 CRITICAL  
**Sprint:** Sprint 1 (Week 1)  
**Assigned To:** _[To be assigned]_  
**Estimated Effort:** 3 days

---

## 📋 Overview

Core book catalog management including browsing, filtering, searching, and curated feeds. This is the primary discovery mechanism for the marketplace.

**Key Features:**
- List all books with advanced filtering
- Get individual book details
- Search books by multiple criteria
- Curated feeds (trending, popular, new releases, recommended)
- Sort by various attributes
- Category and author filtering

---

## 🎯 Business Rules

1. **Public Access:** All book browsing endpoints are public (no auth required)
2. **Pagination:** Default 20 items per page, max 100
3. **Filtering:** Multiple filters can be applied simultaneously (AND logic)
4. **Price Range:** Supports min/max price filters
5. **Ratings:** Only show books with verified reviews
6. **Availability:** Show all books, indicate stock status

---

## 📡 API Endpoints

### Endpoint Summary Table

| Method | Endpoint | Description | Auth | Status |
|--------|----------|-------------|------|--------|
| GET | `/api/books` | List books with filters | Public | ⚠️ Needs Update |
| GET | `/api/books/{id}` | Get book details | Public | ⚠️ Needs Update |
| GET | `/api/books/trending` | Trending books | Public | ❌ Not Implemented |
| GET | `/api/books/popular` | Popular books | Public | ❌ Not Implemented |
| GET | `/api/books/recommended` | Personalized recommendations | Optional Auth | ❌ Not Implemented |
| GET | `/api/books/new-releases` | Recently added books | Public | ❌ Not Implemented |
| GET | `/api/books/search` | Full-text search | Public | ⚠️ Basic Only |
| GET | `/api/books/search/suggestions` | Autocomplete | Public | ❌ Not Implemented |
| POST | `/api/books` | Create book (Admin) | Admin | ✅ Complete |
| PUT | `/api/books/{id}` | Update book (Admin) | Admin | ✅ Complete |

---

## 🔧 Implementation Details

### 1. List Books ⚠️ NEEDS MAJOR UPDATE
**GET** `/api/books`

**Current Issues:**
- ❌ Pagination format doesn't match FE expectations
- ❌ Missing filter query parameters
- ❌ Missing sort options
- ❌ Doesn't include rating/review data

**Required Query Parameters:**
```
?page=0
&size=20
&categoryId=5
&authorId=10
&condition=NEW,LIKE_NEW
&priceMin=10.00
&priceMax=50.00
&rating=4
&sortBy=price|rating|createdAt|title
&sortOrder=asc|desc
&availability=IN_STOCK,LOW_STOCK
```

**Current Response Format:**
```json
{
  "content": [],
  "totalElements": 100,
  "totalPages": 5
}
```

**Required Response Format:**
```json
{
  "success": true,
  "data": [
    {
      "id": 123,
      "isbn": "978-0132350884",
      "title": "Clean Code",
      "subtitle": "A Handbook of Agile Software Craftsmanship",
      "author": {
        "id": 10,
        "name": "Robert C. Martin",
        "bio": "Software engineer and author"
      },
      "category": {
        "id": 5,
        "name": "Software Engineering",
        "slug": "software-engineering"
      },
      "publisher": "Prentice Hall",
      "publishedDate": "2008-08-01",
      "language": "English",
      "pages": 464,
      "description": "Even bad code can function. But if code isn't clean...",
      "coverImage": "https://cdn.example.com/books/clean-code.jpg",
      "price": 45.99,
      "condition": "NEW",
      "stockQuantity": 50,
      "availability": "IN_STOCK",
      "averageRating": 4.7,
      "totalReviews": 1250,
      "createdAt": "2025-12-01T10:00:00Z"
    }
  ],
  "meta": {
    "page": 0,
    "totalPages": 5,
    "totalItems": 95,
    "itemsPerPage": 20
  }
}
```

**Required Backend Changes:**

```java
@GetMapping
public ResponseEntity<PagedResponse<BookDto>> getBooks(
    @RequestParam(required = false) Long categoryId,
    @RequestParam(required = false) Long authorId,
    @RequestParam(required = false) List<BookCondition> condition,
    @RequestParam(required = false) BigDecimal priceMin,
    @RequestParam(required = false) BigDecimal priceMax,
    @RequestParam(required = false) Integer rating, // Minimum rating
    @RequestParam(required = false) List<String> availability,
    @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
    @RequestParam(required = false, defaultValue = "desc") String sortOrder,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size
) {
    // Build specification with all filters
    Specification<BookMeta> spec = Specification.where(null);
    
    if (categoryId != null) {
        spec = spec.and(BookSpecification.hasCategory(categoryId));
    }
    if (authorId != null) {
        spec = spec.and(BookSpecification.hasAuthor(authorId));
    }
    if (condition != null && !condition.isEmpty()) {
        spec = spec.and(BookSpecification.hasCondition(condition));
    }
    if (priceMin != null || priceMax != null) {
        spec = spec.and(BookSpecification.priceRange(priceMin, priceMax));
    }
    if (rating != null) {
        spec = spec.and(BookSpecification.minRating(rating));
    }
    
    // Sort
    Sort sort = sortOrder.equalsIgnoreCase("asc") 
        ? Sort.by(sortBy).ascending() 
        : Sort.by(sortBy).descending();
    
    Pageable pageable = PageRequest.of(page, size, sort);
    
    Page<BookMeta> bookPage = bookRepository.findAll(spec, pageable);
    
    return ResponseEntity.ok(PagedResponse.of(bookPage, bookMapper::toDto));
}
```

**Create BookSpecification.java:**
```java
public class BookSpecification {
    public static Specification<BookMeta> hasCategory(Long categoryId) {
        return (root, query, cb) -> 
            cb.equal(root.get("category").get("id"), categoryId);
    }
    
    public static Specification<BookMeta> hasAuthor(Long authorId) {
        return (root, query, cb) -> 
            cb.equal(root.get("author").get("id"), authorId);
    }
    
    public static Specification<BookMeta> hasCondition(List<BookCondition> conditions) {
        return (root, query, cb) -> 
            root.get("condition").in(conditions);
    }
    
    public static Specification<BookMeta> priceRange(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            if (min != null && max != null) {
                return cb.between(root.get("price"), min, max);
            } else if (min != null) {
                return cb.greaterThanOrEqualTo(root.get("price"), min);
            } else if (max != null) {
                return cb.lessThanOrEqualTo(root.get("price"), max);
            }
            return null;
        };
    }
    
    public static Specification<BookMeta> minRating(Integer minRating) {
        return (root, query, cb) -> {
            // Join with reviews table to get average rating
            Subquery<Double> subquery = query.subquery(Double.class);
            Root<Review> reviewRoot = subquery.from(Review.class);
            subquery.select(cb.avg(reviewRoot.get("rating")));
            subquery.where(cb.equal(reviewRoot.get("book"), root));
            
            return cb.greaterThanOrEqualTo(subquery, minRating.doubleValue());
        };
    }
}
```

---

### 2. Get Book Details ⚠️ NEEDS UPDATE
**GET** `/api/books/{id}`

**Current Issues:**
- ❌ Missing averageRating and totalReviews
- ❌ Missing seller information
- ❌ Missing related books

**Required Response:**
```json
{
  "success": true,
  "data": {
    "id": 123,
    "isbn": "978-0132350884",
    "title": "Clean Code",
    "subtitle": "A Handbook of Agile Software Craftsmanship",
    "author": {
      "id": 10,
      "name": "Robert C. Martin",
      "bio": "Software engineer...",
      "totalBooks": 8
    },
    "category": {
      "id": 5,
      "name": "Software Engineering",
      "description": "Books about software development"
    },
    "publisher": "Prentice Hall",
    "publishedDate": "2008-08-01",
    "language": "English",
    "pages": 464,
    "description": "Full description...",
    "coverImage": "https://cdn.example.com/books/clean-code.jpg",
    "price": 45.99,
    "condition": "NEW",
    "stockQuantity": 50,
    "availability": "IN_STOCK",
    "dimensions": {
      "width": 7.5,
      "height": 9.2,
      "depth": 1.1,
      "unit": "inches"
    },
    "weight": {
      "value": 1.8,
      "unit": "pounds"
    },
    "averageRating": 4.7,
    "totalReviews": 1250,
    "ratingDistribution": {
      "5": 850,
      "4": 280,
      "3": 85,
      "2": 25,
      "1": 10
    },
    "seller": {
      "id": 50,
      "username": "bookstore_pro",
      "businessName": "Pro Book Store",
      "rating": 4.8,
      "totalSales": 5000,
      "isProSeller": true
    },
    "relatedBooks": [
      {
        "id": 124,
        "title": "The Pragmatic Programmer",
        "coverImage": "...",
        "price": 42.99
      }
    ],
    "createdAt": "2025-12-01T10:00:00Z",
    "updatedAt": "2025-12-15T14:30:00Z"
  }
}
```

**Required Changes:**
- Add rating calculation from reviews
- Include seller information
- Add related books (same category/author)
- Add rating distribution

---

### 3. Trending Books ❌ NEW ENDPOINT
**GET** `/api/books/trending`

**Logic:** Books with most views/sales in last 7 days

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 123,
      "title": "Clean Code",
      "author": "Robert C. Martin",
      "coverImage": "...",
      "price": 45.99,
      "averageRating": 4.7,
      "trendingScore": 1250
    }
  ]
}
```

**Implementation:**
```java
@GetMapping("/trending")
public ResponseEntity<List<BookDto>> getTrendingBooks() {
    LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
    
    // Query books with most activity in last 7 days
    // Activity = view count + order count * 10
    List<BookMeta> trending = bookRepository.findTrendingBooks(
        weekAgo, 
        PageRequest.of(0, 20)
    );
    
    return ResponseEntity.ok(bookMapper.toDtoList(trending));
}
```

**Required Repository Method:**
```java
@Query("""
    SELECT b FROM BookMeta b
    LEFT JOIN Listing l ON l.book = b
    LEFT JOIN OrderItem oi ON oi.listing = l
    WHERE l.createdAt > :since OR oi.createdAt > :since
    GROUP BY b.id
    ORDER BY (COUNT(DISTINCT l.viewCount) + COUNT(DISTINCT oi.id) * 10) DESC
""")
List<BookMeta> findTrendingBooks(LocalDateTime since, Pageable pageable);
```

---

### 4. Popular Books ❌ NEW ENDPOINT
**GET** `/api/books/popular`

**Logic:** Books with highest average rating and minimum 10 reviews

**Implementation:**
```java
@GetMapping("/popular")
public ResponseEntity<List<BookDto>> getPopularBooks() {
    List<BookMeta> popular = bookRepository.findPopularBooks(
        PageRequest.of(0, 20)
    );
    return ResponseEntity.ok(bookMapper.toDtoList(popular));
}
```

**Repository Method:**
```java
@Query("""
    SELECT b FROM BookMeta b
    LEFT JOIN Review r ON r.book = b
    GROUP BY b.id
    HAVING COUNT(r.id) >= 10
    ORDER BY AVG(r.rating) DESC, COUNT(r.id) DESC
""")
List<BookMeta> findPopularBooks(Pageable pageable);
```

---

### 5. Recommended Books ❌ NEW ENDPOINT
**GET** `/api/books/recommended`

**Logic:**
- For authenticated users: Based on order history and browsing patterns
- For guests: Popular books from various categories

**Implementation:**
```java
@GetMapping("/recommended")
public ResponseEntity<List<BookDto>> getRecommendedBooks(
    @AuthenticationPrincipal User currentUser
) {
    List<BookMeta> recommended;
    
    if (currentUser != null) {
        // Personalized recommendations
        recommended = recommendationService
            .getPersonalizedRecommendations(currentUser.getId());
    } else {
        // Generic popular books
        recommended = bookRepository.findPopularBooks(PageRequest.of(0, 20));
    }
    
    return ResponseEntity.ok(bookMapper.toDtoList(recommended));
}
```

---

### 6. New Releases ❌ NEW ENDPOINT
**GET** `/api/books/new-releases`

**Logic:** Books added in last 30 days, sorted by creation date DESC

**Implementation:**
```java
@GetMapping("/new-releases")
public ResponseEntity<List<BookDto>> getNewReleases() {
    LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
    
    List<BookMeta> newBooks = bookRepository.findByCreatedAtAfter(
        thirtyDaysAgo,
        PageRequest.of(0, 20, Sort.by("createdAt").descending())
    );
    
    return ResponseEntity.ok(bookMapper.toDtoList(newBooks));
}
```

---

### 7. Search Books ⚠️ NEEDS ENHANCEMENT
**GET** `/api/books/search`

**Current:** Basic title search only

**Required:** Full-text search across title, author, ISBN, publisher, description

**Query Parameters:**
```
?q=clean code
&page=0
&size=20
```

**Implementation with PostgreSQL Full-Text Search:**
```java
@Query("""
    SELECT b FROM BookMeta b
    LEFT JOIN b.author a
    WHERE 
        LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%'))
        OR LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%'))
        OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', :query, '%'))
        OR LOWER(b.publisher) LIKE LOWER(CONCAT('%', :query, '%'))
        OR LOWER(b.description) LIKE LOWER(CONCAT('%', :query, '%'))
""")
Page<BookMeta> searchBooks(String query, Pageable pageable);
```

**For Better Performance (Future):**
Consider PostgreSQL `tsvector` or Elasticsearch integration

---

### 8. Search Suggestions ❌ NEW ENDPOINT
**GET** `/api/books/search/suggestions`

**Query Parameters:**
```
?q=cle
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 123,
      "title": "Clean Code",
      "author": "Robert C. Martin",
      "coverImage": "...",
      "matchType": "title"
    },
    {
      "id": 124,
      "title": "Clean Architecture",
      "author": "Robert C. Martin",
      "coverImage": "...",
      "matchType": "title"
    }
  ]
}
```

**Implementation:**
```java
@GetMapping("/search/suggestions")
public ResponseEntity<List<SearchSuggestionDto>> getSearchSuggestions(
    @RequestParam String q
) {
    if (q.length() < 2) {
        return ResponseEntity.ok(List.of());
    }
    
    List<SearchSuggestionDto> suggestions = bookService
        .getSearchSuggestions(q, 10);
    
    return ResponseEntity.ok(suggestions);
}
```

---

## 🗄️ Database Schema

### BookMeta Entity (Already Exists, Update Required)

```java
@Entity
@Table(name = "book_meta")
public class BookMeta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String isbn;
    
    private String title;
    private String subtitle;
    
    @ManyToOne
    @JoinColumn(name = "author_id")
    private Author author;
    
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
    
    private String publisher;
    private LocalDate publishedDate;
    private String language;
    private Integer pages;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private String coverImage;
    private BigDecimal price;
    
    @Enumerated(EnumType.STRING)
    private BookCondition condition;
    
    private Integer stockQuantity;
    
    // ADD THESE FIELDS FOR CACHING (Optional - for performance)
    @Column(name = "average_rating")
    private Double averageRating;
    
    @Column(name = "total_reviews")
    private Integer totalReviews;
    
    @Column(name = "view_count")
    private Integer viewCount = 0;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### Indexes for Performance

```sql
-- Search optimization
CREATE INDEX idx_book_meta_title_gin ON book_meta USING gin(to_tsvector('english', title));
CREATE INDEX idx_book_meta_title_lower ON book_meta(LOWER(title));
CREATE INDEX idx_book_meta_isbn ON book_meta(isbn);

-- Filter optimization
CREATE INDEX idx_book_meta_category_id ON book_meta(category_id);
CREATE INDEX idx_book_meta_author_id ON book_meta(author_id);
CREATE INDEX idx_book_meta_price ON book_meta(price);
CREATE INDEX idx_book_meta_condition ON book_meta(condition);
CREATE INDEX idx_book_meta_created_at ON book_meta(created_at DESC);

-- Rating optimization (if caching in book_meta)
CREATE INDEX idx_book_meta_rating ON book_meta(average_rating DESC);
```

---

## 📦 DTOs

### BookDto (UPDATE REQUIRED)
```java
public class BookDto {
    private Long id;
    private String isbn;
    private String title;
    private String subtitle;
    private AuthorDto author;
    private CategoryDto category;
    private String publisher;
    private LocalDate publishedDate;
    private String language;
    private Integer pages;
    private String description;
    private String coverImage;
    private BigDecimal price;
    private BookCondition condition;
    private Integer stockQuantity;
    private String availability; // IN_STOCK, LOW_STOCK, OUT_OF_STOCK
    
    // NEW FIELDS
    private Double averageRating;
    private Integer totalReviews;
    private RatingDistributionDto ratingDistribution;
    private SellerSummaryDto seller;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### SearchSuggestionDto (NEW)
```java
public class SearchSuggestionDto {
    private Long id;
    private String title;
    private String author;
    private String coverImage;
    private String matchType; // "title", "author", "isbn"
}
```

---

## 🧪 Testing Requirements

### Unit Tests

```java
@Test
void getBooks_WithFilters_ReturnsFilteredResults() {
    // Test all filter combinations
}

@Test
void getBooks_WithPriceRange_ReturnsCorrectBooks() {
    // Test price filtering
}

@Test
void getBookById_IncludesRatingData() {
    // Verify rating data is included
}

@Test
void getTrendingBooks_ReturnsRecentPopular() {
    // Test trending calculation
}

@Test
void search_FindsAcrossMul tipleFields() {
    // Test search across title, author, ISBN
}
```

### Integration Tests

```java
@Test
void getBooks_WithPagination_ReturnsCorrectFormat() throws Exception {
    mockMvc.perform(get("/api/books")
        .param("page", "0")
        .param("size", "20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.meta.page").value(0));
}

@Test
void getBooks_WithCategoryFilter_ReturnsOnlyCategory() throws Exception {
    mockMvc.perform(get("/api/books")
        .param("categoryId", "5"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[*].category.id").value(everyItem(is(5))));
}
```

---

## 🔗 Dependencies

### Related Modules
- **Module 09:** Reviews (for rating data)
- **Module 04:** Listings (for availability)
- **Module 16:** Categories
- **Module 16:** Authors

---

## ✅ Acceptance Criteria

- [ ] Books list endpoint supports all required filters
- [ ] Pagination format matches FE expectations
- [ ] Sort options work correctly
- [ ] Book details include rating and seller info
- [ ] Trending books endpoint returns last 7 days activity
- [ ] Popular books endpoint returns highest rated
- [ ] New releases endpoint returns recent additions
- [ ] Search works across multiple fields
- [ ] Search suggestions provide autocomplete
- [ ] Performance: List queries < 500ms
- [ ] All endpoints return standardized format

---

## 📅 Timeline

| Task | Estimated Time | Status |
|------|----------------|--------|
| Update list endpoint with filters | 4 hours | ❌ |
| Update book details response | 2 hours | ❌ |
| Implement trending endpoint | 3 hours | ❌ |
| Implement popular endpoint | 2 hours | ❌ |
| Implement new releases | 1 hour | ❌ |
| Implement recommended | 3 hours | ❌ |
| Enhance search | 3 hours | ❌ |
| Implement suggestions | 2 hours | ❌ |
| Tests | 4 hours | ❌ |
| **Total** | **24 hours (3 days)** | |

---

## ✔️ Sign-off

**Developer:** _________________ Date: _______  
**Reviewer:** _________________ Date: _______  
**QA:** _________________ Date: _______