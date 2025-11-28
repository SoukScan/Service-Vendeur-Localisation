-- ========================================
-- Script d'initialisation de la base de données Vendor
-- Microservice Vendor - SoukScan
-- ========================================

-- Création de la base de données (si elle n'existe pas)
-- CREATE DATABASE vendor_db;

-- Connexion à la base de données
-- \c vendor_db;

-- Supprimer la table si elle existe (pour les tests)
-- DROP TABLE IF EXISTS vendors CASCADE;

-- Création de la table vendors
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

    -- Contraintes
    CONSTRAINT chk_vendor_status CHECK (vendor_status IN ('PENDING', 'VERIFIED', 'UNVERIFIED', 'REJECTED', 'SUSPENDED')),
    CONSTRAINT chk_rating CHECK (rating >= 0.0 AND rating <= 5.0),
    CONSTRAINT chk_total_reviews CHECK (total_reviews >= 0),
    CONSTRAINT chk_latitude CHECK (latitude >= -90 AND latitude <= 90),
    CONSTRAINT chk_longitude CHECK (longitude >= -180 AND longitude <= 180)
);

-- Création des index pour améliorer les performances
CREATE INDEX idx_vendors_user_id ON vendors(user_id);
CREATE INDEX idx_vendors_email ON vendors(email);
CREATE INDEX idx_vendors_vendor_status ON vendors(vendor_status);
CREATE INDEX idx_vendors_city ON vendors(city);
CREATE INDEX idx_vendors_country ON vendors(country);
CREATE INDEX idx_vendors_is_active ON vendors(is_active);
CREATE INDEX idx_vendors_declared_by_user_id ON vendors(declared_by_user_id);
CREATE INDEX idx_vendors_shop_name ON vendors(shop_name);
CREATE INDEX idx_vendors_location ON vendors(latitude, longitude);

-- Fonction pour mettre à jour automatiquement updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Trigger pour mettre à jour updated_at automatiquement
CREATE TRIGGER update_vendors_updated_at BEFORE UPDATE ON vendors
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ========================================
-- Données de test (optionnel)
-- ========================================

-- Shop vérifié avec document
INSERT INTO vendors (
    user_id, shop_name, shop_address, description, email, phone,
    city, country, postal_code, tax_id, vendor_status,
    shop_verification_file_path, is_active, rating, total_reviews,
    latitude, longitude
) VALUES (
    1,
    'Épicerie Bio Casablanca',
    '12 Rue des Orangers, Maarif, Casablanca',
    'Épicerie spécialisée dans les produits biologiques locaux et importés. Fruits, légumes, produits laitiers et épicerie sèche.',
    'contact@epiceriebio.ma',
    '+212 5 22 12 34 56',
    'Casablanca',
    'Maroc',
    '20250',
    'TAX123456',
    'VERIFIED',
    '/documents/patente_epicerie_bio.pdf',
    TRUE,
    4.7,
    150,
    33.573109,
    -7.589843
);

-- Shop vérifié avec document
INSERT INTO vendors (
    user_id, shop_name, shop_address, description, email, phone,
    city, country, postal_code, vendor_status,
    shop_verification_file_path, is_active, rating, total_reviews,
    latitude, longitude
) VALUES (
    2,
    'Boucherie Moderne Rabat',
    '45 Avenue Mohammed V, Rabat',
    'Boucherie traditionnelle et moderne. Viandes fraîches et halal.',
    'info@boucherie-moderne.ma',
    '+212 5 37 11 22 33',
    'Rabat',
    'Maroc',
    '10000',
    'VERIFIED',
    '/documents/patente_boucherie.pdf',
    TRUE,
    4.5,
    98,
    34.020882,
    -6.841650
);

-- Shop en attente de vérification
INSERT INTO vendors (
    user_id, shop_name, shop_address, description, email, phone,
    city, country, vendor_status,
    shop_verification_file_path, is_active,
    latitude, longitude
) VALUES (
    3,
    'Pharmacie Centrale',
    '78 Boulevard Hassan II, Marrakech',
    'Pharmacie ouverte 24h/24',
    'contact@pharmacie-centrale.ma',
    '+212 5 24 33 44 55',
    'Marrakech',
    'Maroc',
    'PENDING',
    '/documents/licence_pharmacie.pdf',
    TRUE,
    31.629472,
    -7.981084
);

