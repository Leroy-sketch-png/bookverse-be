-- Database Migration: Add Category to Listing
-- Date: 2026-01-05
-- Purpose: Add category relationship to listing table for filtering and sorting

-- Step 1: Add category_id column to listing table
ALTER TABLE listing 
ADD COLUMN IF NOT EXISTS category_id BIGINT;

-- Step 2: Add foreign key constraint
ALTER TABLE listing 
ADD CONSTRAINT fk_listing_category 
FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE SET NULL;

-- Step 3: Create index for better query performance
CREATE INDEX IF NOT EXISTS idx_listing_category_id ON listing(category_id);

-- Step 4: Update existing listings with category based on their book's category
-- This assumes BookMeta already has category information
UPDATE listing l
SET category_id = (
    SELECT bc.category_id
    FROM book_category bc
    WHERE bc.book_meta_id = l.book_id
    LIMIT 1
)
WHERE l.category_id IS NULL
AND EXISTS (
    SELECT 1
    FROM book_category bc
    WHERE bc.book_meta_id = l.book_id
);

-- Step 5: Add comment to document the column
COMMENT ON COLUMN listing.category_id IS 'Category of the listing for filtering and sorting. Can be null if uncategorized.';

-- Verification Queries:
-- SELECT COUNT(*) as total_listings, COUNT(category_id) as categorized_listings FROM listing;
-- SELECT c.name, COUNT(l.id) as listing_count FROM listing l LEFT JOIN category c ON l.category_id = c.id GROUP BY c.name;
