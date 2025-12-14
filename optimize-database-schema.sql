-- Database Optimization Migration Script for Enhanced Price Reporting
-- Execute these statements to optimize the database for the new features

-- 1. Add user tracking fields to price_reports table
ALTER TABLE price_reports 
ADD COLUMN IF NOT EXISTS reported_by_user_id BIGINT NOT NULL DEFAULT 0,
ADD COLUMN IF NOT EXISTS reported_date DATE NOT NULL DEFAULT CURRENT_DATE;

-- Update existing records to have a default user (you may want to set actual user IDs if available)
UPDATE price_reports SET reported_by_user_id = 0 WHERE reported_by_user_id = 0;
UPDATE price_reports SET reported_date = DATE(reported_at) WHERE reported_date = CURRENT_DATE;

-- 2. Drop old unique constraint and add new optimized one
ALTER TABLE price_reports DROP CONSTRAINT IF EXISTS unique_product_vendor_time;
ALTER TABLE price_reports ADD CONSTRAINT unique_user_product_vendor_day 
    UNIQUE (reported_by_user_id, product_id, vendor_id, reported_date);

-- 3. Add optimized indexes for price_reports
CREATE INDEX IF NOT EXISTS idx_reported_by_user ON price_reports (reported_by_user_id);
CREATE INDEX IF NOT EXISTS idx_product_vendor ON price_reports (product_id, vendor_id);
CREATE INDEX IF NOT EXISTS idx_reported_at ON price_reports (reported_at);

-- 4. Add optimized indexes for vendors table
CREATE INDEX IF NOT EXISTS idx_vendor_user_id ON vendors (user_id);
CREATE INDEX IF NOT EXISTS idx_vendor_location ON vendors (latitude, longitude);
CREATE INDEX IF NOT EXISTS idx_vendor_status ON vendors (vendor_status);
CREATE INDEX IF NOT EXISTS idx_vendor_active ON vendors (is_active);
CREATE INDEX IF NOT EXISTS idx_vendor_city ON vendors (city);
CREATE INDEX IF NOT EXISTS idx_vendor_created ON vendors (created_at);

-- 5. Add optimized indexes for vendor_products table
CREATE INDEX IF NOT EXISTS idx_vendor_product_vendor ON vendor_products (vendor_id);
CREATE INDEX IF NOT EXISTS idx_vendor_product_product ON vendor_products (product_id);
CREATE INDEX IF NOT EXISTS idx_vendor_product_available ON vendor_products (is_available);
CREATE INDEX IF NOT EXISTS idx_vendor_product_price ON vendor_products (price);
CREATE INDEX IF NOT EXISTS idx_vendor_product_updated ON vendor_products (updated_at);

-- 6. Add spatial index for locations table (PostGIS required)
CREATE INDEX IF NOT EXISTS idx_locations_geom ON locations USING GIST(geom);
CREATE INDEX IF NOT EXISTS idx_location_vendor ON locations (vendor_id);
CREATE INDEX IF NOT EXISTS idx_location_name ON locations (name);

-- 7. Add foreign key constraints for data integrity (optional, depends on microservice architecture)
-- Uncomment these if you want to enforce referential integrity within the microservice
-- ALTER TABLE price_reports ADD CONSTRAINT fk_price_report_vendor 
--     FOREIGN KEY (vendor_id) REFERENCES vendors(id) ON DELETE CASCADE;

-- 8. Add check constraints for data validation
ALTER TABLE price_reports ADD CONSTRAINT check_price_positive 
    CHECK (price > 0);

ALTER TABLE vendors ADD CONSTRAINT check_rating_range 
    CHECK (rating >= 0 AND rating <= 5);

ALTER TABLE vendors ADD CONSTRAINT check_coordinates_valid 
    CHECK (latitude IS NULL OR (latitude >= -90 AND latitude <= 90))
    CHECK (longitude IS NULL OR (longitude >= -180 AND longitude <= 180));

-- 9. Create materialized view for frequently accessed price averages (optional optimization)
CREATE MATERIALIZED VIEW IF NOT EXISTS mv_product_price_summary AS
SELECT 
    p.product_id,
    COUNT(*) as report_count,
    AVG(p.price) as average_price,
    MIN(p.price) as min_price,
    MAX(p.price) as max_price,
    MAX(p.reported_at) as last_reported,
    COUNT(DISTINCT p.vendor_id) as vendor_count,
    COUNT(DISTINCT p.reported_by_user_id) as reporter_count
FROM price_reports p
WHERE p.reported_at >= NOW() - INTERVAL '30 days'  -- Only recent reports
GROUP BY p.product_id;

-- Create unique index on the materialized view
CREATE UNIQUE INDEX IF NOT EXISTS idx_mv_product_price_summary_product 
    ON mv_product_price_summary (product_id);

-- 10. Set up automatic refresh for the materialized view (adjust schedule as needed)
-- This would typically be done via a scheduled job or trigger

-- Performance analysis queries (run these to check performance)
-- EXPLAIN ANALYZE SELECT * FROM price_reports WHERE reported_by_user_id = 123;
-- EXPLAIN ANALYZE SELECT * FROM price_reports WHERE product_id = '1' AND vendor_id = '1';
-- EXPLAIN ANALYZE SELECT * FROM vendors WHERE latitude BETWEEN 33.5 AND 33.6 AND longitude BETWEEN -7.7 AND -7.5;

COMMIT;