-- Shop déclaré par un utilisateur (comme Waze) - non vérifié
INSERT INTO vendors (
    user_id, shop_name, shop_address, email, phone,
    city, country, vendor_status, declared_by_user_id,
    is_active, rating, total_reviews,
    latitude, longitude
) VALUES (
    4,
    'Hanout Sidi Moumen',
    'Quartier Sidi Moumen, Casablanca',
    'hanout.sidimoumen@example.ma',
    '+212 6 11 22 33 44',
    'Casablanca',
    'Maroc',
    'UNVERIFIED',
    10,  -- Déclaré par l'utilisateur ID 10
    TRUE,
    3.8,
    25,
    33.6072,
    -7.5503
);

-- Shop déclaré par un utilisateur - non vérifié
INSERT INTO vendors (
    user_id, shop_name, shop_address, email,
    city, country, vendor_status, declared_by_user_id,
    is_active, rating, total_reviews,
    latitude, longitude
) VALUES (
    5,
    'Supérette Al Amal',
    'Rue Al Amal, Agadir',
    'superette.amal@example.ma',
    'Agadir',
    'Maroc',
    'UNVERIFIED',
    12,  -- Déclaré par l'utilisateur ID 12
    TRUE,
    4.1,
    42,
    30.427755,
    -9.598107
);

-- Shop en attente de vérification
INSERT INTO vendors (
    user_id, shop_name, shop_address, description, email, phone,
    city, country, postal_code, vendor_status,
    shop_verification_file_path, is_active,
    latitude, longitude
) VALUES (
    6,
    'Café Restaurant Le Petit Paris',
    '23 Avenue des FAR, Tanger',
    'Restaurant français et marocain, café, pâtisserie',
    'lepetitparis@restaurant.ma',
    '+212 5 39 22 33 44',
    'Tanger',
    'Maroc',
    '90000',
    'PENDING',
    '/documents/autorisation_restaurant.pdf',
    TRUE,
    35.759465,
    -5.833954
);

-- Shop vérifié
INSERT INTO vendors (
    user_id, shop_name, shop_address, description, email, phone,
    city, country, vendor_status,
    shop_verification_file_path, is_active, rating, total_reviews,
    latitude, longitude
) VALUES (
    7,
    'Librairie Culturelle',
    '12 Rue de la Liberté, Fès',
    'Librairie, papeterie et fournitures scolaires',
    'contact@librairie-culturelle.ma',
    '+212 5 35 44 55 66',
    'Fès',
    'Maroc',
    'VERIFIED',
    '/documents/registre_commerce_librairie.pdf',
    TRUE,
    4.6,
    87,
    34.033333,
    -5.000000
);

-- ========================================
-- Requêtes utiles pour l'analyse
-- ========================================

-- Statistiques par statut
-- SELECT vendor_status, COUNT(*) as total,
--        ROUND(AVG(rating), 2) as avg_rating,
--        SUM(total_reviews) as total_reviews
-- FROM vendors
-- GROUP BY vendor_status;

-- Vendors par ville
-- SELECT city, COUNT(*) as total_vendors
-- FROM vendors
-- WHERE is_active = TRUE
-- GROUP BY city
-- ORDER BY total_vendors DESC;

-- Top vendors par rating
-- SELECT shop_name, city, rating, total_reviews
-- FROM vendors
-- WHERE is_active = TRUE AND vendor_status = 'VERIFIED'
-- ORDER BY rating DESC, total_reviews DESC
-- LIMIT 10;

-- Vendors en attente de vérification
-- SELECT id, shop_name, email, created_at
-- FROM vendors
-- WHERE vendor_status = 'PENDING'
-- ORDER BY created_at ASC;

-- Shops déclarés par la communauté
-- SELECT id, shop_name, city, declared_by_user_id, rating, total_reviews
-- FROM vendors
-- WHERE vendor_status = 'UNVERIFIED' AND declared_by_user_id IS NOT NULL
-- ORDER BY rating DESC;

