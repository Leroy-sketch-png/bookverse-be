-- Journey 2 Security Audit: Database Migration
-- P1 Security Fix #H1: Add password_changed_at column for session invalidation on password change
-- Run this migration on your PostgreSQL database

-- Add password_changed_at column to user table
ALTER TABLE "user" ADD COLUMN IF NOT EXISTS password_changed_at TIMESTAMP;

-- Add comment explaining the purpose
COMMENT ON COLUMN "user".password_changed_at IS 
    'P1 Security: Timestamp of last password change. Tokens issued before this time are rejected.';

-- Note: Existing users will have NULL password_changed_at, which means their tokens will not be 
-- invalidated retroactively. This is intentional - we only invalidate tokens after the NEXT 
-- password change.
