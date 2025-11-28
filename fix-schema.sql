-- Script pour corriger le schéma de la base de données vendor_db
-- Ce script corrige les types de colonnes qui causent des problèmes de validation

-- 1. Corriger le type de report_id dans price_reports (INTEGER -> BIGINT)
ALTER TABLE price_reports
ALTER COLUMN report_id TYPE BIGINT;

-- 2. Vérifier si la table vendors existe, sinon la créer
CREATE TABLE IF NOT EXISTS vendors (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    shop_name VARCHAR(255) NOT NULL,
    shop_address VARCHAR(500) NOT NULL,
    description TEXT,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(50),
    city VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(20),
    tax_id VARCHAR(100),
    vendor_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    shop_verification_file_path VARCHAR(500),
    verified_by_admin_id BIGINT,
    verified_at TIMESTAMP,
    declared_by_user_id BIGINT,
    is_active BOOLEAN DEFAULT TRUE,
    rating DECIMAL(3, 2) DEFAULT 0.0,
    total_reviews INTEGER DEFAULT 0,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_vendor_status CHECK (vendor_status IN ('PENDING', 'VERIFIED', 'UNVERIFIED', 'REJECTED', 'SUSPENDED')),
    CONSTRAINT chk_rating CHECK (rating >= 0.0 AND rating <= 5.0),
    CONSTRAINT chk_total_reviews CHECK (total_reviews >= 0),
    CONSTRAINT chk_latitude CHECK (latitude >= -90 AND latitude <= 90),
    CONSTRAINT chk_longitude CHECK (longitude >= -180 AND longitude <= 180)
);

-- 3. Créer les index pour vendors (si la table vient d'être créée)
CREATE INDEX IF NOT EXISTS idx_vendors_user_id ON vendors(user_id);
CREATE INDEX IF NOT EXISTS idx_vendors_email ON vendors(email);
CREATE INDEX IF NOT EXISTS idx_vendors_vendor_status ON vendors(vendor_status);
CREATE INDEX IF NOT EXISTS idx_vendors_city ON vendors(city);
CREATE INDEX IF NOT EXISTS idx_vendors_country ON vendors(country);
CREATE INDEX IF NOT EXISTS idx_vendors_is_active ON vendors(is_active);
CREATE INDEX IF NOT EXISTS idx_vendors_declared_by_user_id ON vendors(declared_by_user_id);
CREATE INDEX IF NOT EXISTS idx_vendors_shop_name ON vendors(shop_name);
CREATE INDEX IF NOT EXISTS idx_vendors_location ON vendors(latitude, longitude);

-- 4. Créer ou remplacer la fonction de mise à jour de updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 5. Créer le trigger pour updated_at sur vendors (si pas déjà existant)
DROP TRIGGER IF EXISTS update_vendors_updated_at ON vendors;
CREATE TRIGGER update_vendors_updated_at
BEFORE UPDATE ON vendors
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Afficher un message de succès
DO $$
BEGIN
    RAISE NOTICE 'Schema corrections applied successfully!';
END $$;

