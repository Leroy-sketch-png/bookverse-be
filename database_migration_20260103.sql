-- Database Migration Script for BookVerse Backend
-- Generated: January 3, 2026
-- Purpose: Add missing fields and tables to align with frontend requirements

-- ============================================
-- 1. BookMeta Table Updates
-- ============================================
ALTER TABLE book_meta 
ADD COLUMN IF NOT EXISTS language VARCHAR(10) DEFAULT 'en',
ADD COLUMN IF NOT EXISTS average_rating DECIMAL(3, 2),
ADD COLUMN IF NOT EXISTS total_reviews INTEGER DEFAULT 0;

CREATE INDEX IF NOT EXISTS idx_book_meta_isbn ON book_meta(isbn);
CREATE INDEX IF NOT EXISTS idx_book_meta_language ON book_meta(language);

-- ============================================
-- 2. Review Table Updates
-- ============================================
ALTER TABLE review 
ADD COLUMN IF NOT EXISTS helpful_count INTEGER DEFAULT 0,
ADD COLUMN IF NOT EXISTS verified_purchase BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

-- Add unique constraint to prevent duplicate reviews
ALTER TABLE review 
ADD CONSTRAINT IF NOT EXISTS unique_user_book UNIQUE (user_id, book_id);

CREATE INDEX IF NOT EXISTS idx_review_book_id ON review(book_id);
CREATE INDEX IF NOT EXISTS idx_review_user_id ON review(user_id);

