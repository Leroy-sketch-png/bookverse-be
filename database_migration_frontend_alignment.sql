-- Database Migration Script for Backend-Frontend Alignment
-- Generated: January 5, 2026
-- Purpose: Align backend database schema with frontend TypeScript types

-- ===========================================================================
-- 1. USER-ROLE RELATIONSHIP: Convert from Many-to-One to Many-to-Many
-- ===========================================================================

-- Create user_role join table
CREATE TABLE IF NOT EXISTS user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE
);

-- Migrate existing user-role data
INSERT INTO user_role (user_id, role_id)
SELECT id, role_id FROM "user" WHERE role_id IS NOT NULL
ON CONFLICT DO NOTHING;

-- Create index for better query performance
CREATE INDEX IF NOT EXISTS idx_user_role_user_id ON user_role(user_id);
CREATE INDEX IF NOT EXISTS idx_user_role_role_id ON user_role(role_id);

-- Drop old role_id column from user table
ALTER TABLE "user" DROP COLUMN IF EXISTS role_id;

-- ===========================================================================
-- 2. ROLE ENUM: Update role names to match frontend
-- ===========================================================================

-- Update existing roles to match frontend expectations
UPDATE role SET name = 'USER' WHERE name IN ('PRO', 'CASUAL', 'BUYER');

-- Keep only ADMIN and USER roles
DELETE FROM role WHERE name NOT IN ('ADMIN', 'USER');

-- ===========================================================================
-- 3. CATEGORY: Add slug field
-- ===========================================================================

-- Add slug column
ALTER TABLE category ADD COLUMN IF NOT EXISTS slug VARCHAR(150);

-- Generate slugs for existing categories (URL-friendly)
UPDATE category 
SET slug = LOWER(
    REGEXP_REPLACE(
        REGEXP_REPLACE(
            REGEXP_REPLACE(name, '[^a-zA-Z0-9\s-]', '', 'g'),
            '\s+', '-', 'g'
        ),
        '-+', '-', 'g'
    )
)
WHERE slug IS NULL;

-- Make slug unique and not null
ALTER TABLE category ALTER COLUMN slug SET NOT NULL;
ALTER TABLE category ADD CONSTRAINT uk_category_slug UNIQUE (slug);

-- ===========================================================================
-- 4. AUTHOR: Update fields to match frontend
-- ===========================================================================

-- Rename columns to match frontend expectations
ALTER TABLE author RENAME COLUMN biography TO bio;
ALTER TABLE author RENAME COLUMN avatar_url TO avatar;

-- Add new columns for comprehensive author information
ALTER TABLE author ADD COLUMN IF NOT EXISTS position VARCHAR(255);
ALTER TABLE author ADD COLUMN IF NOT EXISTS books_count INTEGER DEFAULT 0;
ALTER TABLE author ADD COLUMN IF NOT EXISTS main_genre VARCHAR(100);
ALTER TABLE author ADD COLUMN IF NOT EXISTS awards TEXT;
ALTER TABLE author ADD COLUMN IF NOT EXISTS date_of_birth VARCHAR(50);
ALTER TABLE author ADD COLUMN IF NOT EXISTS website VARCHAR(500);

-- Update avatar column length for longer URLs
ALTER TABLE author ALTER COLUMN avatar TYPE VARCHAR(1000);

-- ===========================================================================
-- 5. COLLECTION: Create new entity for wishlist collections
-- ===========================================================================

-- Create collection table
CREATE TABLE IF NOT EXISTS collection (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_collection_user FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE
);

