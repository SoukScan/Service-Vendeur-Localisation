# 🚀 Quick Shop Report - Signalement rapide de produits

## 📋 Fonctionnalité

Cette API permet aux utilisateurs de signaler rapidement un produit avec son prix en cliquant sur une position sur la carte, similaire à Waze.

### 🎯 Comportement intelligent

1. **Shop existant à proximité (< 50m par défaut)**
   - ✅ Utilise le shop existant
   - ✅ Ajoute l'utilisateur comme déclarant
   - ✅ Ajoute le produit au shop (ou met à jour le prix)

2. **Pas de shop à proximité**
   - ✅ Crée automatiquement un nouveau shop
   - ✅ Ajoute l'utilisateur comme déclarant
   - ✅ Ajoute le produit avec le prix

---

## 🔧 Endpoint

### POST `/api/quick-report`

Signaler un produit avec son prix à un emplacement.

---

## 📝 Request Body

```json
{
  "productId": 1,                    // OBLIGATOIRE - ID du produit
  "price": 15.50,                    // OBLIGATOIRE - Prix du produit
  "latitude": 33.589886,             // OBLIGATOIRE - Latitude GPS
  "longitude": -7.603869,            // OBLIGATOIRE - Longitude GPS
  "userId": 101,                     // OBLIGATOIRE - ID de l'utilisateur
  "searchRadiusMeters": 50,          // OPTIONNEL - Rayon de recherche (défaut: 50m)
  "shopName": "Épicerie du coin",    // OPTIONNEL - Nom du shop si création
  "shopAddress": "Avenue Mohammed V", // OPTIONNEL - Adresse
  "city": "Casablanca",              // OPTIONNEL - Ville
  "country": "Maroc"                 // OPTIONNEL - Pays
}
```

### Champs obligatoires
- ✅ `productId` - Le produit doit exister et être actif
- ✅ `price` - Prix déclaré par l'utilisateur
- ✅ `latitude` - Position GPS (latitude)
- ✅ `longitude` - Position GPS (longitude)
- ✅ `userId` - Utilisateur qui fait le signalement

### Champs optionnels
- `searchRadiusMeters` - Défaut: 50 mètres
- `shopName` - Si non fourni: "Shop {timestamp}"
- `shopAddress` - Adresse du shop
- `city` - Ville
- `country` - Pays

---

## 📤 Response

### Cas 1 : Nouveau shop créé (201 Created)
```json
{
  "vendorId": 5,
  "shopName": "Épicerie du coin",
  "latitude": 33.589886,
  "longitude": -7.603869,
  "vendorProductId": 12,
  "productId": 1,
  "price": 15.50,
  "isNewShop": true,
  "isNewProduct": true,
  "message": "Nouveau shop créé avec succès"
}
```

### Cas 2 : Shop existant, nouveau produit ajouté (200 OK)
```json
{
  "vendorId": 3,
  "shopName": "Supermarché Atlas",
  "latitude": 33.589890,
  "longitude": -7.603870,
  "vendorProductId": 15,
  "productId": 2,
  "price": 8.00,
  "isNewShop": false,
  "isNewProduct": true,
  "message": "Produit ajouté au shop existant"
}
```

### Cas 3 : Shop existant, prix mis à jour (200 OK)
```json
{
  "vendorId": 3,
  "shopName": "Supermarché Atlas",
  "latitude": 33.589886,
  "longitude": -7.603869,
  "vendorProductId": null,
  "productId": 1,
  "price": 16.50,
  "isNewShop": false,
  "isNewProduct": false,
  "message": "Prix mis à jour pour le shop existant"
}
```

---

## 🎯 Exemples d'utilisation

### Exemple 1 : Signaler un produit (première fois)
```http
POST http://localhost:8081/api/quick-report
Content-Type: application/json

{
  "productId": 1,
  "price": 15.50,
  "latitude": 33.589886,
  "longitude": -7.603869,
  "userId": 101,
  "searchRadiusMeters": 50,
  "shopName": "Épicerie du quartier"
}
```
**Résultat :** Nouveau shop créé + produit ajouté

---

