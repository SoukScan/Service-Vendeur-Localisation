# 🚀 Guide de démarrage rapide - Vendor Product Integration

## 📋 Actions à effectuer

### 1. **Migration base de données**
Avant de démarrer l'application, exécutez ce script SQL :

```sql
ALTER TABLE vendor_products DROP COLUMN IF EXISTS stock_quantity;
```

**Fichier :** `remove-stock-column.sql`

---

### 2. **Vérifier les microservices**

#### ✅ Microservice Product (Port 8082)
```bash
# Doit être démarré en premier
# URL de base : http://localhost:8082/api/products
```

#### ✅ Microservice Vendor (Port 8081)
```bash
# Démarrer après Product
cd C:\Users\MOHAMED\Desktop\vendorms\vendorms
mvn clean install -DskipTests
mvn spring-boot:run
```

---

### 3. **Tests rapides**

#### Test 1 : Voir le catalogue des produits actifs
```http
GET http://localhost:8081/api/vendors/1/products/catalog
```
**Attendu :** Liste des produits avec `isActive = true` uniquement

---

#### Test 2 : Ajouter un produit actif au vendor
```http
POST http://localhost:8081/api/vendors/1/products
Content-Type: application/json

{
  "productId": 1,
  "price": 15.50
}
```
**Attendu :** 201 Created
**Validations :**
- ✅ Produit existe
- ✅ Produit est actif
- ✅ Pas de doublon

---

#### Test 3 : Tenter d'ajouter un produit inactif
```http
POST http://localhost:8081/api/vendors/1/products
Content-Type: application/json

{
  "productId": 5,
  "price": 12.00
}
```
**Attendu :** 500 - "Product with id 5 is not active"

---

#### Test 4 : Voir les produits du vendor
```http
GET http://localhost:8081/api/vendors/1/products
```
**Attendu :** Liste des produits actifs et disponibles chez ce vendor

---

#### Test 5 : Mettre à jour un produit
```http
PUT http://localhost:8081/api/vendors/1/products/1
Content-Type: application/json

{
  "price": 16.00,
  "isAvailable": true
}
```
**Attendu :** 200 OK

---

## 🔍 Vérifications importantes

### ✅ Checklist avant de tester

- [ ] Microservice Product démarré sur port 8082
- [ ] Microservice Vendor démarré sur port 8081
- [ ] Script SQL `remove-stock-column.sql` exécuté
- [ ] Au moins un vendor créé dans la base
- [ ] Au moins un produit actif dans le catalogue Product

---

## 📊 Nouveaux comportements

### 🎯 Ajout d'un produit
**Avant :**
```json
{
  "productId": 1,
  "price": 15.50,
  "stockQuantity": 100  ❌
}
```

**Après :**
```json
{
  "productId": 1,
  "price": 15.50  ✅
}
```

### 🎯 Réponse produit
**Avant :**
```json
{
  "price": 15.50,
  "stockQuantity": 100,  ❌
  "isAvailable": true
}
```

**Après :**
```json
{
  "price": 15.50,
  "isAvailable": true  ✅
}
```

---

## 🛠️ Fichier de tests

Utilisez le fichier : `api-requests-vendor-products.http`

Ce fichier contient tous les exemples de requêtes mis à jour.

---

## ⚠️ Points d'attention

1. **Ordre de démarrage :** Product PUIS Vendor
2. **Produits actifs uniquement :** Seuls les produits avec `isActive=true` peuvent être ajoutés
3. **Pas de stock :** Le champ `stockQuantity` a été supprimé partout
4. **Disponibilité :** Utilisez `isAvailable` pour gérer la disponibilité chez un vendor

---

## 📞 En cas de problème

### Erreur : "404 on GET request"
✅ **Solution :** Vérifier que le microservice Product est démarré sur le port 8082

### Erreur : "Product is not active"
✅ **Solution :** Vérifier que le produit a `isActive=true` dans la table products

### Erreur : "Vendor not found"
✅ **Solution :** Créer un vendor d'abord avec POST /api/vendors

---

**Dernière mise à jour :** 2025-11-17