-- Create collection_listing join table (many-to-many)
CREATE TABLE IF NOT EXISTS collection_listing (
    collection_id BIGINT NOT NULL,
    listing_id BIGINT NOT NULL,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (collection_id, listing_id),
    CONSTRAINT fk_collection_listing_collection FOREIGN KEY (collection_id) REFERENCES collection(id) ON DELETE CASCADE,
    CONSTRAINT fk_collection_listing_listing FOREIGN KEY (listing_id) REFERENCES listing(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_collection_user_id ON collection(user_id);
CREATE INDEX IF NOT EXISTS idx_collection_created_at ON collection(created_at);
CREATE INDEX IF NOT EXISTS idx_collection_listing_collection_id ON collection_listing(collection_id);
CREATE INDEX IF NOT EXISTS idx_collection_listing_listing_id ON collection_listing(listing_id);

-- ===========================================================================
-- 6. SHIPPING ADDRESS: Add Vietnamese address fields
-- ===========================================================================

-- Add ward and district columns for Vietnamese addresses
ALTER TABLE shipping_address ADD COLUMN IF NOT EXISTS ward VARCHAR(100);
ALTER TABLE shipping_address ADD COLUMN IF NOT EXISTS district VARCHAR(100);
ALTER TABLE shipping_address ADD COLUMN IF NOT EXISTS note TEXT;

-- Rename phone_number to phone for frontend compatibility
-- Note: Keep both for backward compatibility
ALTER TABLE shipping_address ADD COLUMN IF NOT EXISTS phone VARCHAR(20);
UPDATE shipping_address SET phone = phone_number WHERE phone IS NULL;

-- ===========================================================================
-- 7. BOOK_META: Ensure language field exists with default
-- ===========================================================================

-- Add language column if not exists
ALTER TABLE book_meta ADD COLUMN IF NOT EXISTS language VARCHAR(10) DEFAULT 'en';

-- ===========================================================================
-- 8. INDEXES: Add missing indexes for performance
-- ===========================================================================

-- Author indexes
CREATE INDEX IF NOT EXISTS idx_author_name ON author(name);
CREATE INDEX IF NOT EXISTS idx_author_nationality ON author(nationality);

-- Category indexes
CREATE INDEX IF NOT EXISTS idx_category_name ON category(name);

-- Book_meta indexes
CREATE INDEX IF NOT EXISTS idx_book_meta_title ON book_meta(title);
CREATE INDEX IF NOT EXISTS idx_book_meta_isbn ON book_meta(isbn);
CREATE INDEX IF NOT EXISTS idx_book_meta_language ON book_meta(language);

-- Listing indexes
CREATE INDEX IF NOT EXISTS idx_listing_status ON listing(status);
CREATE INDEX IF NOT EXISTS idx_listing_seller_id ON listing(seller_id);
CREATE INDEX IF NOT EXISTS idx_listing_created_at ON listing(created_at);

-- Review indexes
CREATE INDEX IF NOT EXISTS idx_review_book_id ON review(book_id);
CREATE INDEX IF NOT EXISTS idx_review_user_id ON review(user_id);
CREATE INDEX IF NOT EXISTS idx_review_rating ON review(rating);

-- Order indexes
CREATE INDEX IF NOT EXISTS idx_order_user_id ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_order_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_order_created_at ON orders(created_at);

-- ===========================================================================
-- 9. DATA VALIDATION: Ensure data integrity
-- ===========================================================================

-- Update empty or null slugs
UPDATE category 
SET slug = CONCAT('category-', id)
WHERE slug IS NULL OR slug = '';

-- Set default books_count for authors
UPDATE author SET books_count = 0 WHERE books_count IS NULL;

-- ===========================================================================
-- 10. COMMENTS: Add table and column comments for documentation
-- ===========================================================================

COMMENT ON TABLE collection IS 'User-created collections for organizing favorite listings (wishlist feature)';
COMMENT ON TABLE collection_listing IS 'Many-to-many relationship between collections and listings';
COMMENT ON COLUMN category.slug IS 'URL-friendly version of category name for SEO and routing';
COMMENT ON COLUMN author.avatar IS 'Author profile image URL';
COMMENT ON COLUMN author.bio IS 'Author biography text';
COMMENT ON COLUMN author.position IS 'Author role or primary genre';
COMMENT ON COLUMN author.books_count IS 'Number of books by this author';
COMMENT ON COLUMN author.main_genre IS 'Primary genre the author writes in';
COMMENT ON COLUMN author.awards IS 'JSON array or comma-separated list of awards';
COMMENT ON COLUMN author.date_of_birth IS 'Author date of birth in ISO format';
COMMENT ON COLUMN shipping_address.ward IS 'Vietnamese administrative division (phường/xã)';
COMMENT ON COLUMN shipping_address.district IS 'Vietnamese administrative division (quận/huyện)';

-- ===========================================================================
-- MIGRATION COMPLETE
-- ===========================================================================

-- Verify migration
DO $$
BEGIN
    RAISE NOTICE 'Migration completed successfully!';
    RAISE NOTICE 'Tables created: collection, collection_listing, user_role';
    RAISE NOTICE 'Columns added: category.slug, author fields, shipping_address fields';
    RAISE NOTICE 'Please run tests to verify all changes';
END $$;
