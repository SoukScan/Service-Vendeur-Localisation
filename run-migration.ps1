# PowerShell script to run database migration
# Run this after connecting to your PostgreSQL database

$sqlScript = @"
-- Add missing columns safely
DO `$`$ 
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
END `$`$;

-- Populate with default values
UPDATE price_reports 
SET reported_by_user_id = COALESCE(reported_by_user_id, 0)
WHERE reported_by_user_id IS NULL;

UPDATE price_reports 
SET reported_date = COALESCE(reported_date, DATE(reported_at))
WHERE reported_date IS NULL;

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_reported_by_user ON price_reports (reported_by_user_id);
CREATE INDEX IF NOT EXISTS idx_product_vendor ON price_reports (product_id, vendor_id);  
CREATE INDEX IF NOT EXISTS idx_reported_at ON price_reports (reported_at);

SELECT 'Database migration completed!' as status;
"@

Write-Host "Database Migration SQL Script:"
Write-Host "=============================="
Write-Host $sqlScript
Write-Host ""
Write-Host "Copy the above SQL and run it in your PostgreSQL client (pgAdmin, DBeaver, etc.)"
Write-Host "This will add the missing user tracking columns safely."