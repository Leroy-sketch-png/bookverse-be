# Module 04: Listings Management

**Status:** ⚠️ Needs Refinement (50% Complete)  
**Priority:** 🟡 High  
**Sprint:** Sprint 1 (Week 2)  
**Assigned To:** _[To be assigned]_  
**Estimated Effort:** 2 days

---

## 📋 Overview

Listings are seller-created entries for books they want to sell. Each listing references a BookMeta and includes seller-specific details like condition, price, and stock.

**Key Features:**
- Create/update/delete listings
- Filter by seller
- Filter by status (ACTIVE/SOLD_OUT/DRAFT)
- Image upload for listing-specific photos
- View count tracking
- Stock management

---

## 🎯 Business Rules

1. **Authorization:** Only sellers can create listings
2. **Book Reference:** Listing must reference existing BookMeta
3. **Pricing:** Sellers set their own prices
4. **Stock Management:** Quantity decreases with each order
5. **Status Auto-Update:** Status changes to SOLD_OUT when quantity = 0
6. **View Tracking:** Increment view count on each detail view (exclude seller's own views)
7. **Images:** Sellers can upload listing-specific images

---

## 📡 API Endpoints

### Endpoint Summary Table

| Method | Endpoint | Description | Auth | Status |
|--------|----------|-------------|------|--------|
| GET | `/api/listings` | List all listings | Public | ⚠️ Needs Filters |
| GET | `/api/listings/{id}` | Get listing details | Public | ⚠️ Needs View Count |
| POST | `/api/listings` | Create listing | Seller | ⚠️ Needs Image Upload Check |
| PUT | `/api/listings/{id}` | Update listing | Owner | ✅ Complete |
| DELETE | `/api/listings/{id}` | Delete listing | Owner | ✅ Complete |
| PATCH | `/api/listings/{id}/stock` | Update stock | Owner | ❌ Missing |

---

## 🔧 Implementation Details

### 1. List Listings ⚠️ NEEDS UPDATE
**GET** `/api/listings`

**Current Issues:**
- ❌ No seller filtering
- ❌ No status filtering
- ❌ Pagination format may not match FE

**Required Query Parameters:**
```
?sellerId=123
&status=ACTIVE|SOLD_OUT|DRAFT
&bookId=456
&page=0
&size=20
&sortBy=createdAt|price|viewCount
&sortOrder=asc|desc
```

**Required Response Format:**
```json
{
  "success": true,
  "data": [
    {
      "id": 301,
      "book": {
        "id": 123,
        "title": "Clean Code",
        "author": "Robert C. Martin",
        "isbn": "978-0132350884",
        "coverImage": "https://cdn.example.com/books/clean-code.jpg"
      },
      "seller": {
        "id": 50,
        "username": "bookstore_pro",
        "businessName": "Pro Book Store",
        "rating": 4.8,
        "isProSeller": true
      },
      "condition": "NEW",
      "price": 45.99,
      "originalPrice": 55.99,
      "stockQuantity": 15,
      "status": "ACTIVE",
      "description": "Brand new, sealed copy",
      "images": [
        "https://cdn.example.com/listings/301-1.jpg",
        "https://cdn.example.com/listings/301-2.jpg"
      ],
      "viewCount": 1250,
      "soldCount": 45,
      "createdAt": "2025-12-01T10:00:00Z",
      "updatedAt": "2025-12-15T14:30:00Z"
    }
  ],
  "meta": {
    "page": 0,
    "totalPages": 8,
    "totalItems": 152,
    "itemsPerPage": 20
  }
}
```

**Required Backend Changes:**

```java
@GetMapping
public ResponseEntity<PagedResponse<ListingDto>> getListings(
    @RequestParam(required = false) Long sellerId,
    @RequestParam(required = false) Long bookId,
    @RequestParam(required = false) ListingStatus status,
    @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
    @RequestParam(required = false, defaultValue = "desc") String sortOrder,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size
) {
    Specification<Listing> spec = Specification.where(null);
    
    if (sellerId != null) {
        spec = spec.and(ListingSpecification.hasSeller(sellerId));
    }
    if (bookId != null) {
        spec = spec.and(ListingSpecification.hasBook(bookId));
    }
    if (status != null) {
        spec = spec.and(ListingSpecification.hasStatus(status));
    }
    
    Sort sort = sortOrder.equalsIgnoreCase("asc")
        ? Sort.by(sortBy).ascending()
        : Sort.by(sortBy).descending();
    
    Pageable pageable = PageRequest.of(page, size, sort);
    Page<Listing> listings = listingRepository.findAll(spec, pageable);
    
    return ResponseEntity.ok(PagedResponse.of(listings, listingMapper::toDto));
}
```

**Create ListingSpecification.java:**
```java
public class ListingSpecification {
    public static Specification<Listing> hasSeller(Long sellerId) {
        return (root, query, cb) ->
            cb.equal(root.get("seller").get("id"), sellerId);
    }
    
    public static Specification<Listing> hasBook(Long bookId) {
        return (root, query, cb) ->
            cb.equal(root.get("book").get("id"), bookId);
    }
    
    public static Specification<Listing> hasStatus(ListingStatus status) {
        return (root, query, cb) ->
            cb.equal(root.get("status"), status);
    }
}
```

---

### 2. Get Listing Details ⚠️ NEEDS UPDATE
**GET** `/api/listings/{id}`

**Current Issues:**
- ❌ Doesn't increment view count
- ❌ May be missing related listings

**Required Behavior:**
- Increment `viewCount` by 1 on each GET
- Exclude increment if viewer is the seller
- Include related listings (same book, different sellers)

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "id": 301,
    "book": {
      "id": 123,
      "title": "Clean Code",
      "subtitle": "A Handbook of Agile Software Craftsmanship",
      "author": {
        "id": 10,
        "name": "Robert C. Martin"
      },
      "category": {
        "id": 5,
        "name": "Software Engineering"
      },
      "isbn": "978-0132350884",
      "publisher": "Prentice Hall",
      "publishedDate": "2008-08-01",
      "pages": 464,
      "language": "English",
      "coverImage": "https://cdn.example.com/books/clean-code.jpg",
      "averageRating": 4.7,
      "totalReviews": 1250
    },
    "seller": {
      "id": 50,
      "username": "bookstore_pro",
      "businessName": "Pro Book Store",
      "avatar": "https://cdn.example.com/avatars/seller-50.jpg",
      "rating": 4.8,
      "totalSales": 5000,
      "totalReviews": 450,
      "isProSeller": true,
      "memberSince": "2024-01-01T00:00:00Z"
    },
    "condition": "NEW",
    "price": 45.99,
    "originalPrice": 55.99,
    "discount": 18,
    "stockQuantity": 15,
    "status": "ACTIVE",
    "description": "Brand new, sealed copy. Fast shipping available.",
    "images": [
      "https://cdn.example.com/listings/301-1.jpg",
      "https://cdn.example.com/listings/301-2.jpg"
    ],
    "shippingInfo": {
      "freeShipping": true,
      "estimatedDays": "2-3 business days",
      "shipsFrom": "Ho Chi Minh City, Vietnam"
    },
    "viewCount": 1251,
    "soldCount": 45,
    "createdAt": "2025-12-01T10:00:00Z",
    "updatedAt": "2025-12-15T14:30:00Z",
    "relatedListings": [
      {
        "id": 302,
        "seller": {
          "id": 51,
          "username": "another_seller"
        },
        "condition": "LIKE_NEW",
        "price": 42.99,
        "stockQuantity": 8
      }
    ]
  }
}
```

**Implementation:**
```java
@GetMapping("/{id}")
public ResponseEntity<ListingDetailDto> getListingById(
    @PathVariable Long id,
    @AuthenticationPrincipal User currentUser
) {
    Listing listing = listingRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));
    
    // Increment view count (exclude seller's own views)
    if (currentUser == null || !currentUser.getId().equals(listing.getSeller().getId())) {
        listingService.incrementViewCount(id);
    }
    
    ListingDetailDto dto = listingMapper.toDetailDto(listing);
    
    // Add related listings (same book, different sellers)
    List<ListingSummaryDto> related = listingService
        .getRelatedListings(listing.getBook().getId(), id);
    dto.setRelatedListings(related);
    
    return ResponseEntity.ok(ApiResponse.success(dto));
}
```

**Service Method:**
```java
@Transactional
public void incrementViewCount(Long listingId) {
    listingRepository.incrementViewCount(listingId);
}
```

**Repository Method:**
```java
@Modifying
@Query("UPDATE Listing l SET l.viewCount = l.viewCount + 1 WHERE l.id = :id")
void incrementViewCount(@Param("id") Long id);
```

---

### 3. Create Listing ⚠️ VERIFY IMAGE UPLOAD
**POST** `/api/listings`

**Authorization:** `@PreAuthorize("hasRole('SELLER')")`

**Request:** `multipart/form-data` or JSON

**Option 1: Multipart (with images)**
```
Content-Type: multipart/form-data

