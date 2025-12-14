-- Migration pour supporter plusieurs déclarants par shop
-- Supprime l'ancienne colonne declared_by_user_id
-- Crée une nouvelle table vendor_declarations pour la relation many-to-many

-- Étape 1 : Créer la nouvelle table pour les déclarations
CREATE TABLE IF NOT EXISTS vendor_declarations (
    vendor_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (vendor_id, user_id),
    CONSTRAINT fk_vendor_declarations_vendor
        FOREIGN KEY (vendor_id)
        REFERENCES vendors(id)
        ON DELETE CASCADE
);

-- Étape 2 : Migrer les données existantes (si declared_by_user_id existe et n'est pas null)
INSERT INTO vendor_declarations (vendor_id, user_id)
SELECT id, declared_by_user_id
FROM vendors
WHERE declared_by_user_id IS NOT NULL;

-- Étape 3 : Supprimer l'ancienne colonne declared_by_user_id
ALTER TABLE vendors DROP COLUMN IF EXISTS declared_by_user_id;

-- Créer un index pour améliorer les performances des requêtes
CREATE INDEX IF NOT EXISTS idx_vendor_declarations_user_id ON vendor_declarations(user_id);
CREATE INDEX IF NOT EXISTS idx_vendor_declarations_vendor_id ON vendor_declarations(vendor_id);

