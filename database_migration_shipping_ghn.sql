-- Database migration for GHN shipping integration
-- Adds provinceId, districtId, and wardCode fields to shipping_address table

ALTER TABLE shipping_address 
ADD COLUMN IF NOT EXISTS province_id INTEGER,
ADD COLUMN IF NOT EXISTS district_id INTEGER,
ADD COLUMN IF NOT EXISTS ward_code VARCHAR(20);

-- Add comment for documentation
COMMENT ON COLUMN shipping_address.province_id IS 'GHN Province ID for shipping rate calculation';
COMMENT ON COLUMN shipping_address.district_id IS 'GHN District ID for shipping rate calculation';
COMMENT ON COLUMN shipping_address.ward_code IS 'GHN Ward Code for shipping rate calculation';