bookId: 123
condition: NEW
price: 45.99
originalPrice: 55.99
stockQuantity: 15
description: "Brand new, sealed copy"
status: ACTIVE
images: [file1, file2, file3]
```

**Option 2: JSON (images uploaded separately)**
```json
{
  "bookId": 123,
  "condition": "NEW",
  "price": 45.99,
  "originalPrice": 55.99,
  "stockQuantity": 15,
  "description": "Brand new, sealed copy",
  "status": "ACTIVE",
  "imageUrls": [
    "https://cdn.example.com/temp/img1.jpg",
    "https://cdn.example.com/temp/img2.jpg"
  ]
}
```

**Response:** `201 Created`
```json
{
  "success": true,
  "data": {
    "id": 301,
    "book": { ... },
    "seller": { ... },
    "condition": "NEW",
    "price": 45.99,
    "stockQuantity": 15,
    "status": "ACTIVE",
    "images": [
      "https://cdn.example.com/listings/301-1.jpg",
      "https://cdn.example.com/listings/301-2.jpg"
    ],
    "createdAt": "2026-01-01T10:00:00Z"
  },
  "message": "Listing created successfully"
}
```

**Validation:**
- `bookId`: Required, must exist
- `condition`: Required, one of: NEW, LIKE_NEW, GOOD, ACCEPTABLE
- `price`: Required, must be > 0
- `stockQuantity`: Required, must be >= 0
- `description`: Optional, max 2000 chars
- `images`: Max 5 images, each max 5MB

**Implementation:**
```java
@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<ApiResponse<ListingDto>> createListing(
    @RequestPart("data") CreateListingRequest request,
    @RequestPart(value = "images", required = false) List<MultipartFile> images,
    @AuthenticationPrincipal User currentUser
) {
    // Verify user is seller
    if (!currentUser.getRoles().stream()
            .anyMatch(role -> role.getName().equals("ROLE_SELLER"))) {
        throw new ForbiddenException("Only sellers can create listings");
    }
    
    // Upload images
    List<String> imageUrls = new ArrayList<>();
    if (images != null && !images.isEmpty()) {
        imageUrls = imageUploadService.uploadListingImages(images);
    }
    
    ListingDto listing = listingService.createListing(
        currentUser.getId(),
        request,
        imageUrls
    );
    
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(listing, "Listing created successfully"));
}
```

---

### 4. Update Listing
**PUT** `/api/listings/{id}`

**Authorization:** Must be listing owner

**Request Body:** Same as Create (without bookId)

**Response:** `200 OK` (same structure as Get)

**Status:** ✅ Already Implemented

---

### 5. Delete Listing
**DELETE** `/api/listings/{id}`

**Authorization:** Must be listing owner

**Response:** `204 No Content`

**Business Logic:**
- Can only delete if no pending orders
- Can delete if status is DRAFT or SOLD_OUT
- For ACTIVE listings with stock, change to DRAFT instead of deleting

**Status:** ✅ Already Implemented

---

### 6. Update Stock ❌ NEW ENDPOINT
**PATCH** `/api/listings/{id}/stock`

**Authorization:** Must be listing owner

**Request Body:**
```json
{
  "quantity": 25,
  "operation": "SET"
}
```

or

```json
{
  "quantity": 5,
  "operation": "ADD"
}
```

or

```json
{
  "quantity": 3,
  "operation": "SUBTRACT"
}
```

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "listingId": 301,
    "oldQuantity": 15,
    "newQuantity": 25,
    "status": "ACTIVE"
  },
  "message": "Stock updated successfully"
}
```

