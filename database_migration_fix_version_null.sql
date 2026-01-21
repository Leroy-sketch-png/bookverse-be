-- Migration: Fix NULL version column in user_profile table
-- Date: 2026-01-22
-- Issue: Hibernate @Version column was NULL, causing NPE on profile update
-- Root cause: Column was added without default value, existing rows have NULL

-- Step 1: Set all NULL versions to 0
UPDATE user_profile SET version = 0 WHERE version IS NULL;

-- Step 2: Set default for future inserts
ALTER TABLE user_profile ALTER COLUMN version SET DEFAULT 0;

-- Step 3: Add NOT NULL constraint to prevent future nulls
ALTER TABLE user_profile ALTER COLUMN version SET NOT NULL;
