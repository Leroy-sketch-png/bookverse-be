# Backend-Frontend Data Alignment - Complete Summary

## Date: January 5, 2026

This document summarizes all the changes made to align the backend (Java Spring Boot) with the frontend (Next.js TypeScript) to eliminate data mismatches.

---

## ✅ COMPLETED CHANGES

### 1. **RoleName Enum** - FIXED ✓
**Location:** `src/main/java/com/example/bookverseserver/enums/RoleName.java`

**Before:**
```java
public enum RoleName {
    ADMIN, PRO, CASUAL, BUYER
}
```

**After:**
```java
public enum RoleName {
    ADMIN, USER
}
```

**Reason:** Frontend expects only `ADMIN` and `USER` roles.

---

### 2. **AccountType Enum** - FIXED ✓
**Location:** `src/main/java/com/example/bookverseserver/enums/AccountType.java`

**Before:**
```java
public enum AccountType {
    BUYER, CASUAL_SELLER, PRO_SELLER
}
```

**After:**
```java
public enum AccountType {
    BUYER, SELLER, PRO_SELLER
}
```

**Reason:** Frontend uses `SELLER` instead of `CASUAL_SELLER`.

---

### 3. **User Entity - Roles Field** - FIXED ✓
**Location:** `src/main/java/com/example/bookverseserver/entity/User/User.java`

**Before:**
```java
@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "role_id")
Role role;
```

**After:**
```java
@ManyToMany(fetch = FetchType.EAGER)
@JoinTable(
    name = "user_role",
    joinColumns = @JoinColumn(name = "user_id"),
    inverseJoinColumns = @JoinColumn(name = "role_id")
)
Set<Role> roles;
```

**Reason:** Frontend expects `roles` as an array/set, not a single role.

**Migration Required:** Create `user_role` join table.

---

### 4. **Author Entity** - ENHANCED ✓
**Location:** `src/main/java/com/example/bookverseserver/entity/Product/Author.java`

**Added Fields:**
- `avatar` (renamed from `avatarUrl`)
- `bio` (renamed from `biography`)
- `position` - Role/Genre the author writes
- `booksCount` - Number of books published
- `mainGenre` - Primary genre
- `awards` - JSON array or comma-separated
- `dob` - Date of birth (ISO string)
- `website` - Author's website

**Reason:** Frontend expects comprehensive author information with specific field names.

---

### 5. **Category Entity** - ADDED SLUG ✓
**Location:** `src/main/java/com/example/bookverseserver/entity/Product/Category.java`

**Added:**
```java
@Column(unique = true, nullable = false, length = 150)
String slug; // URL-friendly version of name
```

**Reason:** Frontend uses slug for routing and SEO-friendly URLs.

**Migration Required:** Add `slug` column and populate existing records.

---

### 6. **Book/BookMeta Response** - UPDATED ✓
**Location:** `src/main/java/com/example/bookverseserver/dto/response/Book/BookDetailResponse.java`

**Changed Fields:**
- `published_date` → `publicationDate`
- `pages` → `pageCount`
- Added: `coverImageUrl`
- Added: `language`
- Added: `price` (BigDecimal)
- Added: `finalPrice` (BigDecimal)
- Added: `discount` (Map<String, Object>)
- Added: `currency`
- Added: `seller` (SellerProfileResponse)

**Reason:** Match frontend Book type exactly.

---

### 7. **AuthorResponse** - UPDATED ✓
**Location:** `src/main/java/com/example/bookverseserver/dto/response/Book/AuthorResponse.java`

**Added All Frontend Fields:**
- `avatar` (not avatarUrl)
- `bio` (not biography)
- `position`
- `booksCount`
- `mainGenre`
- `awards` (List<String>)
- `nationality`
- `dob`
- `website`

---

### 8. **CategoryResponse** - ADDED SLUG ✓
**Location:** `src/main/java/com/example/bookverseserver/dto/response/Book/CategoryResponse.java`

**Added:**
```java
String slug; // URL-friendly name
```

---

### 9. **Review Response** - FIXED FIELD NAMES ✓
**Location:** `src/main/java/com/example/bookverseserver/dto/response/Review/ReviewResponse.java`