**Implementation:**
```java
@PatchMapping("/{id}/stock")
public ResponseEntity<ApiResponse<StockUpdateDto>> updateStock(
    @PathVariable Long id,
    @RequestBody UpdateStockRequest request,
    @AuthenticationPrincipal User currentUser
) {
    StockUpdateDto result = listingService.updateStock(
        id,
        currentUser.getId(),
        request
    );
    
    return ResponseEntity.ok(ApiResponse.success(result));
}
```

---

## 🗄️ Database Schema

### Listing Entity (Already Exists, Verify Fields)

```java
@Entity
@Table(name = "listing")
public class Listing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private BookMeta book;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookCondition condition;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(name = "original_price", precision = 10, scale = 2)
    private BigDecimal originalPrice;
    
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ListingStatus status = ListingStatus.ACTIVE;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @ElementCollection
    @CollectionTable(name = "listing_images", joinColumns = @JoinColumn(name = "listing_id"))
    @Column(name = "image_url")
    private List<String> images = new ArrayList<>();
    
    @Column(name = "view_count")
    private Integer viewCount = 0;
    
    @Column(name = "sold_count")
    private Integer soldCount = 0;
    
    @Column(name = "free_shipping")
    private Boolean freeShipping = false;
    
    @Column(name = "estimated_shipping_days")
    private String estimatedShippingDays;
    
    @Column(name = "ships_from")
    private String shipsFrom;
    
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
        
        // Auto-update status based on stock
        if (stockQuantity <= 0 && status == ListingStatus.ACTIVE) {
            status = ListingStatus.SOLD_OUT;
        } else if (stockQuantity > 0 && status == ListingStatus.SOLD_OUT) {
            status = ListingStatus.ACTIVE;
        }
    }
}
```

