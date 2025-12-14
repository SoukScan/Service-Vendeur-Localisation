# 🔒 Quick Shop Report Sécurisé - Workflow en 2 Étapes

## 🎯 Objectif

**Empêcher les déclarations aléatoires** en forçant l'utilisateur à être **physiquement proche** du shop qu'il déclare.

---

## 🚀 Nouveau Workflow

### ÉTAPE 1 : Rechercher les shops à proximité

L'utilisateur envoie sa position GPS et obtient la liste des shops proches.

```http
GET /api/quick-report/nearby-shops?productId=1&lat=33.5898&lon=-7.6038&radius=50
```

**Réponse :**
```json
{
  "nearbyShops": [
    {
      "vendorId": 16,
      "shopName": "Épicerie du coin",
      "shopAddress": "Avenue Mohammed V",
      "latitude": 33.589886,
      "longitude": -7.603869,
      "distanceMeters": 15.5,
      "hasProduct": false
    }
  ],
  "count": 1,
  "canCreateNew": true,
  "searchRadiusMeters": 50.0
}
```

### ÉTAPE 2 : Signaler le produit

#### Option A : Shop existant trouvé
```http
POST /api/quick-report
Content-Type: application/json

{
  "productId": 1,
  "price": 15.50,
  "latitude": 33.589886,
  "longitude": -7.603869,
  "userId": 101,
  "vendorId": 16,          // ✅ ID du shop proche
  "searchRadiusMeters": 50
}
```

#### Option B : Aucun shop proche (créer nouveau)
```http
POST /api/quick-report
Content-Type: application/json

{
  "productId": 1,
  "price": 15.50,
  "latitude": 33.600000,
  "longitude": -7.620000,
  "userId": 101,
  "vendorId": null,        // ✅ null = créer nouveau
  "searchRadiusMeters": 50,
  "shopName": "Épicerie du quartier"
}
```

---

## 🔒 Validations de Sécurité

### ✅ Si vendorId fourni (shop existant)

**Validation STRICTE :**
```java
double distance = calculateDistance(userPosition, shopPosition);
if (distance > searchRadiusMeters) {
    throw new RuntimeException("Vous êtes trop loin du shop");
}
```

**Message d'erreur :**
```
"Vous êtes trop loin du shop (120m). Distance maximale autorisée : 50m. 
Vous devez être physiquement près du shop pour le déclarer."
```

### ✅ Si vendorId = null (nouveau shop)

**Validation :**
```java
// Vérifier qu'il n'y a AUCUN shop dans le rayon
List<Vendor> nearbyVendors = findShopsInRadius(userPosition, radius);
if (!nearbyVendors.isEmpty()) {
    throw new RuntimeException("Un shop existe déjà à proximité");
}
```

**Message d'erreur :**
```
"Un ou plusieurs shops existent déjà à proximité (2 trouvé(s)). 
Veuillez d'abord utiliser l'endpoint de recherche pour les voir."
```

---

## 🧪 Tests de Sécurité

### Test 1 : Utilisateur proche (SUCCESS ✅)
```http
# Shop à (33.589886, -7.603869)
# Utilisateur à (33.589890, -7.603870) → ~5 mètres

POST /api/quick-report
{
  "vendorId": 16,
  "latitude": 33.589890,
  "longitude": -7.603870,
  ...
}
```
**Résultat :** ✅ Accepté (distance < 50m)

---

### Test 2 : Tentative de triche (REJETÉ ❌)
```http
# Shop à (33.589886, -7.603869)
# Utilisateur à (33.600000, -7.620000) → ~2000 mètres

POST /api/quick-report
{
  "vendorId": 16,
  "latitude": 33.600000,
  "longitude": -7.620000,
  ...
}
```
**Résultat :** ❌ Rejeté
```json
{
  "error": "Vous êtes trop loin du shop (2000m). Distance maximale : 50m"
}
```

---

