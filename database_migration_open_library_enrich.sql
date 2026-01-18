-- Migration: Add table_of_contents and google_books_id columns to book_meta
-- Date: 2026-01-18
-- Purpose: Capture additional value from Open Library API

-- Add table_of_contents column for book preview feature
ALTER TABLE book_meta ADD COLUMN IF NOT EXISTS table_of_contents TEXT;

-- Add google_books_id for cross-platform linking
ALTER TABLE book_meta ADD COLUMN IF NOT EXISTS google_books_id VARCHAR(50);

-- Comment the columns for documentation
COMMENT ON COLUMN book_meta.table_of_contents IS 'JSON array of chapter titles: [{"label": "Chapter 1", "title": "..."}]';
COMMENT ON COLUMN book_meta.google_books_id IS 'Google Books ID for cross-platform linking';