-- ============================================
-- 3. Review Helpful Tracking Table (NEW)
-- ============================================
CREATE TABLE IF NOT EXISTS review_helpful (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    review_id BIGINT NOT NULL REFERENCES review(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(user_id, review_id)
);

CREATE INDEX IF NOT EXISTS idx_review_helpful_review ON review_helpful(review_id);
CREATE INDEX IF NOT EXISTS idx_review_helpful_user ON review_helpful(user_id);

-- ============================================
-- 4. UserProfile Table Updates
-- ============================================
ALTER TABLE user_profile 
ADD COLUMN IF NOT EXISTS cover_image_url TEXT,
ADD COLUMN IF NOT EXISTS is_pro_seller BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS response_time VARCHAR(50),
ADD COLUMN IF NOT EXISTS fulfillment_rate DECIMAL(5, 2);

CREATE INDEX IF NOT EXISTS idx_user_profile_is_pro_seller ON user_profile(is_pro_seller);

-- ============================================
-- 5. ShippingAddress Table Updates
-- ============================================
ALTER TABLE shipping_address 
ADD COLUMN IF NOT EXISTS ward VARCHAR(100),
ADD COLUMN IF NOT EXISTS district VARCHAR(100),
ADD COLUMN IF NOT EXISTS note TEXT;

-- ============================================
-- 6. Listing Table Updates
-- ============================================
ALTER TABLE listing 
ADD COLUMN IF NOT EXISTS active_promotion_id BIGINT,
ADD COLUMN IF NOT EXISTS last_viewed_at TIMESTAMP;

-- Note: Foreign key constraint for active_promotion_id will be added after promotion table is created

CREATE INDEX IF NOT EXISTS idx_listing_active_promotion ON listing(active_promotion_id);
CREATE INDEX IF NOT EXISTS idx_listing_seller_status ON listing(seller_id, status);
CREATE INDEX IF NOT EXISTS idx_listing_book_meta ON listing(book_id);

-- ============================================
-- 7. Order Table Updates
-- ============================================
ALTER TABLE orders 
ADD COLUMN IF NOT EXISTS buyer_notes TEXT;

-- ============================================
-- 8. Promotion Table (NEW)
-- ============================================
CREATE TABLE IF NOT EXISTS promotion (
    id BIGSERIAL PRIMARY KEY,
    seller_id BIGINT NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    discount_percentage INTEGER NOT NULL CHECK (discount_percentage BETWEEN 0 AND 100),
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    total_revenue DECIMAL(12, 2),
    items_sold INTEGER,
    created_at TIMESTAMP DEFAULT NOW(),
    CONSTRAINT chk_promotion_dates CHECK (end_date > start_date)
);

CREATE INDEX IF NOT EXISTS idx_promotion_seller ON promotion(seller_id);
CREATE INDEX IF NOT EXISTS idx_promotion_status ON promotion(status);
CREATE INDEX IF NOT EXISTS idx_promotion_dates ON promotion(start_date, end_date);

-- ============================================
-- 9. Promotion-Listing Junction Table (NEW)
-- ============================================
CREATE TABLE IF NOT EXISTS promotion_listing (
    promotion_id BIGINT NOT NULL REFERENCES promotion(id) ON DELETE CASCADE,
    listing_id BIGINT NOT NULL REFERENCES listing(id) ON DELETE CASCADE,
    PRIMARY KEY (promotion_id, listing_id)
);

CREATE INDEX IF NOT EXISTS idx_promotion_listing_promo ON promotion_listing(promotion_id);
CREATE INDEX IF NOT EXISTS idx_promotion_listing_list ON promotion_listing(listing_id);

-- Now add the foreign key constraint to listing table
ALTER TABLE listing 
ADD CONSTRAINT IF NOT EXISTS fk_listing_promotion 
FOREIGN KEY (active_promotion_id) REFERENCES promotion(id) ON DELETE SET NULL;

-- ============================================
-- 10. Pro Seller Application Table (NEW)
-- ============================================
CREATE TABLE IF NOT EXISTS pro_seller_application (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    business_name VARCHAR(255) NOT NULL,
    business_address TEXT NOT NULL,
    business_phone VARCHAR(20) NOT NULL,
    tax_id VARCHAR(50) NOT NULL,
    business_license_number VARCHAR(50),
    business_description TEXT,
    years_in_business INTEGER,
    monthly_inventory INTEGER,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    review_notes TEXT,
    submitted_at TIMESTAMP DEFAULT NOW(),
    reviewed_at TIMESTAMP,
    reviewed_by BIGINT REFERENCES "user"(id)
);

CREATE INDEX IF NOT EXISTS idx_pro_seller_app_user ON pro_seller_application(user_id);
CREATE INDEX IF NOT EXISTS idx_pro_seller_app_status ON pro_seller_application(status);

-- ============================================
-- 11. Pro Seller Application Documents Table (NEW)
-- ============================================
CREATE TABLE IF NOT EXISTS pro_seller_documents (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL REFERENCES pro_seller_application(id) ON DELETE CASCADE,
    document_url TEXT NOT NULL,
    document_type VARCHAR(50)
);

CREATE INDEX IF NOT EXISTS idx_pro_seller_docs_app ON pro_seller_documents(application_id);

-- ============================================
-- 12. Update Existing Enums (If using PostgreSQL ENUMs)
-- ============================================
-- Note: If you're using VARCHAR for enums, these are not needed
-- If using PostgreSQL ENUM types, you may need to recreate them

-- For OrderStatus enum (if applicable)
-- ALTER TYPE order_status ADD VALUE IF NOT EXISTS 'PENDING';
-- ALTER TYPE order_status ADD VALUE IF NOT EXISTS 'PROCESSING';
-- ALTER TYPE order_status ADD VALUE IF NOT EXISTS 'SHIPPED';
-- ALTER TYPE order_status ADD VALUE IF NOT EXISTS 'DELIVERED';
-- ALTER TYPE order_status ADD VALUE IF NOT EXISTS 'CANCELLED';

-- For ListingStatus enum (if applicable)
-- ALTER TYPE listing_status ADD VALUE IF NOT EXISTS 'DRAFT';
-- ALTER TYPE listing_status ADD VALUE IF NOT EXISTS 'ACTIVE';
-- ALTER TYPE listing_status ADD VALUE IF NOT EXISTS 'OUT_OF_STOCK';

-- ============================================
-- 13. Add Computed Columns (Optional - PostgreSQL specific)
-- ============================================
-- PostgreSQL doesn't support computed columns directly, 
-- but we can create views or use triggers

-- ============================================
-- 14. Update Existing Data (If needed)
-- ============================================
-- Set default language for existing books
UPDATE book_meta 
SET language = 'en' 
WHERE language IS NULL;

-- Initialize review counts for existing books
UPDATE book_meta bm
SET total_reviews = (
    SELECT COUNT(*) 
    FROM review r 
    WHERE r.book_id = bm.id AND r.is_visible = TRUE
),
average_rating = (
    SELECT AVG(r.rating)::DECIMAL(3,2)
    FROM review r 
    WHERE r.book_id = bm.id AND r.is_visible = TRUE
);

-- ============================================
-- 15. Create Triggers for Automatic Updates
-- ============================================
-- Trigger to update BookMeta review statistics when a review is added/updated/deleted

CREATE OR REPLACE FUNCTION update_book_review_stats()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE book_meta
    SET 
        total_reviews = (
            SELECT COUNT(*) 
            FROM review 
            WHERE book_id = COALESCE(NEW.book_id, OLD.book_id) 
            AND is_visible = TRUE
        ),
        average_rating = (
            SELECT AVG(rating)::DECIMAL(3,2)
            FROM review 
            WHERE book_id = COALESCE(NEW.book_id, OLD.book_id) 
            AND is_visible = TRUE
        )
    WHERE id = COALESCE(NEW.book_id, OLD.book_id);
    
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Drop existing trigger if it exists
DROP TRIGGER IF EXISTS trg_update_book_review_stats ON review;

-- Create trigger
CREATE TRIGGER trg_update_book_review_stats
AFTER INSERT OR UPDATE OR DELETE ON review
FOR EACH ROW
EXECUTE FUNCTION update_book_review_stats();

-- ============================================
-- 16. Create Trigger to Update Review Helpful Count
-- ============================================
CREATE OR REPLACE FUNCTION update_review_helpful_count()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE review
    SET helpful_count = (
        SELECT COUNT(*) 
        FROM review_helpful 
        WHERE review_id = COALESCE(NEW.review_id, OLD.review_id)
    )
    WHERE id = COALESCE(NEW.review_id, OLD.review_id);
    
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Drop existing trigger if it exists
DROP TRIGGER IF EXISTS trg_update_review_helpful_count ON review_helpful;

-- Create trigger
CREATE TRIGGER trg_update_review_helpful_count
AFTER INSERT OR DELETE ON review_helpful
FOR EACH ROW
EXECUTE FUNCTION update_review_helpful_count();

-- ============================================
-- 17. Create View for Seller Statistics
-- ============================================
CREATE OR REPLACE VIEW v_seller_stats AS
SELECT 
    u.id as seller_id,
    up.display_name,
    up.avatar_url,
    up.rating_avg as average_rating,
    up.rating_count as total_reviews,
    COUNT(DISTINCT o.id) as total_sales,
    up.fulfillment_rate,
    up.response_time,
    up.seller_since as member_since,
    up.is_pro_seller
FROM "user" u
LEFT JOIN user_profile up ON u.id = up.user_id
LEFT JOIN listing l ON l.seller_id = u.id
LEFT JOIN order_items oi ON oi.listing_id = l.id
LEFT JOIN orders o ON oi.order_id = o.id AND o.status = 'DELIVERED'
GROUP BY u.id, up.display_name, up.avatar_url, up.rating_avg, 
         up.rating_count, up.fulfillment_rate, up.response_time, 
         up.seller_since, up.is_pro_seller;

-- ============================================
-- 18. Grant Permissions (Adjust as needed)
-- ============================================
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO your_app_user;
-- GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO your_app_user;

-- ============================================
-- 19. Verification Queries
-- ============================================
-- Uncomment to run verification

-- Check if all new columns exist
-- SELECT column_name, data_type 
-- FROM information_schema.columns 
-- WHERE table_name IN ('book_meta', 'review', 'user_profile', 'shipping_address', 'listing')
-- ORDER BY table_name, ordinal_position;

-- Check if all new tables exist
-- SELECT table_name 
-- FROM information_schema.tables 
-- WHERE table_schema = 'public' 
-- AND table_name IN ('promotion', 'promotion_listing', 'pro_seller_application', 'pro_seller_documents', 'review_helpful')
-- ORDER BY table_name;

-- ============================================
-- Migration Script Complete
-- ============================================
-- Remember to:
-- 1. Backup your database before running this script
-- 2. Test in a development environment first
-- 3. Update your JPA entities to match these changes
-- 4. Run any necessary data migrations
-- 5. Update your application.properties if needed
-- ============================================
