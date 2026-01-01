# BookVerse Backend API Implementation Status Report

**Last Updated:** January 1, 2026  
**Analysis Scope:** Complete Frontend & Backend Source Code Review

---

## Table of Contents
1. [Executive Summary](#executive-summary)
2. [Completed APIs](#1-completed-apis-)
3. [APIs Needing Refinement](#2-apis-needing-refinement-)
4. [Missing APIs](#3-missing-apis-)
5. [Implementation Checklist](#4-implementation-checklist)
6. [Progress Summary Dashboard](#5-progress-summary-dashboard)
7. [Database Schema Additions](#7-database-schema-additions-required)
8. [API Response Format](#8-api-response-format-standardization)
9. [Deployment Checklist](#9-deployment-checklist)

---

## Executive Summary

### Overall Status
- **✅ Completed:** 45% (Core authentication, cart, checkout, basic CRUD)
- **⚠️ Needs Refinement:** 20% (Pagination, filters, response formats)
- **❌ Missing:** 35% (Social features, seller dashboard, analytics)

### Critical Findings
1. **Strong Foundation:** Auth, Cart, Payment, and Basic Book/Listing CRUD are solid.
2. **Major Gaps:** No Review system, No Wishlist, No Seller Dashboard/Analytics.
3. **Data Model Issues:** Missing entities for Reviews, Promotions, Wishlist.
4. **Frontend Blockers:** Several FE pages will fail due to missing API endpoints.

---

## 1. Completed APIs ✅

These endpoints are **fully implemented** in the Backend and match FE requirements.

### 1.1 Authentication & Authorization
**Backend:** `AuthenticationController.java`, `OtpController.java`  
**Frontend:** `src/services/auth.ts`, `src/app/auth/*`

| Method | Endpoint | Controller | Status | Notes |
|--------|----------|------------|--------|-------|
| POST | `/api/auth/register` | AuthenticationController | ✅ | With OTP verification |
| POST | `/api/auth/verify-otp` | OtpController | ✅ | Email verification |
| POST | `/api/auth/login` | AuthenticationController | ✅ | Returns JWT |
| POST | `/api/auth/refresh` | AuthenticationController | ✅ | Token refresh |
| POST | `/api/auth/logout` | AuthenticationController | ✅ | Invalidates token |
| POST | `/api/auth/introspect` | AuthenticationController | ✅ | Token validation |
| POST | `/api/auth/google` | AuthenticationController | ✅ | OAuth integration |

**Verification:** ✅ All auth flows work correctly with FE.

---

### 1.2 User Profile Management
**Backend:** `UserController.java`, `UserProfileController.java`  
**Frontend:** `src/services/users.ts`, `src/app/onboarding/profile`

| Method | Endpoint | Controller | Status | Notes |
|--------|----------|------------|--------|-------|
| GET | `/api/users/me` | UserController | ✅ | Get current user |
| POST | `/api/users/profile` | UserProfileController | ✅ | Create profile |
| PUT | `/api/users/profile` | UserProfileController | ✅ | Update profile |
| POST | `/api/users/avatar` | UserProfileController | ✅ | Upload avatar (multipart) |

**Verification:** ✅ Profile CRUD works.

---

### 1.3 Shopping Cart
**Backend:** `CartController.java`, `CartItemController.java`  
**Frontend:** `src/app/home/cart`

| Method | Endpoint | Controller | Status | Notes |
|--------|----------|------------|--------|-------|
| GET | `/api/cart` | CartController | ✅ | Get user cart |
| POST | `/api/cart/items` | CartItemController | ✅ | Add to cart |
| PUT | `/api/cart/items/{id}` | CartItemController | ✅ | Update quantity |
| DELETE | `/api/cart/items/{id}` | CartItemController | ✅ | Remove item |

**Verification:** ✅ Cart operations work.

---

### 1.4 Checkout & Payment
**Backend:** `CheckoutController.java`, `TransactionController.java`  
**Frontend:** `src/app/home/checkout`

| Method | Endpoint | Controller | Status | Notes |
|--------|----------|------------|--------|-------|
| POST | `/api/checkout/create-order` | CheckoutController | ✅ | Initialize order |
| POST | `/api/payment/stripe/intent` | TransactionController | ✅ | Create payment intent |
| POST | `/api/payment/stripe/confirm` | TransactionController | ✅ | Confirm payment |
| GET | `/api/transactions/history` | TransactionController | ✅ | Payment history |

**Verification:** ✅ Stripe integration functional.

---

### 1.5 Shipping Addresses
**Backend:** `ShippingAddressController.java`  
**Frontend:** `src/app/home/checkout/shipping`

| Method | Endpoint | Controller | Status | Notes |
|--------|----------|------------|--------|-------|
| GET | `/api/shipping-addresses` | ShippingAddressController | ✅ | List addresses |
| POST | `/api/shipping-addresses` | ShippingAddressController | ✅ | Create address |
| PUT | `/api/shipping-addresses/{id}` | ShippingAddressController | ✅ | Update address |
| DELETE | `/api/shipping-addresses/{id}` | ShippingAddressController | ✅ | Delete address |
| PATCH | `/api/shipping-addresses/{id}/default` | ShippingAddressController | ✅ | Set default |

**Verification:** ✅ Address management works.

---

### 1.6 Categories & Authors (Basic CRUD)
**Backend:** `CategoryController.java`, `AuthorController.java`  
**Frontend:** `src/app/categories`, `src/components/FeaturedAuthors.tsx`

| Method | Endpoint | Controller | Status | Notes |
|--------|----------|------------|--------|-------|
| GET | `/api/categories` | CategoryController | ✅ | List all |
| POST | `/api/categories` | CategoryController | ✅ | Admin only |
| GET | `/api/authors` | AuthorController | ✅ | List all |
| POST | `/api/authors` | AuthorController | ✅ | Admin only |

**Verification:** ✅ Basic operations work.

---

### 1.7 Vouchers
**Backend:** `VoucherController.java`  
**Frontend:** `src/app/home/checkout`

| Method | Endpoint | Controller | Status | Notes |
|--------|----------|------------|--------|-------|
| POST | `/api/vouchers/validate` | VoucherController | ✅ | Check if code valid |
| POST | `/api/vouchers` | VoucherController | ✅ | Admin create |
| GET | `/api/vouchers/{code}` | VoucherController | ✅ | Get voucher details |

**Verification:** ✅ Voucher system works.

---

## 2. APIs Needing Refinement ⚠️

These endpoints **exist** but need updates to match FE expectations.

### 2.1 Books Catalog
**Backend:** `BookController.java`  
**Frontend:** `src/services/books.ts`, `src/hooks/use-books.ts`

| Method | Endpoint | Current Issue | Required Fix |
|--------|----------|---------------|--------------|
| GET | `/api/books` | ⚠️ Pagination format mismatch | Return `{ data: [], meta: { page, totalPages, totalItems } }` |
| GET | `/api/books` | ⚠️ Missing filters | Add `categoryId`, `priceMin`, `priceMax`, `condition`, `rating` query params |
| GET | `/api/books` | ⚠️ Missing sort options | Add `sortBy` (price, rating, createdAt) and `sortOrder` (asc, desc) |
| GET | `/api/books/{id}` | ⚠️ Missing related data | Include `averageRating`, `totalReviews`, `seller` info in response |

**Action Items:**
```java
// BookController.java - Add Query Parameters
@GetMapping
public ResponseEntity<PagedResponse<BookDto>> getBooks(
    @RequestParam(required = false) Long categoryId,
    @RequestParam(required = false) BigDecimal priceMin,
    @RequestParam(required = false) BigDecimal priceMax,
    @RequestParam(required = false) BookCondition condition,
    @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
    @RequestParam(required = false, defaultValue = "desc") String sortOrder,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size
) {
    // Implementation with filters
}
```

**Priority:** 🔴 High (Blocking FE book browsing)

---

### 2.2 Listings
**Backend:** `ListingController.java`  
**Frontend:** `src/app/seller/listings`

| Method | Endpoint | Current Issue | Required Fix |
|--------|----------|---------------|--------------|
| GET | `/api/listings` | ⚠️ No seller filtering | Add `?sellerId={id}` to filter listings by seller |
| GET | `/api/listings` | ⚠️ Missing status filter | Add `?status=ACTIVE/SOLD_OUT/DRAFT` |
| POST | `/api/listings` | ⚠️ Image upload unclear | Verify multipart/form-data handling, return image URLs |
| GET | `/api/listings/{id}` | ⚠️ Missing view count increment | Increment `viewCount` on each GET (exclude seller's own views) |

**Action Items:**
```java
// ListingController.java
@GetMapping
public ResponseEntity<PagedResponse<ListingDto>> getListings(
    @RequestParam(required = false) Long sellerId,
    @RequestParam(required = false) ListingStatus status,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "20") int size
) {
    // Implementation
}

@GetMapping("/{id}")
public ResponseEntity<ListingDetailDto> getListingById(@PathVariable Long id) {
    // Increment viewCount
    listingService.incrementViewCount(id);
    return ResponseEntity.ok(listingService.getDetailById(id));
}
```

**Priority:** 🟡 Medium (Seller dashboard needs this)

---

### 2.3 Orders
**Backend:** `OrderController.java`  
**Frontend:** `src/app/home/my-account` (orders tab)

| Method | Endpoint | Current Issue | Required Fix |
|--------|----------|---------------|--------------|
| GET | `/api/orders` | ⚠️ Returns all orders | Should filter by current user automatically |
| GET | `/api/orders` | ⚠️ Missing status filter | Add `?status=PENDING/SHIPPED/DELIVERED/CANCELLED` |
| GET | `/api/orders/{id}` | ⚠️ Missing detailed items | Include full `OrderItem` list with book details, seller info |

**Action Items:**
- Ensure `OrderController` uses `@AuthenticationPrincipal` to get current user
- Add DTO that includes nested book/seller information

**Priority:** 🟡 Medium

---

### 2.4 User Roles & Seller Registration
**Backend:** `UserController.java`, `RoleController.java`  
**Frontend:** `src/app/onboarding/seller`

| Method | Endpoint | Current Issue | Required Fix |
|--------|----------|---------------|--------------|
| POST | `/api/users/upgrade-to-seller` | ⚠️ Logic unclear | Verify it assigns `ROLE_SELLER` and creates seller profile entry |
| GET | `/api/users/me` | ⚠️ Missing seller status | Response should include `isSeller: boolean`, `isProSeller: boolean` |

**Action Items:**
```java
// UserDto should include:
private boolean isSeller;
private boolean isProSeller;
private SellerProfile sellerProfile; // Nested if seller
```

**Priority:** 🟡 Medium

---

### 2.5 Search
**Backend:** `BookController.java` or dedicated `SearchController`  
**Frontend:** `src/components/SearchBar.tsx`

| Method | Endpoint | Current Issue | Required Fix |
|--------|----------|---------------|--------------|
| GET | `/api/books/search?q={query}` | ⚠️ Basic search only | Should search across: title, author name, ISBN, category |
| N/A | N/A | ❌ Missing | Add `/api/books/search/suggestions?q={query}` for autocomplete (returns top 5 titles) |

**Action Items:**
- Implement full-text search (PostgreSQL `tsvector` or integrate Elasticsearch)
- Add suggestions endpoint for search-as-you-type

**Priority:** 🟡 Medium

---

## 3. Missing APIs ❌

These features are **completely absent** from the Backend but required by the Frontend.

### 3.1 Reviews & Ratings System
**Backend:** ❌ No `ReviewController`, no `Review` entity  
**Frontend:** `src/hooks/use-reviews.ts`, `src/app/books/[id]` (reviews section)

#### Required Endpoints

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| **POST** | `/api/books/{bookId}/reviews` | Create review | `{ rating: number, comment: string }` | `ReviewDto` |
| **GET** | `/api/books/{bookId}/reviews` | List reviews (paginated) | Query: `page`, `size`, `sortBy=rating/createdAt` | `PagedResponse<ReviewDto>` |
| **PUT** | `/api/reviews/{id}` | Update own review | `{ rating: number, comment: string }` | `ReviewDto` |
| **DELETE** | `/api/reviews/{id}` | Delete own review | - | `204 No Content` |
| **GET** | `/api/books/{bookId}/rating` | Get aggregate rating | - | `{ averageRating: 4.5, totalReviews: 120 }` |
| **POST** | `/api/reviews/{id}/helpful` | Mark review helpful | - | `{ helpfulCount: number }` |

#### Required Entity
```java
@Entity
@Table(name = "reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "book_id")
    private BookMeta book;
    
    private Integer rating; // 1-5
    
    @Column(columnDefinition = "TEXT")
    private String comment;
    
    private Integer helpfulCount = 0;
    
    private LocalDateTime createdAt;
    
    // Unique constraint: one review per user per book
    @Table(uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "book_id"}))
}
```

**Priority:** 🔴 **CRITICAL** - Reviews are core to e-commerce UX

---

### 3.2 Wishlist System
**Backend:** ❌ No `WishlistController`, no `Wishlist` entity  
**Frontend:** `src/hooks/use-wishlist.ts`, Heart icons throughout app

#### Required Endpoints

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| **GET** | `/api/wishlist` | Get user's wishlist | - | `List<BookDto>` |
| **POST** | `/api/wishlist` | Add book to wishlist | `{ bookId: number }` | `WishlistItemDto` |
| **DELETE** | `/api/wishlist/{bookId}` | Remove from wishlist | - | `204 No Content` |
| **GET** | `/api/wishlist/check/{bookId}` | Check if book in wishlist | - | `{ inWishlist: boolean }` |

#### Required Entity
```java
@Entity
@Table(name = "wishlist")
public class Wishlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "book_id")
    private BookMeta book;
    
    private LocalDateTime addedAt;
    
    @Table(uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "book_id"}))
}
```

**Priority:** 🔴 **CRITICAL** - Wishlist is expected in all book browsing pages

---

### 3.3 Seller Dashboard & Analytics
**Backend:** ❌ No `SellerController` or `SellerAnalyticsController`  
**Frontend:** `src/app/seller/*`, `src/services/seller.ts`

#### 3.3.1 Seller Orders Management

| Method | Endpoint | Description | Query Params | Response |
|--------|----------|-------------|--------------|----------|
| **GET** | `/api/seller/orders` | Get orders containing seller's books | `status`, `page`, `size` | `PagedResponse<SellerOrderDto>` |
| **GET** | `/api/seller/orders/{orderId}` | Get order details | - | `SellerOrderDetailDto` |
| **PATCH** | `/api/seller/orders/{orderId}/status` | Update order status | `{ status: "SHIPPED/DELIVERED" }` | `SellerOrderDto` |

**Logic:** An order can contain items from multiple sellers. Each seller should only see/manage their own items within orders.

#### 3.3.2 Seller Analytics

| Method | Endpoint | Description | Query Params | Response |
|--------|----------|-------------|--------------|----------|
| **GET** | `/api/seller/analytics/overview` | Dashboard summary | `period=7d/30d/90d/1y` | `{ totalRevenue, totalOrders, totalProducts, avgRating }` |
| **GET** | `/api/seller/analytics/revenue-chart` | Revenue over time | `period`, `groupBy=day/week/month` | `List<{ date, revenue }>` |
| **GET** | `/api/seller/analytics/top-products` | Best-selling products | `limit=10` | `List<{ bookId, title, salesCount, revenue }>` |
| **GET** | `/api/seller/analytics/traffic` | View/click stats | `period` | `{ totalViews, totalClicks, conversionRate }` |

#### Required Implementation
```java
@RestController
@RequestMapping("/api/seller")
@PreAuthorize("hasRole('SELLER')")
public class SellerController {
    
    @GetMapping("/orders")
    public ResponseEntity<PagedResponse<SellerOrderDto>> getSellerOrders(
        @AuthenticationPrincipal User currentUser,
        @RequestParam(required = false) OrderStatus status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        // Logic: Find all OrderItems where listing.seller = currentUser
        // Group by Order, return orders that contain seller's items
    }
    
    @GetMapping("/analytics/overview")
    public ResponseEntity<SellerAnalyticsDto> getAnalytics(
        @AuthenticationPrincipal User currentUser,
        @RequestParam(defaultValue = "30d") String period
    ) {
        // Calculate revenue, order count, product count for seller's listings
        // within the specified period
    }
}
```

**Priority:** 🔴 **CRITICAL** - Entire seller dashboard is non-functional without this

---

### 3.4 Curated Book Feeds
**Backend:** ❌ Logic not implemented in `BookController`  
**Frontend:** `src/hooks/use-trending-books.ts`, `use-popular-books.ts`, `use-recommend-books.ts`, `use-new-release.ts`

#### Required Endpoints

| Method | Endpoint | Description | Logic | Response |
|--------|----------|-------------|-------|----------|
| **GET** | `/api/books/trending` | Trending books | Most views/sales in last 7 days | `List<BookDto>` (limit 20) |
| **GET** | `/api/books/popular` | Popular books | Highest average rating + review count | `List<BookDto>` (limit 20) |
| **GET** | `/api/books/recommended` | Personalized recommendations | Based on user's order history or category preferences | `List<BookDto>` (limit 20) |
| **GET** | `/api/books/new-releases` | New releases | Books added in last 30 days, sorted by creation date desc | `List<BookDto>` (limit 20) |

#### Implementation Example
```java
// BookController.java
@GetMapping("/trending")
public ResponseEntity<List<BookDto>> getTrendingBooks() {
    LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
    
    // Option 1: Join Listing with Order/View tracking
    // Option 2: Use a cached/scheduled job to calculate trending books
    
    List<BookMeta> trendingBooks = bookRepository.findTrendingBooks(weekAgo, PageRequest.of(0, 20));
    return ResponseEntity.ok(bookMapper.toDtoList(trendingBooks));
}

@GetMapping("/popular")
public ResponseEntity<List<BookDto>> getPopularBooks() {
    // Find books with highest average rating (from reviews) and review count > 10
    List<BookMeta> popularBooks = bookRepository.findPopularBooks(PageRequest.of(0, 20));
    return ResponseEntity.ok(bookMapper.toDtoList(popularBooks));
}
```

**Priority:** 🟡 High - Homepage relies on these feeds

---

### 3.5 Seller-Created Promotions
**Backend:** ❌ No `PromotionController`, no `Promotion` entity  
**Frontend:** `src/app/seller/promotions`

#### Required Endpoints

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| **POST** | `/api/seller/promotions` | Create promotion | `{ code, discountPercent, validFrom, validTo, maxUses, applicableBookIds[] }` | `PromotionDto` |
| **GET** | `/api/seller/promotions` | List seller's promotions | `status=active/expired/upcoming` | `List<PromotionDto>` |
| **PUT** | `/api/seller/promotions/{id}` | Update promotion | Same as POST | `PromotionDto` |
| **DELETE** | `/api/seller/promotions/{id}` | Delete promotion | - | `204 No Content` |
| **GET** | `/api/promotions/{code}/validate` | Validate promo code (public) | Query: `bookIds[]` | `{ valid: boolean, discountPercent, applicableBooks[] }` |

#### Required Entity
```java
@Entity
@Table(name = "promotions")
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User seller;
    
    private String code; // Unique
    
    private Integer discountPercent; // 10 = 10% off
    
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    
    private Integer maxUses; // Null = unlimited
    private Integer currentUses = 0;
    
    @ManyToMany
    @JoinTable(
        name = "promotion_books",
        joinColumns = @JoinColumn(name = "promotion_id"),
        inverseJoinColumns = @JoinColumn(name = "book_id")
    )
    private Set<BookMeta> applicableBooks; // Empty = all seller's books
}
```

**Priority:** 🟡 High - Key feature for sellers to drive sales

---

### 3.6 Pro Seller Application
**Backend:** ❌ No `ProSellerController`, no `SellerApplication` entity  
**Frontend:** `src/app/seller/upgrade-to-pro`

#### Required Endpoints

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| **POST** | `/api/seller/pro/apply` | Submit Pro application | `{ businessName, taxId, documents[] }` (multipart) | `ApplicationDto` |
| **GET** | `/api/seller/pro/status` | Check application status | - | `{ status: "PENDING/APPROVED/REJECTED", submittedAt, reviewedAt, notes }` |

#### Required Entity
```java
@Entity
@Table(name = "seller_applications")
public class SellerApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    private String businessName;
    private String taxId;
    
    @ElementCollection
    private List<String> documentUrls; // S3/Cloudinary URLs
    
    @Enumerated(EnumType.STRING)
    private ApplicationStatus status; // PENDING, APPROVED, REJECTED
    
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    
    @Column(columnDefinition = "TEXT")
    private String adminNotes;
}
```

**Priority:** 🟡 High - Required for seller tier system

---

### 3.7 Public Seller Profile
**Backend:** ❌ No endpoint for public user/seller data  
**Frontend:** `src/app/seller/[sellerId]/page.tsx`

#### Required Endpoints

| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| **GET** | `/api/sellers/{id}/profile` | Public seller info | `{ id, username, avatar, bio, joinedAt, rating, totalSales, totalReviews }` |
| **GET** | `/api/sellers/{id}/listings` | Seller's active listings | `PagedResponse<ListingDto>` with filters |
| **GET** | `/api/sellers/{id}/reviews` | Reviews about the seller | `PagedResponse<SellerReviewDto>` |

**Security Note:** Do **not** expose email, phone, address in public profile.

**Priority:** 🟡 High - Important for marketplace trust

---

### 3.8 Blog/Content System
**Backend:** ❌ No `BlogController`, no `BlogPost` entity  
**Frontend:** `src/app/blogs/page.tsx`

#### Required Endpoints

| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| **GET** | `/api/blogs` | List blog posts | `PagedResponse<BlogPostDto>` |
| **GET** | `/api/blogs/{slug}` | Get blog by slug | `BlogPostDetailDto` |
| **POST** | `/api/blogs` | Create blog (Admin) | `BlogPostDto` |

**Priority:** 🟢 Low - Can be hardcoded in FE initially, add later

---

### 3.9 System Configuration
**Backend:** ❌ `SystemParameter` entity exists but no Controller  
**Frontend:** May be needed for dynamic config

#### Required Endpoints

| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| **GET** | `/api/system/config` | Public config | `{ maintenanceMode: boolean, supportEmail, taxRate, currency }` |

**Priority:** 🟢 Low - Nice to have for production

---

### 3.10 Search Suggestions
**Backend:** ❌ No autocomplete endpoint  
**Frontend:** `src/components/SearchBar.tsx` (search-as-you-type)

#### Required Endpoints

| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| **GET** | `/api/books/search/suggestions?q={query}` | Autocomplete | `List<{ id, title, author, coverImage }>` (max 10) |

**Implementation:**
```java
@GetMapping("/search/suggestions")
public ResponseEntity<List<SearchSuggestionDto>> getSuggestions(
    @RequestParam String q
) {
    // Search book titles and author names using LIKE or full-text search
    // Return lightweight DTOs (no full book data)
    List<SearchSuggestionDto> suggestions = bookService.getSearchSuggestions(q, 10);
    return ResponseEntity.ok(suggestions);
}
```

**Priority:** 🟢 Medium - Improves UX but not blocking

---

## 4. Implementation Checklist

### 🔴 Critical Priority (Week 1-2) - Frontend Blockers

#### Reviews & Ratings
- [ ] Create `Review` entity with unique constraint (user + book)
- [ ] Create `ReviewController` with CRUD endpoints
- [ ] Implement `POST /api/books/{bookId}/reviews`
- [ ] Implement `GET /api/books/{bookId}/reviews` (paginated)
- [ ] Implement `PUT /api/reviews/{id}` (owner only)
- [ ] Implement `DELETE /api/reviews/{id}` (owner only)
- [ ] Implement `GET /api/books/{bookId}/rating` (aggregate)
- [ ] Implement `POST /api/reviews/{id}/helpful` (helpful votes)
- [ ] Update `BookDto` to include `averageRating` and `totalReviews`
- [ ] Write unit tests for ReviewService
- [ ] Write integration tests for ReviewController

#### Wishlist
- [ ] Create `Wishlist` entity with unique constraint (user + book)
- [ ] Create `WishlistController`
- [ ] Implement `GET /api/wishlist`
- [ ] Implement `POST /api/wishlist` (add book)
- [ ] Implement `DELETE /api/wishlist/{bookId}`
- [ ] Implement `GET /api/wishlist/check/{bookId}`
- [ ] Write unit tests for WishlistService
- [ ] Write integration tests for WishlistController

#### Seller Dashboard - Orders
- [ ] Create `SellerController`
- [ ] Implement `GET /api/seller/orders` (filtered by seller's listings)
- [ ] Implement `GET /api/seller/orders/{id}` (order details)
- [ ] Implement `PATCH /api/seller/orders/{id}/status` (update shipping status)
- [ ] Create `SellerOrderDto` with nested order items
- [ ] Ensure order email notifications trigger on status update
- [ ] Write unit tests for SellerOrderService
- [ ] Write integration tests for SellerController

---

### 🟡 High Priority (Week 3-4) - Core Features

#### Seller Analytics
- [ ] Implement `GET /api/seller/analytics/overview`
- [ ] Implement `GET /api/seller/analytics/revenue-chart`
- [ ] Implement `GET /api/seller/analytics/top-products`
- [ ] Implement `GET /api/seller/analytics/traffic`
- [ ] Create scheduled job to pre-calculate analytics (optional optimization)
- [ ] Write unit tests for AnalyticsService

#### Promotions
- [ ] Create `Promotion` entity
- [ ] Create `PromotionController`
- [ ] Implement `POST /api/seller/promotions`
- [ ] Implement `GET /api/seller/promotions`
- [ ] Implement `PUT /api/seller/promotions/{id}`
- [ ] Implement `DELETE /api/seller/promotions/{id}`
- [ ] Implement `GET /api/promotions/{code}/validate` (public)
- [ ] Update `CartService` to apply promotions automatically
- [ ] Write unit tests for PromotionService

#### Pro Seller Application
- [ ] Create `SellerApplication` entity
- [ ] Create `ProSellerController`
- [ ] Implement `POST /api/seller/pro/apply` with file upload
- [ ] Implement `GET /api/seller/pro/status`
- [ ] Add admin endpoint `PATCH /api/admin/applications/{id}` (approve/reject)
- [ ] Update `User` entity to include `isProSeller` flag
- [ ] Send email notification on application status change
- [ ] Write unit tests for ProSellerService

#### Public Seller Profile
- [ ] Implement `GET /api/sellers/{id}/profile`
- [ ] Implement `GET /api/sellers/{id}/listings`
- [ ] Implement `GET /api/sellers/{id}/reviews`
- [ ] Ensure response excludes private user data (email, phone, etc.)
- [ ] Write unit tests for SellerProfileService

#### Curated Feeds
- [ ] Implement `GET /api/books/trending`
- [ ] Implement `GET /api/books/popular`
- [ ] Implement `GET /api/books/recommended`
- [ ] Implement `GET /api/books/new-releases`
- [ ] Add database indexes for performance (views, rating, createdAt)
- [ ] Consider caching these endpoints (Redis)
- [ ] Write unit tests for feed queries

---

### ⚠️ Refinement Tasks (Week 3-4) - Align with FE

#### Books API
- [ ] Update `GET /api/books` to return standardized pagination format
- [ ] Add query params: `categoryId`, `priceMin`, `priceMax`, `condition`, `rating`
- [ ] Add query params: `sortBy` (price, rating, createdAt), `sortOrder` (asc, desc)
- [ ] Update `GET /api/books/{id}` to include `averageRating`, `totalReviews`, `seller` info
- [ ] Write integration tests for all filter combinations

#### Listings API
- [ ] Update `GET /api/listings` to support `sellerId` filter
- [ ] Add `status` filter (ACTIVE, SOLD_OUT, DRAFT)
- [ ] Ensure `GET /api/listings/{id}` increments `viewCount` (exclude seller)
- [ ] Verify multipart file upload returns public image URLs
- [ ] Write tests for view count increment logic

#### Orders API
- [ ] Update `GET /api/orders` to auto-filter by current user
- [ ] Add `status` filter query param
- [ ] Update `GET /api/orders/{id}` to include full order item details with book/seller info
- [ ] Write tests for order filtering

#### User API
- [ ] Update `GET /api/users/me` response to include `isSeller`, `isProSeller`, `sellerProfile`
- [ ] Verify `POST /api/users/upgrade-to-seller` creates seller profile correctly
- [ ] Write tests for seller upgrade flow

#### Search
- [ ] Implement full-text search across title, author, ISBN, category
- [ ] Add `GET /api/books/search/suggestions?q={query}` for autocomplete
- [ ] Consider Elasticsearch integration for better performance
- [ ] Write tests for search accuracy

---

### 🟢 Low Priority (Month 2) - Polish & Enhancements

#### Blog System
- [ ] Create `BlogPost` entity
- [ ] Create `BlogController`
- [ ] Implement `GET /api/blogs` (paginated)
- [ ] Implement `GET /api/blogs/{slug}`
- [ ] Implement `POST /api/blogs` (Admin only)
- [ ] Add rich text editor support for blog content

#### System Configuration
- [ ] Create `SystemConfigController`
- [ ] Implement `GET /api/system/config` (public safe configs)
- [ ] Implement `PUT /api/admin/config` (Admin update configs)

#### Admin Panel APIs
- [ ] Create `AdminController`
- [ ] Implement user management endpoints (list, ban, delete)
- [ ] Implement content moderation endpoints (reviews, listings)
- [ ] Implement platform analytics (total users, revenue, etc.)

#### Notification System
- [ ] Create `NotificationController`
- [ ] Implement `GET /api/notifications` (user's notifications)
- [ ] Implement `PATCH /api/notifications/{id}/read`
- [ ] Add real-time notifications (WebSocket or Server-Sent Events)

#### Enhanced Features
- [ ] Add book comparison feature (compare 2-3 books side-by-side)
- [ ] Add reading list/collection feature (beyond wishlist)
- [ ] Add book recommendation engine (ML-based, optional)
- [ ] Add seller messaging system (buyer can message seller)

---

### 🧪 Testing & Quality Assurance

#### Unit Tests
- [ ] Achieve 80%+ code coverage for all services
- [ ] Test all business logic in isolation
- [ ] Test edge cases (empty lists, null values, invalid inputs)

#### Integration Tests
- [ ] Test all controller endpoints with MockMvc
- [ ] Test authentication/authorization on protected endpoints
- [ ] Test database transactions (rollback on error)
- [ ] Test file upload scenarios

#### End-to-End Tests (Optional)
- [ ] Set up E2E test framework (Selenium/Cypress)
- [ ] Test critical user journeys (register → browse → purchase)
- [ ] Test seller workflows (create listing → receive order → ship)

#### Performance Tests
- [ ] Load test high-traffic endpoints (book listing, search)
- [ ] Optimize database queries (add indexes, avoid N+1)
- [ ] Set up database connection pooling
- [ ] Consider implementing Redis caching for read-heavy endpoints

---

### 📚 Documentation

- [ ] Update Swagger/OpenAPI documentation for all new endpoints
- [ ] Add request/response examples in Swagger
- [ ] Document error codes and messages
- [ ] Create API integration guide for FE team
- [ ] Document database schema changes
- [ ] Create deployment guide (Docker, env variables)

---

### 🔒 Security & Production Readiness

- [ ] Audit all endpoints for proper authorization (`@PreAuthorize`)
- [ ] Add rate limiting (Spring Boot Rate Limiter or API Gateway)
- [ ] Implement input validation on all DTOs (`@Valid`, `@NotNull`, etc.)
- [ ] Add SQL injection protection (verify all queries use parameterized statements)
- [ ] Add XSS protection (sanitize user-generated content)
- [ ] Configure CORS for production frontend URL
- [ ] Set up HTTPS in production
- [ ] Configure database connection pooling (HikariCP)
- [ ] Set up logging and monitoring (ELK stack or CloudWatch)
- [ ] Implement database backups and disaster recovery

---

## 5. Progress Summary Dashboard

### Implementation Status by Module

| Module | Completed | To Refine | Missing | Total | Progress |
|--------|-----------|-----------|---------|-------|----------|
| **Authentication** | 7 | 0 | 0 | 7 | 100% ✅ |
| **User Profile** | 4 | 2 | 1 | 7 | 57% ⚠️ |
| **Books Catalog** | 2 | 4 | 4 | 10 | 20% ❌ |
| **Listings** | 4 | 4 | 0 | 8 | 50% ⚠️ |
| **Cart** | 4 | 0 | 0 | 4 | 100% ✅ |
| **Checkout** | 4 | 0 | 0 | 4 | 100% ✅ |
| **Orders** | 1 | 3 | 0 | 4 | 25% ❌ |
| **Reviews** | 0 | 0 | 8 | 8 | 0% ❌ |
| **Wishlist** | 0 | 0 | 4 | 4 | 0% ❌ |
| **Seller Dashboard** | 0 | 0 | 7 | 7 | 0% ❌ |
| **Seller Analytics** | 0 | 0 | 4 | 4 | 0% ❌ |
| **Promotions** | 0 | 0 | 7 | 7 | 0% ❌ |
| **Pro Seller** | 0 | 0 | 4 | 4 | 0% ❌ |
| **Public Seller** | 0 | 0 | 3 | 3 | 0% ❌ |
| **Categories/Authors** | 4 | 0 | 0 | 4 | 100% ✅ |
| **Vouchers** | 3 | 0 | 0 | 3 | 100% ✅ |
| **Shipping** | 5 | 0 | 0 | 5 | 100% ✅ |
| **Blog** | 0 | 0 | 3 | 3 | 0% ❌ |
| **System Config** | 0 | 0 | 1 | 1 | 0% ❌ |
| **Search** | 0 | 2 | 1 | 3 | 0% ❌ |
| **TOTAL** | **38** | **15** | **47** | **100** | **38%** |

---

### Sprint Planning Recommendations

#### Sprint 1 (2 weeks) - Unblock Frontend
**Goal:** Enable FE review and wishlist features
- ✅ Implement Reviews API (8 endpoints)
- ✅ Implement Wishlist API (4 endpoints)
- ✅ Refine Books API (add filters, pagination)
- ✅ Refine Listings API (add seller filter)

**Expected Outcome:** FE can fully browse books, add reviews, manage wishlist

---

#### Sprint 2 (2 weeks) - Enable Seller Features
**Goal:** Make seller dashboard functional
- ✅ Implement Seller Orders Management (3 endpoints)
- ✅ Implement Seller Analytics (4 endpoints)
- ✅ Implement Promotions (7 endpoints)
- ✅ Refine Orders API (add filters)

**Expected Outcome:** Sellers can manage orders, view analytics, create promotions

---

#### Sprint 3 (2 weeks) - Complete Seller Ecosystem
**Goal:** Pro seller and public profiles
- ✅ Implement Pro Seller Application (4 endpoints)
- ✅ Implement Public Seller Profile (3 endpoints)
- ✅ Implement Curated Feeds (4 endpoints)
- ✅ Refine User API (add seller flags)

**Expected Outcome:** Complete marketplace functionality

---

#### Sprint 4 (1 week) - Polish & Testing
**Goal:** Production readiness
- ✅ Search refinements (full-text + suggestions)
- ✅ Complete test coverage (unit + integration)
- ✅ Security audit
- ✅ Performance optimization
- ✅ Documentation update

**Expected Outcome:** Ready for production deployment

---

## 6. Critical Blockers for Frontend Team

The following missing APIs are **currently blocking** FE features:

1. **Reviews API** - Blocks:
   - Book detail page review section
   - User profile "My Reviews" tab
   - Rating display on all book cards

2. **Wishlist API** - Blocks:
   - Heart icon functionality on all book cards
   - Wishlist page (`/wishlist`)
   - Wishlist count in header

3. **Seller Orders API** - Blocks:
   - Entire `/seller/orders` page
   - Order status updates
   - Shipping management

4. **Seller Analytics API** - Blocks:
   - Entire `/seller/dashboard` page
   - All charts and statistics
   - Performance tracking

5. **Promotions API** - Blocks:
   - `/seller/promotions` page
   - Promo code application in cart
   - Discount calculations

**Recommendation:** Prioritize implementing Reviews and Wishlist APIs first (Sprint 1), as they affect the core user browsing experience. Seller features can follow in Sprint 2.

---

## 7. Database Schema Additions Required

### New Tables Needed

```sql
-- Reviews table
CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    book_id BIGINT NOT NULL REFERENCES book_meta(id),
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    helpful_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, book_id)
);

-- Wishlist table
CREATE TABLE wishlist (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    book_id BIGINT NOT NULL REFERENCES book_meta(id),
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, book_id)
);

-- Promotions table
CREATE TABLE promotions (
    id BIGSERIAL PRIMARY KEY,
    seller_id BIGINT NOT NULL REFERENCES users(id),
    code VARCHAR(50) UNIQUE NOT NULL,
    discount_percent INTEGER NOT NULL CHECK (discount_percent > 0 AND discount_percent <= 100),
    valid_from TIMESTAMP NOT NULL,
    valid_to TIMESTAMP NOT NULL,
    max_uses INTEGER,
    current_uses INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Promotion applicable books (many-to-many)
CREATE TABLE promotion_books (
    promotion_id BIGINT REFERENCES promotions(id),
    book_id BIGINT REFERENCES book_meta(id),
    PRIMARY KEY (promotion_id, book_id)
);

-- Seller applications table
CREATE TABLE seller_applications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL REFERENCES users(id),
    business_name VARCHAR(255) NOT NULL,
    tax_id VARCHAR(50) NOT NULL,
    document_urls TEXT[],
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP,
    admin_notes TEXT
);

-- Blog posts table (if implementing blog)
CREATE TABLE blog_posts (
    id BIGSERIAL PRIMARY KEY,
    slug VARCHAR(255) UNIQUE NOT NULL,
    title VARCHAR(500) NOT NULL,
    content TEXT NOT NULL,
    author_id BIGINT REFERENCES users(id),
    published_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Indexes to Add

```sql
-- Reviews indexes
CREATE INDEX idx_reviews_book_id ON reviews(book_id);
CREATE INDEX idx_reviews_user_id ON reviews(user_id);
CREATE INDEX idx_reviews_rating ON reviews(rating);

-- Wishlist indexes
CREATE INDEX idx_wishlist_user_id ON wishlist(user_id);
CREATE INDEX idx_wishlist_book_id ON wishlist(book_id);

-- Promotions indexes
CREATE INDEX idx_promotions_seller_id ON promotions(seller_id);
CREATE INDEX idx_promotions_code ON promotions(code);
CREATE INDEX idx_promotions_valid_dates ON promotions(valid_from, valid_to);

-- Book search optimization
CREATE INDEX idx_book_meta_title ON book_meta USING gin(to_tsvector('english', title));
CREATE INDEX idx_book_meta_created_at ON book_meta(created_at DESC);

-- Listing optimization
CREATE INDEX idx_listing_seller_id ON listing(seller_id);
CREATE INDEX idx_listing_status ON listing(status);
CREATE INDEX idx_listing_view_count ON listing(view_count DESC);
```

---

## 8. API Response Format Standardization

Ensure **all** endpoints follow this consistent format:

### Success Response
```json
{
  "success": true,
  "data": { /* actual data */ },
  "message": "Operation successful"
}
```

### Paginated Response
```json
{
  "success": true,
  "data": [/* items */],
  "meta": {
    "page": 1,
    "totalPages": 10,
    "totalItems": 95,
    "itemsPerPage": 10
  }
}
```

### Error Response
```json
{
  "success": false,
  "error": {
    "code": "RESOURCE_NOT_FOUND",
    "message": "Book with ID 123 not found",
    "details": null
  }
}
```

Update `GlobalExceptionHandler.java` to enforce this format across all exceptions.

---

## 9. Deployment Checklist

Before deploying to production:

- [ ] All environment variables configured (database, Stripe keys, email service)
- [ ] Database migrations executed
- [ ] CORS configured for production frontend URL
- [ ] HTTPS/SSL certificate configured
- [ ] Rate limiting enabled
- [ ] Logging configured (file rotation, log levels)
- [ ] Monitoring set up (health checks, error tracking)
- [ ] Backup strategy implemented
- [ ] Load balancer configured (if using multiple instances)
- [ ] CDN configured for static assets (book covers, avatars)

---

## 10. Contact & Coordination

**For Backend Team:**
- Review this document and confirm timeline feasibility
- Clarify any ambiguous requirements before implementation
- Update this document as work progresses
- Coordinate with FE team on response format changes

**For Frontend Team:**
- Confirm API requirements match your implementation
- Report any discrepancies found during integration
- Test each endpoint as it's deployed to staging
- Provide feedback on response format/structure

---

**Document Version:** 1.0  
**Last Review:** January 1, 2026  
**Next Review:** After Sprint 1 completion
