-- Database Migration: Fix role_name_check constraint
-- Date: January 6, 2026
-- Purpose: Update role table CHECK constraint to match Vision spec
--          OLD: ROLE_USER, ROLE_SELLER, ROLE_ADMIN
--          NEW: USER, SELLER, MODERATOR, ADMIN, PRO_SELLER

-- ============================================
-- 1. Drop the old CHECK constraint
-- ============================================
ALTER TABLE role DROP CONSTRAINT IF EXISTS role_name_check;

-- ============================================
-- 2. Create new CHECK constraint with Vision values
-- ============================================
ALTER TABLE role ADD CONSTRAINT role_name_check 
CHECK (name IN ('USER', 'SELLER', 'MODERATOR', 'ADMIN', 'PRO_SELLER'));

-- ============================================
-- 3. Update existing role names (if any)
-- ============================================
UPDATE role SET name = 'USER' WHERE name = 'ROLE_USER';
UPDATE role SET name = 'SELLER' WHERE name = 'ROLE_SELLER';
UPDATE role SET name = 'ADMIN' WHERE name = 'ROLE_ADMIN';

-- ============================================
-- 4. Clean up: Remove any duplicate roles
-- ============================================
-- This keeps only the lowest ID for each name
DELETE FROM role 
WHERE id NOT IN (
    SELECT MIN(id) FROM role GROUP BY name
);

-- ============================================
-- 5. Verify the roles exist (will be created by app if not)
-- ============================================
-- If app fails to start, manually insert:
-- INSERT INTO role (name) VALUES ('USER') ON CONFLICT DO NOTHING;
-- INSERT INTO role (name) VALUES ('SELLER') ON CONFLICT DO NOTHING;
-- INSERT INTO role (name) VALUES ('MODERATOR') ON CONFLICT DO NOTHING;
-- INSERT INTO role (name) VALUES ('ADMIN') ON CONFLICT DO NOTHING;
