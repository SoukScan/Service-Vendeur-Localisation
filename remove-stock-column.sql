-- Script pour supprimer la colonne stock_quantity de la table vendor_products
ALTER TABLE vendor_products DROP COLUMN IF EXISTS stock_quantity;