### ListingStatus Enum

```java
public enum ListingStatus {
    DRAFT,      // Not yet published
    ACTIVE,     // Available for sale
    SOLD_OUT,   // No stock available
    INACTIVE    // Temporarily disabled by seller
}
```

### Database Indexes

```sql
-- Performance indexes
CREATE INDEX idx_listing_seller_id ON listing(seller_id);
CREATE INDEX idx_listing_book_id ON listing(book_id);
CREATE INDEX idx_listing_status ON listing(status);
CREATE INDEX idx_listing_price ON listing(price);
CREATE INDEX idx_listing_view_count ON listing(view_count DESC);
CREATE INDEX idx_listing_created_at ON listing(created_at DESC);

-- Composite indexes
CREATE INDEX idx_listing_seller_status ON listing(seller_id, status);
CREATE INDEX idx_listing_book_status ON listing(book_id, status);
```

---

## 📦 DTOs

### ListingDto (UPDATE)
```java
public class ListingDto {
    private Long id;
    private BookSummaryDto book;
    private SellerSummaryDto seller;
    private BookCondition condition;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer discount; // Calculated: (originalPrice - price) / originalPrice * 100
    private Integer stockQuantity;
    private ListingStatus status;
    private String description;
    private List<String> images;
    private Integer viewCount;
    private Integer soldCount;
    private ShippingInfoDto shippingInfo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### CreateListingRequest
```java
public class CreateListingRequest {
    @NotNull
    private Long bookId;
    
    @NotNull
    private BookCondition condition;
    
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal price;
    
    @DecimalMin("0.01")
    private BigDecimal originalPrice;
    
    @NotNull
    @Min(0)
    private Integer stockQuantity;
    
    @Size(max = 2000)
    private String description;
    
    private ListingStatus status = ListingStatus.ACTIVE;
    
    private Boolean freeShipping;
    private String estimatedShippingDays;
    private String shipsFrom;
}
```

### UpdateStockRequest
```java
public class UpdateStockRequest {
    @NotNull
    @Min(0)
    private Integer quantity;
    
    @NotNull
    private StockOperation operation; // SET, ADD, SUBTRACT
}

public enum StockOperation {
    SET,       // Set stock to exact quantity
    ADD,       // Add to current stock
    SUBTRACT   // Subtract from current stock
}
```

### StockUpdateDto
```java
public class StockUpdateDto {
    private Long listingId;
    private Integer oldQuantity;
    private Integer newQuantity;
    private ListingStatus status;
}
```

---

## 🧪 Testing Requirements

### Unit Tests

```java
@Test
void getListings_WithSellerFilter_ReturnsOnlySellersListings() {
    // Given: Listings from multiple sellers
    // When: Filter by sellerId=50
    // Then: Only seller 50's listings returned
}

@Test
void getListings_WithStatusFilter_ReturnsCorrectStatus() {
    // Given: Listings with various statuses
    // When: Filter by status=ACTIVE
    // Then: Only ACTIVE listings returned
}

@Test
void getListingById_IncrementsViewCount() {
    // Given: Listing with viewCount=100
    // When: GET /api/listings/{id}
    // Then: viewCount becomes 101
}

@Test
void getListingById_SellerView_DoesNotIncrementViewCount() {
    // Given: Listing owned by user A, viewCount=100
    // When: User A views the listing
    // Then: viewCount remains 100
}

@Test
void createListing_NotSeller_ThrowsForbidden() {
    // Given: User without ROLE_SELLER
    // When: POST /api/listings
    // Then: Throw ForbiddenException
}