### Exemple 2 : Signaler à proximité d'un shop existant
```http
POST http://localhost:8081/api/quick-report
Content-Type: application/json

{
  "productId": 2,
  "price": 8.00,
  "latitude": 33.589890,
  "longitude": -7.603870,
  "userId": 102,
  "searchRadiusMeters": 50
}
```
**Résultat :** Shop existant utilisé + produit ajouté

---

### Exemple 3 : Mettre à jour un prix
```http
POST http://localhost:8081/api/quick-report
Content-Type: application/json

{
  "productId": 1,
  "price": 16.00,
  "latitude": 33.589886,
  "longitude": -7.603869,
  "userId": 103,
  "searchRadiusMeters": 50
}
```
**Résultat :** Prix mis à jour dans le shop existant

---

## 🔍 Algorithme de recherche de proximité

### Formule de Haversine
Le système utilise la formule de Haversine pour calculer la distance entre deux points GPS.

```
Distance = R × c

où:
- R = 6371000 (rayon de la Terre en mètres)
- c = 2 × arctan2(√a, √(1−a))
- a = sin²(Δlat/2) + cos(lat1) × cos(lat2) × sin²(Δlon/2)
```

### Rayon par défaut
- **50 mètres** - Précision suffisante pour distinguer les shops
- Configurable via `searchRadiusMeters`

### Sélection du shop
- Si plusieurs shops sont dans le rayon, le **plus proche** est sélectionné
- Logs de la distance pour debugging

---

## ✅ Validations

### 1. Produit existe et est actif
```
❌ Product with id X not found
❌ Product with id X is not active
```

### 2. Coordonnées GPS valides
```
❌ La latitude est obligatoire
❌ La longitude est obligatoire
```

### 3. Utilisateur fourni
```
❌ L'ID de l'utilisateur est obligatoire
```

---

## 🎓 Cas d'usage

### 📱 Scénario mobile typique

1. **Utilisateur sélectionne un produit** dans le catalogue
   ```
   GET /api/vendors/1/products/catalog
   ```

2. **Utilisateur clique sur la carte** pour définir l'emplacement
   ```
   Récupération GPS: lat=33.589886, lng=-7.603869
   ```

3. **Utilisateur entre le prix** payé
   ```
   Prix: 15.50 MAD
   ```

4. **Soumission du signalement**
   ```
   POST /api/quick-report
   {
     "productId": 1,
     "price": 15.50,
     "latitude": 33.589886,
     "longitude": -7.603869,
     "userId": 101
   }
   ```

5. **Système traite automatiquement**
   - Recherche shop à proximité
   - Crée ou utilise shop existant
   - Ajoute/met à jour le produit

---

## 🔐 Avantages

### ✅ Simplicité
- Pas besoin de remplir tous les champs du shop
- Création automatique si nécessaire

### ✅ Collaboration
- Plusieurs utilisateurs peuvent contribuer au même shop
- Liste des déclarants automatiquement gérée

### ✅ Précision
- Recherche de proximité intelligente
- Évite les doublons de shops

### ✅ Performance
- Une seule requête pour tout faire
- Transactions atomiques

---

## 📊 Workflow

```
┌─────────────────┐
│ Utilisateur     │
│ sélectionne     │
│ produit         │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Utilisateur     │
│ clique sur      │
│ carte (GPS)     │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Utilisateur     │
│ entre prix      │
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────┐
│ POST /api/quick-report          │
└────────┬────────────────────────┘
         │
         ▼
    ┌────────┐
    │ Système│
    │ cherche│
    │ shop   │
    │ proche │
    └───┬────┘
        │
   ┌────┴─────┐
   │          │
   ▼          ▼
Trouvé    Pas trouvé
   │          │
   ▼          ▼
Utilise    Crée
 shop       shop
   │          │
   └────┬─────┘
        │
        ▼
   Ajoute/MAJ
    produit
        │
        ▼
    ┌────────┐
    │Réponse │
    └────────┘
```

---

## 🛠️ Tests

Utilisez le fichier : **`api-requests-quick-report.http`**

---

## 📞 Support

### Erreurs communes

1. **"Product not found"**
   - Vérifier que le produit existe dans le microservice Product
   
2. **"Product is not active"**
   - Vérifier que `isActive = true` pour le produit

3. **Validation errors**
   - Vérifier que tous les champs obligatoires sont fournis

---

**Version :** 1.0.0  
**Date :** 2025-11-17

