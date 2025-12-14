-- COMPLETE DATABASE MIGRATION SCRIPT FOR USER TRACKING
-- Run this in your PostgreSQL database (pgAdmin, DBeaver, or any SQL client)

-- Step 1: Add the missing columns with safe defaults
DO $$ 
BEGIN
    -- Add reported_by_user_id column if it doesn't exist
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'price_reports' 
        AND column_name = 'reported_by_user_id'
        AND table_schema = 'public'
    ) THEN
        ALTER TABLE price_reports ADD COLUMN reported_by_user_id BIGINT;
        RAISE NOTICE 'Added reported_by_user_id column';
    ELSE
        RAISE NOTICE 'reported_by_user_id column already exists';
    END IF;
    
    -- Add reported_date column if it doesn't exist  
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'price_reports' 
        AND column_name = 'reported_date'
        AND table_schema = 'public'
    ) THEN
        ALTER TABLE price_reports ADD COLUMN reported_date DATE;
        RAISE NOTICE 'Added reported_date column';
    ELSE
        RAISE NOTICE 'reported_date column already exists';
    END IF;
END $$;

-- Step 2: Populate the new columns with default values for existing records
UPDATE price_reports 
SET reported_by_user_id = COALESCE(reported_by_user_id, 0)
WHERE reported_by_user_id IS NULL;

UPDATE price_reports 
SET reported_date = COALESCE(reported_date, DATE(reported_at))
WHERE reported_date IS NULL;

-- Step 3: Now make the columns NOT NULL
ALTER TABLE price_reports ALTER COLUMN reported_by_user_id SET NOT NULL;
ALTER TABLE price_reports ALTER COLUMN reported_date SET NOT NULL;

-- Step 4: Add the unique constraint for anti-abuse (drop existing if it exists)
DO $$
BEGIN
    -- Drop old constraint if it exists
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'unique_product_vendor_time'
        AND table_name = 'price_reports'
    ) THEN
        ALTER TABLE price_reports DROP CONSTRAINT unique_product_vendor_time;
        RAISE NOTICE 'Dropped old unique_product_vendor_time constraint';
    END IF;
    
    -- Add new constraint if it doesn't exist
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'unique_user_product_vendor_day' 
        AND table_name = 'price_reports'
    ) THEN
        ALTER TABLE price_reports 
        ADD CONSTRAINT unique_user_product_vendor_day 
        UNIQUE (reported_by_user_id, product_id, vendor_id, reported_date);
        RAISE NOTICE 'Added unique_user_product_vendor_day constraint';
    ELSE
        RAISE NOTICE 'unique_user_product_vendor_day constraint already exists';
    END IF;
END $$;

-- Step 5: Create optimized indexes
CREATE INDEX IF NOT EXISTS idx_reported_by_user ON price_reports (reported_by_user_id);
CREATE INDEX IF NOT EXISTS idx_product_vendor ON price_reports (product_id, vendor_id);  
CREATE INDEX IF NOT EXISTS idx_reported_at ON price_reports (reported_at);
CREATE INDEX IF NOT EXISTS idx_price_reports_date ON price_reports (reported_date);

-- Additional performance indexes for other tables
CREATE INDEX IF NOT EXISTS idx_vendor_location ON vendors (latitude, longitude);
CREATE INDEX IF NOT EXISTS idx_vendor_status ON vendors (vendor_status);
CREATE INDEX IF NOT EXISTS idx_vendor_active ON vendors (is_active);
CREATE INDEX IF NOT EXISTS idx_vendor_user_id ON vendors (user_id);

CREATE INDEX IF NOT EXISTS idx_vendor_product_vendor ON vendor_products (vendor_id);
CREATE INDEX IF NOT EXISTS idx_vendor_product_product ON vendor_products (product_id);
CREATE INDEX IF NOT EXISTS idx_vendor_product_available ON vendor_products (is_available);

-- Create spatial index for locations (if PostGIS is available)
DO $$
BEGIN
    -- Try to create spatial index, ignore if PostGIS not available
    EXECUTE 'CREATE INDEX IF NOT EXISTS idx_locations_geom ON locations USING GIST(geom)';
    RAISE NOTICE 'Created spatial index on locations.geom';
EXCEPTION
    WHEN OTHERS THEN
        RAISE NOTICE 'Could not create spatial index (PostGIS may not be available): %', SQLERRM;
END $$;

-- Step 6: Verification queries
SELECT 
    'Migration completed successfully!' as status,
    COUNT(*) as total_price_reports,
    COUNT(reported_by_user_id) as reports_with_user_id,
    COUNT(reported_date) as reports_with_date
FROM price_reports;

-- Show table structure
\d price_reports;