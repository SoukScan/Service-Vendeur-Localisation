# Modifications effectuées - Suppression du stock et validation des produits actifs

## 📋 Résumé des changements

### 1. **Suppression du champ stock_quantity**
Le champ `stockQuantity` a été supprimé de toutes les entités, DTOs, services et controllers pour simplifier la gestion. On utilise maintenant uniquement le champ `isAvailable` pour gérer la disponibilité d'un produit chez un vendor.

### 2. **Validation des produits actifs**
Avant d'ajouter ou d'afficher un produit, le système vérifie maintenant que le produit est actif (`isActive = true`) dans le microservice Product.

---

## 🔧 Fichiers modifiés

### **Entités**
- ✅ `VendorProduct.java` - Suppression de la colonne `stock_quantity`

### **DTOs**
- ✅ `AddProductRequest.java` - Suppression du champ `stockQuantity`
- ✅ `VendorProductResponse.java` - Suppression du champ `stockQuantity`

### **Services**
- ✅ `VendorProductService.java`
  - Méthode `addProductToVendor()` : Vérifie que le produit est actif avant l'ajout
  - Méthode `updateVendorProduct()` : Suppression du paramètre `stockQuantity`
  - Méthode `getVendorProducts()` : Filtre les produits inactifs
  - Méthode `getProductCatalog()` : Retourne uniquement les produits actifs
  - Méthode `searchProductCatalog()` : Retourne uniquement les produits actifs

### **Controllers**
- ✅ `VendorProductController.java`
  - Endpoint POST : Suppression du paramètre `stockQuantity`
  - Endpoint PUT : Suppression du paramètre `stockQuantity`

### **Documentation API**
- ✅ `api-requests-vendor-products.http` - Mise à jour de tous les exemples

### **Scripts SQL**
- ✅ `remove-stock-column.sql` - Script pour supprimer la colonne de la BDD

---

## 🎯 Nouvelles règles métier

### **Ajout d'un produit à un shop**
```
1. Le produit doit exister dans le catalogue (microservice Product)
2. Le produit doit être actif (isActive = true)
3. Le produit ne doit pas déjà être ajouté au vendor
4. Le vendor doit exister
```

### **Affichage du catalogue global**
```
- Seuls les produits actifs (isActive = true) sont retournés
- S'applique à : GET /catalog et GET /catalog/search
```

### **Affichage des produits d'un vendor**
```
- Seuls les produits actifs dans le catalogue sont affichés
- Les produits inactifs sont ignorés avec un log warning
- Le filtre isAvailable du vendor s'applique en plus
```

---

## 📝 Nouveaux endpoints et leur comportement

### **POST /api/vendors/{vendorId}/products**
Ajouter un produit au vendor
```json
{
  "productId": 1,
  "price": 15.50
}
```
**Validations :**
- ✅ Produit existe dans le catalogue
- ✅ Produit est actif (isActive = true)
- ✅ Produit pas déjà ajouté
- ✅ Vendor existe

### **GET /api/vendors/{vendorId}/products**
Liste des produits du vendor
**Filtres automatiques :**
- ✅ Produits actifs dans le catalogue uniquement
- ✅ Produits disponibles chez le vendor (si onlyAvailable=true)

### **GET /api/vendors/{vendorId}/products/catalog**
Catalogue global des produits
**Filtre automatique :**
- ✅ Produits actifs uniquement (isActive = true)

### **GET /api/vendors/{vendorId}/products/catalog/search?name=**
Recherche dans le catalogue
**Filtre automatique :**
- ✅ Produits actifs uniquement (isActive = true)

### **PUT /api/vendors/{vendorId}/products/{vendorProductId}**
Mise à jour d'un produit du vendor
```json
{
  "price": 16.00,
  "isAvailable": true
}
```
**Champs modifiables :**
- ✅ price (prix chez le vendor)
- ✅ isAvailable (disponibilité chez le vendor)
- ❌ ~~stockQuantity~~ (supprimé)

---

## 🗃️ Migration base de données

### **Action requise**
Exécuter le script SQL suivant sur votre base de données :

```sql
-- Supprimer la colonne stock_quantity
ALTER TABLE vendor_products DROP COLUMN IF EXISTS stock_quantity;
```

**Fichier :** `remove-stock-column.sql`

---

## ✅ Tests à effectuer

### **1. Ajouter un produit actif** ✅
```http
POST http://localhost:8081/api/vendors/1/products
{
  "productId": 1,
  "price": 15.50
}
```
**Résultat attendu :** 201 Created

### **2. Tenter d'ajouter un produit inactif** ❌
```http
POST http://localhost:8081/api/vendors/1/products
{
  "productId": 5,
  "price": 12.00
}
```
**Résultat attendu :** 500 - "Product with id 5 is not active"

### **3. Voir le catalogue (produits actifs uniquement)** ✅
```http
GET http://localhost:8081/api/vendors/1/products/catalog
```
**Résultat attendu :** Liste des produits actifs uniquement

### **4. Voir les produits d'un vendor** ✅
```http
GET http://localhost:8081/api/vendors/1/products
```
**Résultat attendu :** Liste des produits actifs et disponibles

### **5. Mettre à jour le prix** ✅
```http
PUT http://localhost:8081/api/vendors/1/products/1
{
  "price": 16.00
}
```
**Résultat attendu :** 200 OK

---

## 📊 Structure simplifiée

### **Avant**
```
VendorProduct {
  - productId
  - price
  - stockQuantity  ❌
  - isAvailable
}
```

### **Après**
```
VendorProduct {
  - productId
  - price
  - isAvailable  ✅ (seul indicateur de disponibilité)
}
```

---

## 🔍 Gestion des erreurs

### **Erreurs possibles lors de l'ajout d'un produit :**
1. ❌ "Vendor with id X not found" - Vendor inexistant
2. ❌ "Product with id X not found in catalog" - Produit inexistant
3. ❌ "Product with id X is not active" - Produit inactif (NOUVEAU)
4. ❌ "Product already added to this vendor" - Doublon

### **Comportement lors de l'affichage :**
- Les produits inactifs dans le catalogue sont automatiquement filtrés
- Un log warning est généré pour chaque produit filtré
- L'application continue sans erreur

---

## 🎓 Notes importantes

1. **isAvailable** contrôle la disponibilité chez un vendor spécifique
2. **isActive** (dans Product) contrôle l'activation globale du produit
3. Un produit peut être ajouté uniquement s'il est actif
4. Un produit inactif n'apparaît plus dans aucun catalogue
5. Le stock est géré au niveau de l'application mobile/frontend si nécessaire

---

**Date de modification :** 2025-11-17
**Version :** 1.1.0