@Test
void createListing_WithImages_UploadsAndReturnsUrls() {
    // Given: Valid listing data with 3 images
    // When: POST /api/listings
    // Then: Images uploaded, URLs in response
}

@Test
void updateStock_SetOperation_SetsExactQuantity() {
    // Given: Listing with stock=15
    // When: Update with quantity=25, operation=SET
    // Then: Stock becomes 25
}

@Test
void updateStock_SubtractBelowZero_ThrowsException() {
    // Given: Listing with stock=5
    // When: Update with quantity=10, operation=SUBTRACT
    // Then: Throw BadRequestException
}

@Test
void updateListing_AutoUpdateStatus_SoldOutWhenStockZero() {
    // Given: Listing with status=ACTIVE, stock=1
    // When: Update stock to 0
    // Then: Status automatically changes to SOLD_OUT
}
```

### Integration Tests

```java
@Test
@WithMockUser(roles = "SELLER")
void getListings_WithFilters_ReturnsFilteredResults() throws Exception {
    mockMvc.perform(get("/api/listings")
        .param("sellerId", "50")
        .param("status", "ACTIVE"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data[*].seller.id").value(everyItem(is(50))))
        .andExpect(jsonPath("$.data[*].status").value(everyItem(is("ACTIVE"))));
}

@Test
@WithMockUser(roles = "SELLER")
void createListing_ValidData_Returns201() throws Exception {
    MockMultipartFile data = new MockMultipartFile(
        "data",
        "",
        "application/json",
        objectMapper.writeValueAsBytes(validRequest)
    );
    
    MockMultipartFile image = new MockMultipartFile(
        "images",
        "test.jpg",
        "image/jpeg",
        "test image content".getBytes()
    );
    
    mockMvc.perform(multipart("/api/listings")
        .file(data)
        .file(image))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.images").isArray());
}

@Test
@WithMockUser(userId = "50", roles = "SELLER")
void updateStock_ValidRequest_Returns200() throws Exception {
    mockMvc.perform(patch("/api/listings/301/stock")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"quantity\": 25, \"operation\": \"SET\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.newQuantity").value(25));
}

@Test
@WithMockUser(userId = "999", roles = "SELLER")
void updateListing_NotOwner_Returns403() throws Exception {
    mockMvc.perform(put("/api/listings/301")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isForbidden());
}
```

---

## 🔗 Dependencies

### Related Modules
- **Module 02:** User Management (seller verification)
- **Module 03:** Books Catalog (book reference)
- **Module 11:** Seller Dashboard (listing management)
- **Module 05:** Shopping Cart (add to cart uses listing)

### External Services
- Image upload service (S3/Cloudinary)
- Image processing (resize/optimize)

---

## ✅ Acceptance Criteria

- [ ] List endpoint supports seller and status filtering
- [ ] Pagination format matches FE expectations
- [ ] View count increments on listing detail view
- [ ] View count doesn't increment for seller's own views
- [ ] Sellers can create listings with image upload
- [ ] Images are validated (size, format)
- [ ] Sellers can update their own listings
- [ ] Stock update endpoint works with SET/ADD/SUBTRACT operations
- [ ] Status auto-updates when stock reaches zero
- [ ] Related listings shown on detail page
- [ ] All endpoints return standardized format
- [ ] Unit test coverage ≥ 80%
- [ ] Integration tests pass

---

## 📅 Timeline

| Task | Estimated Time | Status |
|------|----------------|--------|
| Add filters to list endpoint | 2 hours | ❌ |
| Implement view count increment | 1 hour | ❌ |
| Verify image upload in create | 2 hours | ❌ |
| Implement stock update endpoint | 2 hours | ❌ |
| Add auto-status update logic | 1 hour | ❌ |
| Add related listings to detail | 2 hours | ❌ |
| Write unit tests | 3 hours | ❌ |
| Write integration tests | 2 hours | ❌ |
| Documentation | 1 hour | ❌ |
| **Total** | **16 hours (2 days)** | |

---

## 📝 Notes

- Consider adding bulk operations (update multiple listings at once)
- Consider adding listing analytics (views over time, conversion rate)
- Consider adding featured/promoted listings (future)
- Image upload should support drag & drop in FE
- Consider automatic image optimization/CDN integration
- Monitor view count accuracy (prevent view count inflation)

---

## ✔️ Sign-off

**Developer:** _________________ Date: _______  
**Reviewer:** _________________ Date: _______  
**QA:** _________________ Date: _______