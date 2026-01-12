-- Migration: Add seller_settings table for persisting seller configuration
-- Date: January 12, 2026
-- Author: GitHub Copilot
-- Related: Journey 5 F2 - Migrate settings from localStorage to backend

-- ============================================================================
-- SELLER SETTINGS TABLE
-- Stores shipping preferences, notification settings, and privacy settings
-- Uses JSONB for flexible nested configuration that can evolve over time
-- ============================================================================

CREATE TABLE IF NOT EXISTS seller_settings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    
    -- Shipping preferences (JSONB for flexibility)
    shipping_settings JSONB DEFAULT '{
        "standardShipping": true,
        "expressShipping": false,
        "localPickup": false,
        "freeShippingThreshold": 200000,
        "freeShippingEnabled": false
    }'::jsonb,
    
    -- Notification preferences (JSONB for flexibility)
    notification_settings JSONB DEFAULT '{
        "emailNewOrder": true,
        "emailOrderShipped": true,
        "emailLowStock": true,
        "emailWeeklyReport": false,
        "pushNewOrder": true,
        "pushMessages": true
    }'::jsonb,
    
    -- Privacy settings (JSONB for flexibility)
    privacy_settings JSONB DEFAULT '{
        "showPhone": true,
        "allowMessages": true,
        "showStats": true
    }'::jsonb,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for fast lookups by user
CREATE INDEX IF NOT EXISTS idx_seller_settings_user_id ON seller_settings(user_id);

-- Comment for documentation
COMMENT ON TABLE seller_settings IS 'Seller-specific configuration: shipping, notifications, privacy. Created for Journey 5 F2 fix.';
COMMENT ON COLUMN seller_settings.shipping_settings IS 'Shipping preferences: standardShipping, expressShipping, localPickup, freeShippingThreshold, freeShippingEnabled';
COMMENT ON COLUMN seller_settings.notification_settings IS 'Notification preferences: emailNewOrder, emailOrderShipped, emailLowStock, emailWeeklyReport, pushNewOrder, pushMessages';
COMMENT ON COLUMN seller_settings.privacy_settings IS 'Privacy settings: showPhone, allowMessages, showStats';