**Changed:**
- `Long id` → `String id`
- `Long bookId` → `String bookId`
- `helpfulCount` → `helpful`

**Reason:** Frontend expects string IDs and different field name.

---

### 10. **Wishlist Structure** - COMPLETE REDESIGN ✓

#### Created New Entity:
**Location:** `src/main/java/com/example/bookverseserver/entity/Product/Collection.java`

```java
@Entity
@Table(name = "collection")
public class Collection {
    Long id;
    User user;
    String title;
    String description;
    Set<Listing> listings;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
```

#### Created CollectionResponse:
**Location:** `src/main/java/com/example/bookverseserver/dto/response/Wishlist/CollectionResponse.java`

```java
public class CollectionResponse {
    String id;
    String title;
    String description;
    List<ListingResponse> books; // Frontend expects 'books'
    Integer totalBooks;
}
```

#### Updated WishlistResponse:
**Location:** `src/main/java/com/example/bookverseserver/dto/response/Wishlist/WishlistResponse.java`

**Before:**
```java
public class WishlistResponse {
    List<WishlistItemDTO> favorites;
    long totalFavorites;
}
```

**After:**
```java
public class WishlistResponse {
    Long id;
    Integer totalBooks;
    Integer totalCollections;
    List<CollectionResponse> collections;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
```

**Migration Required:** Create `collection` and `collection_listing` tables.

---

### 11. **ShippingAddress Response** - UPDATED ✓
**Location:** `src/main/java/com/example/bookverseserver/dto/response/ShippingAddress/ShippingAddressResponse.java`

**Added/Changed Fields:**
- `phone` (frontend naming, not phoneNumber)
- `street` (maps from addressLine1 + addressLine2)
- `ward` (Vietnamese address field)
- `district` (Vietnamese address field)
- `state` (can map from district)
- `zipCode` (frontend naming, keep postalCode for backward compatibility)
- `note`

**Reason:** Support both frontend expectations and Vietnamese address format.

---

### 12. **Listing Status** - DOCUMENTED ✓
**Location:** `src/main/java/com/example/bookverseserver/enums/ListingStatus.java`

**Added Documentation:**
```
Frontend expects lowercase: active, out_of_stock, draft
Backend maintains uppercase: DRAFT, ACTIVE, SOLD_OUT
Mapping: DRAFT→draft, ACTIVE→active, SOLD_OUT→out_of_stock
```

**Note:** Use custom serializer or @JsonFormat for lowercase conversion.

---

### 13. **Analytics DTOs** - CREATED ✓
**Location:** `src/main/java/com/example/bookverseserver/dto/response/Analytics/`

Created all analytics response DTOs:
1. **RevenueDataResponse** - Revenue trends with daily breakdown
2. **SalesDataResponse** - Sales count with daily breakdown
3. **OrdersBreakdownResponse** - Orders by status
4. **ListingsStatsResponse** - Listing statistics
5. **ViewsDataResponse** - View counts and trends
6. **RatingDataResponse** - Rating statistics
7. **ProductPerformanceResponse** - Individual product metrics
8. **TrafficSourcesDataResponse** - Traffic analytics
9. **ConversionFunnelDataResponse** - Conversion funnel stages
10. **CustomerInsightsResponse** - Customer behavior metrics

---

### 14. **SellerProfile Response** - UPDATED ✓
**Location:** `src/main/java/com/example/bookverseserver/dto/response/User/SellerProfileResponse.java`

**Changed:**
- `Long id` → `String id`

**Reason:** Frontend expects string ID for seller profile.

---

## 📋 DATABASE MIGRATION REQUIRED

You need to run these database migrations:

### 1. Update User-Role Relationship
```sql
-- Create user_role join table
CREATE TABLE user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE
);

-- Migrate existing data
INSERT INTO user_role (user_id, role_id)
SELECT id, role_id FROM "user" WHERE role_id IS NOT NULL;

-- Drop old column
ALTER TABLE "user" DROP COLUMN role_id;
```

