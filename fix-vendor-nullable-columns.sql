-- Migration pour rendre les colonnes email, user_id et shop_address nullable
-- Date: 2025-11-10

-- Supprimer les contraintes NOT NULL
ALTER TABLE vendors ALTER COLUMN email DROP NOT NULL;
ALTER TABLE vendors ALTER COLUMN user_id DROP NOT NULL;
ALTER TABLE vendors ALTER COLUMN shop_address DROP NOT NULL;

-- Supprimer les contraintes d'unicité si elles existent
ALTER TABLE vendors DROP CONSTRAINT IF EXISTS uk_vendors_email;
ALTER TABLE vendors DROP CONSTRAINT IF EXISTS uk_vendors_user_id;

-- Recréer les contraintes d'unicité partielle (uniquement pour les valeurs non-null)
CREATE UNIQUE INDEX IF NOT EXISTS uk_vendors_email_not_null
ON vendors(email) WHERE email IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_vendors_user_id_not_null
ON vendors(user_id) WHERE user_id IS NOT NULL;

