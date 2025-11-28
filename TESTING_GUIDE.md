# Guide de Test du Microservice Vendor

## 🧪 Tests Manuels avec les Endpoints API

### Prérequis
- Le microservice doit être démarré sur le port 8081
- Utiliser un client HTTP (Postman, Insomnia, ou le fichier `api-requests.http` dans IntelliJ/VS Code)

## 📋 Scénarios de Test

### 1. Créer un nouveau vendeur ✅

**Endpoint:** `POST http://localhost:8081/api/vendors`

**Body:**
```json
{
  "name": "Test Vendor",
  "description": "Un vendeur de test",
  "email": "test@vendor.com",
  "phone": "+212600000000",
  "address": "123 Rue Test",
  "city": "Casablanca",
  "country": "Maroc",
  "postalCode": "20000",
  "taxId": "TAX123",
  "rating": 4.5
}
```

**Résultat attendu:** Status 201 Created avec les données du vendeur créé incluant l'ID

---

### 2. Tester la validation - Email invalide ❌

**Endpoint:** `POST http://localhost:8081/api/vendors`

**Body:**
```json
{
  "name": "Test Vendor",
  "email": "invalid-email",
  "phone": "+212600000000"
}
```

**Résultat attendu:** Status 400 Bad Request avec message d'erreur de validation

---

### 3. Tester la duplication - Email existant ❌

**Endpoint:** `POST http://localhost:8081/api/vendors`

Créer deux vendeurs avec le même email

**Résultat attendu:** 
- Premier appel: Status 201 Created
- Deuxième appel: Status 409 Conflict avec message "Un vendeur avec cet email existe déjà"

---

### 4. Récupérer tous les vendeurs ✅

**Endpoint:** `GET http://localhost:8081/api/vendors`

**Résultat attendu:** Status 200 OK avec liste de tous les vendeurs

---

### 5. Récupérer un vendeur par ID ✅

**Endpoint:** `GET http://localhost:8081/api/vendors/1`

**Résultat attendu:** Status 200 OK avec les détails du vendeur

---

### 6. Récupérer un vendeur inexistant ❌

**Endpoint:** `GET http://localhost:8081/api/vendors/9999`

**Résultat attendu:** Status 404 Not Found avec message "Vendeur non trouvé avec l'ID: 9999"

---

### 7. Récupérer les vendeurs actifs ✅

**Endpoint:** `GET http://localhost:8081/api/vendors/active`

**Résultat attendu:** Status 200 OK avec liste des vendeurs où isActive=true

---

### 8. Rechercher par ville ✅

**Endpoint:** `GET http://localhost:8081/api/vendors/city/Casablanca`

**Résultat attendu:** Status 200 OK avec liste des vendeurs de Casablanca

---

### 9. Rechercher par nom ✅

**Endpoint:** `GET http://localhost:8081/api/vendors/search?name=Test`

**Résultat attendu:** Status 200 OK avec liste des vendeurs dont le nom contient "Test" (insensible à la casse)

---

### 10. Mettre à jour un vendeur ✅

**Endpoint:** `PUT http://localhost:8081/api/vendors/1`

**Body:**
```json
{
  "name": "Test Vendor Updated",
  "description": "Description mise à jour",
  "email": "test@vendor.com",
  "phone": "+212611111111",
  "address": "456 Rue Nouvelle",
  "city": "Rabat",
  "country": "Maroc",
  "postalCode": "10000",
  "taxId": "TAX456",
  "rating": 4.8
}
```

**Résultat attendu:** Status 200 OK avec les données mises à jour

---

### 11. Basculer le statut d'un vendeur ✅

**Endpoint:** `PATCH http://localhost:8081/api/vendors/1/toggle-status`

**Résultat attendu:** Status 200 OK avec isActive inversé (true → false ou false → true)

---

### 12. Supprimer un vendeur ✅

**Endpoint:** `DELETE http://localhost:8081/api/vendors/1`

**Résultat attendu:** Status 204 No Content

---

## 🔍 Vérifications dans la Base de Données

Connectez-vous à votre base de données Neon et exécutez :

```sql
-- Voir tous les vendeurs
SELECT * FROM vendors ORDER BY id;

-- Compter les vendeurs actifs
SELECT COUNT(*) FROM vendors WHERE is_active = true;

-- Voir les vendeurs par ville
SELECT city, COUNT(*) as count FROM vendors GROUP BY city;

-- Vérifier les timestamps
SELECT id, name, created_at, updated_at FROM vendors;
```

## 📊 Tests de Performance (optionnel)

### Test de charge avec Apache Bench (si installé)
```bash
# 100 requêtes, 10 en parallèle
ab -n 100 -c 10 http://localhost:8081/api/vendors
```

### Test de charge avec curl (Windows)
```bash
# Créer 10 vendeurs rapidement
for /L %i in (1,1,10) do curl -X POST http://localhost:8081/api/vendors -H "Content-Type: application/json" -d "{\"name\":\"Vendor %i\",\"email\":\"vendor%i@test.com\"}"
```

## ✅ Checklist de Test Complète

- [ ] Créer un vendeur avec toutes les informations
- [ ] Créer un vendeur avec informations minimales (name + email)
- [ ] Tester validation email invalide
- [ ] Tester validation champs obligatoires manquants
- [ ] Tester duplication d'email
- [ ] Récupérer tous les vendeurs
- [ ] Récupérer un vendeur par ID
- [ ] Récupérer un vendeur inexistant (404)
- [ ] Récupérer les vendeurs actifs
- [ ] Rechercher par ville
- [ ] Rechercher par nom (case insensitive)
- [ ] Mettre à jour un vendeur
- [ ] Mettre à jour avec email déjà existant (d'un autre vendeur)
- [ ] Basculer le statut actif/inactif
- [ ] Supprimer un vendeur
- [ ] Supprimer un vendeur inexistant (404)
- [ ] Vérifier les timestamps created_at et updated_at
- [ ] Tester CORS (depuis un frontend si disponible)
- [ ] Vérifier les logs dans la console
- [ ] Vérifier les données dans la base de données Neon

## 🐛 Cas d'Erreur à Tester

1. **Données invalides**
   - Email sans @
   - Nom vide
   - Rating négatif ou > 5
   
2. **ID invalides**
   - ID négatif
   - ID avec lettres
   - ID très grand (overflow)

3. **Requêtes malformées**
   - JSON invalide
   - Content-Type manquant
   - Méthode HTTP incorrecte

## 📝 Notes

- Les timestamps sont automatiquement gérés
- L'email doit être unique dans toute la base
- Le statut isActive est true par défaut
- La recherche par nom est insensible à la casse

