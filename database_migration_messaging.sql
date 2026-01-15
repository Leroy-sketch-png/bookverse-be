-- ============================================================================
-- Database Migration: Messaging System
-- Date: 2026-01-10
-- Description: Creates conversation and chat_message tables for real-time messaging
-- ============================================================================

-- ─────────────────────────────────────────────────────────────────────────────
-- CONVERSATION TABLE
-- Represents a conversation thread between a buyer and seller
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS conversation (
    id                    BIGSERIAL PRIMARY KEY,
    
    -- Participants (foreign keys to users table)
    buyer_id              BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    seller_id             BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    
    -- Denormalized participant names for quick display (avoid joins in list queries)
    buyer_name            VARCHAR(100),
    buyer_avatar          VARCHAR(500),
    seller_name           VARCHAR(100),
    seller_avatar         VARCHAR(500),
    
    -- Context (optional links to listing/order)
    listing_id            BIGINT REFERENCES listing(id) ON DELETE SET NULL,
    order_id              BIGINT REFERENCES orders(id) ON DELETE SET NULL,
    
    -- Last message preview (for inbox display)
    last_message_preview  VARCHAR(200),
    last_message_at       TIMESTAMP,
    last_sender_id        BIGINT,
    
    -- Unread tracking (per-participant)
    buyer_unread_count    INTEGER DEFAULT 0,
    seller_unread_count   INTEGER DEFAULT 0,
    
    -- Timestamps
    created_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Unique constraint: one conversation per buyer-seller-listing combination
    CONSTRAINT uq_conversation_participants_listing UNIQUE (buyer_id, seller_id, listing_id)
);

-- Index for faster inbox queries (get user's conversations)
CREATE INDEX IF NOT EXISTS idx_conversation_buyer ON conversation(buyer_id, last_message_at DESC);
CREATE INDEX IF NOT EXISTS idx_conversation_seller ON conversation(seller_id, last_message_at DESC);
CREATE INDEX IF NOT EXISTS idx_conversation_listing ON conversation(listing_id);

-- ─────────────────────────────────────────────────────────────────────────────
-- CHAT MESSAGE TABLE
-- Represents a single message within a conversation
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS chat_message (
    id                    BIGSERIAL PRIMARY KEY,
    
    -- Conversation link
    conversation_id       BIGINT NOT NULL REFERENCES conversation(id) ON DELETE CASCADE,
    
    -- Sender
    sender_id             BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    sender_name           VARCHAR(100),
    sender_avatar         VARCHAR(500),
    
    -- Message content
    message               TEXT NOT NULL,
    message_type          VARCHAR(20) DEFAULT 'TEXT', -- TEXT, LISTING_SHARE, ORDER_UPDATE, SYSTEM
    related_id            BIGINT, -- For LISTING_SHARE: listing_id, For ORDER_UPDATE: order_id
    
    -- Shared content preview (denormalized for display)
    shared_listing_title  VARCHAR(200),
    shared_listing_image  VARCHAR(500),
    shared_listing_price  DECIMAL(12, 2),
    
    -- Read tracking
    read_at               TIMESTAMP,
    
    -- Timestamps
    created_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for faster message queries
CREATE INDEX IF NOT EXISTS idx_message_conversation ON chat_message(conversation_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_message_sender ON chat_message(sender_id);

-- ─────────────────────────────────────────────────────────────────────────────
-- TRIGGER: Auto-update updated_at on conversation
-- ─────────────────────────────────────────────────────────────────────────────
CREATE OR REPLACE FUNCTION update_conversation_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_conversation_updated_at ON conversation;
CREATE TRIGGER trigger_conversation_updated_at
    BEFORE UPDATE ON conversation
    FOR EACH ROW
    EXECUTE FUNCTION update_conversation_updated_at();

-- ============================================================================
-- VERIFICATION: Check tables were created
-- ============================================================================
-- SELECT 'conversation' as table_name, COUNT(*) as row_count FROM conversation
-- UNION ALL
-- SELECT 'chat_message' as table_name, COUNT(*) as row_count FROM chat_message;
