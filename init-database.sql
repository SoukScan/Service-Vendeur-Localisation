-- ================================================================
-- INITIALIZATION SCRIPT FOR VENDOR SERVICE WITH POSTGIS
-- ================================================================
-- Ce script crée les tables nécessaires pour le microservice Vendor
-- avec support PostGIS pour la géolocalisation
-- ================================================================

-- Activer l'extension PostGIS (à exécuter une seule fois)
CREATE EXTENSION IF NOT EXISTS postgis;

-- Vérifier que PostGIS est bien installé
SELECT PostGIS_Version();

-- ================================================================
-- TABLE: locations
-- Stocke les vendeurs et leurs emplacements géographiques
-- ================================================================

CREATE TABLE IF NOT EXISTS locations (
    -- Identifiant unique du vendor (UUID ou string du microservice)
    vendor_id VARCHAR(50) PRIMARY KEY,

    -- Nom du vendor/emplacement
    name VARCHAR(255) NOT NULL,

    -- Adresse complète
    address TEXT,

    -- Colonne PostGIS: Stocke le point géographique (latitude, longitude)
    -- GEOMETRY(Point, 4326) = Type Point avec système de référence WGS 84 (GPS standard)
    geom GEOMETRY(Point, 4326)
);

-- Index spatial GIST pour des requêtes géographiques ultra-rapides
-- C'est cet index qui rend les recherches ST_DWithin extrêmement performantes
CREATE INDEX IF NOT EXISTS locations_geom_idx ON locations USING GIST (geom);

-- Index sur le nom pour les recherches textuelles
CREATE INDEX IF NOT EXISTS locations_name_idx ON locations (name);

-- ================================================================
-- TABLE: price_reports
-- Stocke chaque rapport de prix individuel de chaque vendor pour chaque produit
-- ================================================================