### Test 3 : Créer shop alors qu'il en existe un (REJETÉ ❌)
```http
# Shop existant à 20m
POST /api/quick-report
{
  "vendorId": null,  // Tente de créer nouveau
  "latitude": 33.589900,
  "longitude": -7.603880,
  ...
}
```
**Résultat :** ❌ Rejeté
```json
{
  "error": "Un ou plusieurs shops existent déjà à proximité (1 trouvé(s))"
}
```

---

## 📊 Workflow Complet (UI Mobile)

```
┌─────────────────────┐
│  1. Utilisateur     │
│  sélectionne produit│
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│  2. Utilisateur     │
│  clique sur carte   │
│  (GPS récupéré)     │
└──────────┬──────────┘
           │
           ▼
┌──────────────────────────────┐
│ 3. App appelle               │
│ GET /nearby-shops            │
│ lat=33.5898&lon=-7.6038      │
└──────────┬───────────────────┘
           │
           ▼
      ┌────┴─────┐
      │          │
  Shops      Aucun shop
  trouvés    trouvé
      │          │
      ▼          ▼
┌──────────┐  ┌──────────┐
│Afficher  │  │Demander  │
│liste     │  │nom shop  │
│shops     │  │(optionnel)│
└────┬─────┘  └────┬─────┘
     │             │
     ▼             ▼
┌────────────┐  ┌────────────┐
│Utilisateur │  │Utilisateur │
│sélectionne │  │confirme    │
│shop        │  │création    │
└────┬───────┘  └────┬───────┘
     │               │
     └───────┬───────┘
             │
             ▼
     ┌───────────────┐
     │Utilisateur    │
     │entre le prix  │
     └───────┬───────┘
             │
             ▼
     ┌─────────────────────┐
     │ POST /quick-report  │
     │ avec validation     │
     └─────────┬───────────┘
               │
               ▼
         ┌─────────┐
         │SUCCESS! │
         └─────────┘
```

---

## 🎓 Avantages du Système

### ✅ Sécurité
- Impossible de déclarer un shop à distance
- Validation GPS stricte côté backend
- Double vérification (recherche + création)

### ✅ Expérience Utilisateur
- L'utilisateur voit d'abord les shops proches
- Choix clair : utiliser existant ou créer nouveau
- Messages d'erreur explicites

### ✅ Qualité des Données
- Évite les doublons de shops
- Élimine les déclarations aléatoires
- Encourage la collaboration (plusieurs utilisateurs → même shop)

---

## 📏 Configuration du Rayon

### Par défaut : 50 mètres
```
- Assez précis pour distinguer les shops
- Assez large pour compenser l'imprécision GPS
```

### Personnalisable : 10-200 mètres
```json
{
  "searchRadiusMeters": 100  // 100m au lieu de 50m
}
```

### Recommandations
- **Zone urbaine dense** : 30-50m
- **Zone résidentielle** : 50-100m
- **Zone rurale** : 100-200m

---

## 🔧 Formule de Distance (Haversine)

```java
distance = R × c

où:
- R = 6,371,000 mètres (rayon de la Terre)
- c = 2 × arctan2(√a, √(1−a))
- a = sin²(Δlat/2) + cos(lat1) × cos(lat2) × sin²(Δlon/2)
```

**Précision :** ±1 mètre pour distances < 1km

---

## 📱 Intégration Mobile

### Permissions requises
```
- ACCESS_FINE_LOCATION (GPS précis)
- ACCESS_COARSE_LOCATION (GPS approximatif)
```

### Workflow recommandé
1. Obtenir position GPS actuelle
2. Afficher carte avec position utilisateur
3. Appeler `/nearby-shops` avec position
4. Afficher marqueurs des shops trouvés
5. Utilisateur clique sur shop OU zone vide
6. Demander prix
7. Soumettre avec validation

---

## ✅ Résumé des Changements

### Avant
- ❌ L'utilisateur pouvait déclarer n'importe où
- ❌ Pas de vérification de proximité
- ❌ Risque de déclarations aléatoires

### Maintenant
- ✅ Workflow en 2 étapes obligatoire
- ✅ Validation stricte de proximité GPS
- ✅ Messages d'erreur explicites
- ✅ Impossible de tricher

---

**Date :** 2025-11-17  
**Version :** 2.0.0 (Sécurisé)

