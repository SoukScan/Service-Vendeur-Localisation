-- URGENT DATABASE MIGRATION - Run these commands in PostgreSQL

-- Step 1: Update existing price_reports with default values
UPDATE price_reports 
SET reported_by_user_id = 0 
WHERE reported_by_user_id IS NULL;

UPDATE price_reports 
SET reported_date = DATE(reported_at) 
WHERE reported_date IS NULL;

-- Step 2: Now add the NOT NULL constraints (if columns don't exist yet)
DO $$ 
BEGIN
    -- Add reported_by_user_id column if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'price_reports' AND column_name = 'reported_by_user_id') THEN
        ALTER TABLE price_reports ADD COLUMN reported_by_user_id BIGINT DEFAULT 0 NOT NULL;
    END IF;
    
    -- Add reported_date column if it doesn't exist  
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'price_reports' AND column_name = 'reported_date') THEN
        ALTER TABLE price_reports ADD COLUMN reported_date DATE DEFAULT CURRENT_DATE NOT NULL;
    END IF;
END $$;

-- Step 3: Update existing records with proper dates
UPDATE price_reports 
SET reported_date = DATE(reported_at) 
WHERE reported_date = CURRENT_DATE AND reported_at IS NOT NULL;

-- Step 4: Add the unique constraint for anti-abuse
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                   WHERE constraint_name = 'unique_user_product_vendor_day' 
                   AND table_name = 'price_reports') THEN
        ALTER TABLE price_reports 
        ADD CONSTRAINT unique_user_product_vendor_day 
        UNIQUE (reported_by_user_id, product_id, vendor_id, reported_date);
    END IF;
END $$;

-- Step 5: Create missing indexes
CREATE INDEX IF NOT EXISTS idx_reported_by_user ON price_reports (reported_by_user_id);
CREATE INDEX IF NOT EXISTS idx_product_vendor ON price_reports (product_id, vendor_id);  
CREATE INDEX IF NOT EXISTS idx_reported_at ON price_reports (reported_at);

-- Verification queries
SELECT COUNT(*) as total_reports FROM price_reports;
SELECT COUNT(*) as reports_with_user_id FROM price_reports WHERE reported_by_user_id IS NOT NULL;
SELECT COUNT(*) as reports_with_date FROM price_reports WHERE reported_date IS NOT NULL;

-- Success message
SELECT 'Database migration completed successfully!' as status;