CREATE TABLE IF NOT EXISTS price_reports (
    -- ID auto-incrémenté
    report_id SERIAL PRIMARY KEY,

    -- Référence au Product Microservice (pas de FK stricte entre microservices)
    product_id VARCHAR(50) NOT NULL,

    -- Référence au vendor dans la table locations
    vendor_id VARCHAR(50) NOT NULL REFERENCES locations (vendor_id) ON DELETE CASCADE,

    -- Prix rapporté
    price DECIMAL(10, 2) NOT NULL CHECK (price > 0),

    -- Horodatage du rapport (avec timezone pour gérer les différents fuseaux)
    reported_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Contrainte unique: Un vendor ne peut pas rapporter le même prix au même instant
-- (permet les mises à jour de prix dans le temps)
CREATE UNIQUE INDEX IF NOT EXISTS price_reports_unique_idx
    ON price_reports (product_id, vendor_id, reported_at);

-- Index pour requêtes fréquentes
CREATE INDEX IF NOT EXISTS price_reports_product_idx ON price_reports (product_id);
CREATE INDEX IF NOT EXISTS price_reports_vendor_idx ON price_reports (vendor_id);
CREATE INDEX IF NOT EXISTS price_reports_reported_at_idx ON price_reports (reported_at DESC);

-- ================================================================
-- TABLE: price_avg
-- Stocke le prix moyen calculé pour chaque produit
-- ================================================================

CREATE TABLE IF NOT EXISTS price_avg (
    -- L'ID du produit sert de clé primaire
    product_id VARCHAR(50) PRIMARY KEY,

    -- Prix moyen calculé
    average_price DECIMAL(10, 2) NOT NULL CHECK (average_price >= 0),

    -- Nombre de rapports utilisés pour calculer la moyenne
    report_count INTEGER DEFAULT 0 CHECK (report_count >= 0),

    -- Date de dernière mise à jour
    last_updated TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Index sur le prix moyen pour les requêtes de tri et de filtrage
CREATE INDEX IF NOT EXISTS price_avg_price_idx ON price_avg (average_price);
CREATE INDEX IF NOT EXISTS price_avg_updated_idx ON price_avg (last_updated DESC);

-- ================================================================
-- FONCTIONS UTILITAIRES
-- ================================================================

-- Fonction pour mettre à jour automatiquement le timestamp last_updated
CREATE OR REPLACE FUNCTION update_last_updated_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.last_updated = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Trigger pour mettre à jour automatiquement price_avg.last_updated
DROP TRIGGER IF EXISTS update_price_avg_last_updated ON price_avg;
CREATE TRIGGER update_price_avg_last_updated
    BEFORE UPDATE ON price_avg
    FOR EACH ROW
    EXECUTE FUNCTION update_last_updated_column();

-- ================================================================
-- DONNÉES DE TEST (optionnel)
-- ================================================================

-- Exemple: Insertion de quelques locations au Maroc
INSERT INTO locations (vendor_id, name, address, geom) VALUES
    ('vendor-001', 'Marché Central Casablanca', 'Boulevard Mohammed V, Casablanca',
     ST_SetSRID(ST_MakePoint(-7.5898, 33.5731), 4326)),
    ('vendor-002', 'Souk Rabat', 'Avenue Hassan II, Rabat',
     ST_SetSRID(ST_MakePoint(-6.8416, 34.0209), 4326)),
    ('vendor-003', 'Souk Marrakech', 'Place Jemaa el-Fna, Marrakech',
     ST_SetSRID(ST_MakePoint(-7.9811, 31.6295), 4326))
ON CONFLICT (vendor_id) DO NOTHING;

-- Exemple: Insertion de rapports de prix
INSERT INTO price_reports (product_id, vendor_id, price) VALUES
    ('pain-blanc', 'vendor-001', 1.50),
    ('pain-blanc', 'vendor-002', 1.60),
    ('huile-olive', 'vendor-001', 85.00)
ON CONFLICT DO NOTHING;

-- Calcul des prix moyens
INSERT INTO price_avg (product_id, average_price, report_count)
SELECT
    product_id,
    AVG(price) as average_price,
    COUNT(*) as report_count
FROM price_reports
GROUP BY product_id
ON CONFLICT (product_id)
DO UPDATE SET
    average_price = EXCLUDED.average_price,
    report_count = EXCLUDED.report_count,
    last_updated = CURRENT_TIMESTAMP;

-- ================================================================
-- REQUÊTES UTILES POUR VÉRIFICATION
-- ================================================================

-- Lister toutes les tables
SELECT tablename FROM pg_tables WHERE schemaname = 'public';

-- Vérifier les locations
SELECT vendor_id, name, ST_AsText(geom) as coordinates FROM locations;

-- Vérifier les rapports de prix
SELECT * FROM price_reports ORDER BY reported_at DESC;

-- Vérifier les prix moyens
SELECT * FROM price_avg ORDER BY average_price;

-- Trouver les vendors dans un rayon de 10 km autour de Casablanca
SELECT
    vendor_id,
    name,
    ST_Distance(geom::geography, ST_SetSRID(ST_MakePoint(-7.5898, 33.5731), 4326)::geography) / 1000 as distance_km
FROM locations
WHERE ST_DWithin(
    geom::geography,
    ST_SetSRID(ST_MakePoint(-7.5898, 33.5731), 4326)::geography,
    10000
)
ORDER BY distance_km;

-- ================================================================
-- NOTES IMPORTANTES
-- ================================================================

/*
1. SRID 4326 = WGS 84 (système GPS standard)
   - Latitude: -90 à 90
   - Longitude: -180 à 180

2. Format Point: ST_MakePoint(longitude, latitude)
   - Attention: X = longitude, Y = latitude

3. Distance: ST_DWithin utilise geography pour des distances précises en mètres

4. Index GIST: Crucial pour la performance des requêtes spatiales

5. Cascade Delete: Si un vendor est supprimé, tous ses price_reports sont supprimés

6. Timestamps: TIMESTAMP WITH TIME ZONE pour gérer les fuseaux horaires
*/