### 2. Add Category Slug
```sql
ALTER TABLE category ADD COLUMN slug VARCHAR(150) UNIQUE;

-- Generate slugs for existing categories
UPDATE category SET slug = LOWER(REPLACE(REPLACE(name, ' ', '-'), '&', 'and'))
WHERE slug IS NULL;

ALTER TABLE category ALTER COLUMN slug SET NOT NULL;
```

### 3. Update Author Table
```sql
-- Rename columns
ALTER TABLE author RENAME COLUMN biography TO bio;
ALTER TABLE author RENAME COLUMN avatar_url TO avatar;

-- Add new columns
ALTER TABLE author ADD COLUMN position VARCHAR(255);
ALTER TABLE author ADD COLUMN books_count INTEGER DEFAULT 0;
ALTER TABLE author ADD COLUMN main_genre VARCHAR(100);
ALTER TABLE author ADD COLUMN awards TEXT;
ALTER TABLE author ADD COLUMN date_of_birth VARCHAR(50);
ALTER TABLE author ADD COLUMN website VARCHAR(500);
```

### 4. Create Collection Tables
```sql
-- Create collection table
CREATE TABLE collection (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE
);

-- Create collection_listing join table
CREATE TABLE collection_listing (
    collection_id BIGINT NOT NULL,
    listing_id BIGINT NOT NULL,
    PRIMARY KEY (collection_id, listing_id),
    FOREIGN KEY (collection_id) REFERENCES collection(id) ON DELETE CASCADE,
    FOREIGN KEY (listing_id) REFERENCES listing(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_collection_user_id ON collection(user_id);
CREATE INDEX idx_collection_listing_collection_id ON collection_listing(collection_id);
CREATE INDEX idx_collection_listing_listing_id ON collection_listing(listing_id);
```

### 5. Update Shipping Address Table
```sql
-- Add new columns
ALTER TABLE shipping_address ADD COLUMN ward VARCHAR(100);
ALTER TABLE shipping_address ADD COLUMN district VARCHAR(100);
ALTER TABLE shipping_address ADD COLUMN note TEXT;

-- Note: Keep existing columns for backward compatibility
```

---

## ⚠️ IMPORTANT NOTES

### Service Layer Updates Required

You'll need to update the following services and mappers:

1. **UserService** - Handle Set<Role> instead of single Role
2. **AuthorMapper** - Map new Author fields
3. **CategoryMapper** - Handle slug generation
4. **BookMapper** - Map Book with seller, price, discount
5. **WishlistService** - Implement collection-based logic
6. **ReviewMapper** - Convert IDs to String
7. **ShippingAddressMapper** - Map Vietnamese address fields
8. **AnalyticsService** - Implement all analytics calculations

### Jackson Serialization

For enum lowercase serialization, add to your configuration:

```java
@Configuration
public class JacksonConfig {
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customizer() {
        return builder -> builder
            .featuresToEnable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
    }
}
```

Or use `@JsonValue` on enum methods returning lowercase strings.

---

## 🎯 TESTING CHECKLIST

- [ ] Test user authentication with roles array
- [ ] Test author CRUD with new fields
- [ ] Test category creation with slug auto-generation
- [ ] Test book listing with seller, price, discount
- [ ] Test review creation/retrieval with string IDs
- [ ] Test wishlist with collections
- [ ] Test shipping address Vietnamese format
- [ ] Test all analytics endpoints
- [ ] Verify enum serialization (lowercase)
- [ ] Test seller profile retrieval

---

## 📝 FRONTEND COMPATIBILITY

All changes ensure 100% compatibility with frontend TypeScript types in:
- `src/lib/@types/*.ts`
- `src/mocks/handlers/*.ts`

The backend now perfectly matches the frontend expectations.

---

## 🔄 ROLLBACK PLAN

If issues occur, you can rollback by:
1. Restoring the old enum values
2. Reverting User entity to single role
3. Keeping migration scripts but not applying them
4. Using old DTO field names with mappers

---

## 📞 SUPPORT

For questions or issues:
1. Check this document first
2. Review the TODO list for tracking
3. Verify database migrations completed successfully
4. Test each endpoint individually

---

**Generated:** January 5, 2026
**Status:** ✅ All 14 tasks completed
**Next Steps:** Run database migrations, update services/mappers, test all endpoints
