# API Documentation - Vendor Microservice

## Base URL
```
http://localhost:8081/api/vendors
```

## Statuts de Vendeur (VendorStatus)

| Statut | Description |
|--------|-------------|
| `PENDING` | Shop créé par un vendeur avec document, en attente de vérification admin |
| `VERIFIED` | Shop vérifié et approuvé par l'admin avec document |
| `UNVERIFIED` | Shop déclaré par un utilisateur normal sans document (comme Waze) |
| `REJECTED` | Shop rejeté par l'admin |
| `SUSPENDED` | Shop suspendu |

---

## Endpoints

### 1. Gestion des Vendors

#### 1.1 Créer un vendor
**POST** `/api/vendors`

**Request Body:**
```json
{
  "userId": 1,
  "shopName": "Nom du shop",
  "shopAddress": "Adresse complète",
  "description": "Description (optionnel)",
  "email": "contact@shop.ma",
  "phone": "+212 6 XX XX XX XX",
  "city": "Casablanca",
  "country": "Maroc",
  "postalCode": "20000",
  "taxId": "TAX123456",
  "shopVerificationFilePath": "/documents/patente.pdf",
  "declaredByUserId": null,
  "latitude": 33.5731,
  "longitude": -7.5898
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "userId": 1,
  "shopName": "Nom du shop",
  "vendorStatus": "PENDING",
  "isActive": true,
  "rating": 0.0,
  "totalReviews": 0,
  "createdAt": "2025-11-10T10:00:00",
  ...
}
```

---

#### 1.2 Récupérer tous les vendors
**GET** `/api/vendors`

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "userId": 1,
    "shopName": "Épicerie Bio",
    "vendorStatus": "VERIFIED",
    ...
  }
]
```

---

#### 1.3 Récupérer un vendor par ID
**GET** `/api/vendors/{id}`

**Parameters:**
- `id` (path) - ID du vendor

**Response:** `200 OK`
```json
{
  "id": 1,
  "userId": 1,
  "shopName": "Épicerie Bio",
  "shopAddress": "123 Rue Mohammed V",
  "vendorStatus": "VERIFIED",
  ...
}
```

**Errors:**
- `404 Not Found` - Vendor non trouvé

---

#### 1.4 Récupérer le vendor d'un utilisateur
**GET** `/api/vendors/user/{userId}`

**Parameters:**
- `userId` (path) - ID de l'utilisateur

**Response:** `200 OK`
```json
{
  "id": 1,
  "userId": 1,
  "shopName": "Épicerie Bio",
  ...
}
```

**Errors:**
- `404 Not Found` - Aucun vendor pour cet utilisateur

---

#### 1.5 Mettre à jour un vendor
**PUT** `/api/vendors/{id}`

**Parameters:**
- `id` (path) - ID du vendor

**Request Body:** (même structure que POST)

**Response:** `200 OK`

**Errors:**
- `404 Not Found` - Vendor non trouvé
- `409 Conflict` - Email déjà utilisé

---

#### 1.6 Supprimer un vendor
**DELETE** `/api/vendors/{id}`

**Parameters:**
- `id` (path) - ID du vendor

**Response:** `204 No Content`

**Errors:**
- `404 Not Found` - Vendor non trouvé

---

### 2. Filtres et Recherche

#### 2.1 Récupérer les vendors actifs
**GET** `/api/vendors/active`

**Response:** `200 OK` - Liste des vendors avec `isActive = true`

---

#### 2.2 Récupérer les vendors vérifiés
**GET** `/api/vendors/verified`

**Response:** `200 OK` - Liste des vendors avec `vendorStatus = VERIFIED`

---

#### 2.3 Récupérer les vendors en attente
**GET** `/api/vendors/pending`

**Response:** `200 OK` - Liste des vendors avec `vendorStatus = PENDING`

---

#### 2.4 Récupérer les vendors par statut
**GET** `/api/vendors/status/{status}`

**Parameters:**
- `status` (path) - PENDING, VERIFIED, UNVERIFIED, REJECTED, SUSPENDED

**Response:** `200 OK` - Liste des vendors avec le statut spécifié

---

#### 2.5 Récupérer les vendors par ville
**GET** `/api/vendors/city/{city}`

**Parameters:**
- `city` (path) - Nom de la ville

**Response:** `200 OK` - Liste des vendors dans cette ville

**Example:**
```
GET /api/vendors/city/Casablanca
```

---

#### 2.6 Rechercher des vendors par nom
**GET** `/api/vendors/search?name={name}`

**Parameters:**
- `name` (query) - Terme de recherche (insensible à la casse)

**Response:** `200 OK` - Liste des vendors dont le nom contient le terme

**Example:**
```
GET /api/vendors/search?name=épicerie
```

---

### 3. Gestion Admin

#### 3.1 Vérifier un vendor (Approuver)
**PATCH** `/api/vendors/{id}/verify?adminId={adminId}`

**Parameters:**
- `id` (path) - ID du vendor
- `adminId` (query) - ID de l'admin qui vérifie

**Response:** `200 OK`
```json
{
  "id": 1,
  "vendorStatus": "VERIFIED",
  "verifiedByAdminId": 1,
  "verifiedAt": "2025-11-10T10:30:00",
  ...
}
```

**Errors:**
- `404 Not Found` - Vendor non trouvé

---

#### 3.2 Rejeter un vendor
**PATCH** `/api/vendors/{id}/reject?adminId={adminId}`

**Parameters:**
- `id` (path) - ID du vendor
- `adminId` (query) - ID de l'admin qui rejette

**Response:** `200 OK`
```json
{
  "id": 1,
  "vendorStatus": "REJECTED",
  "verifiedByAdminId": 1,
  "isActive": false,
  ...
}
```

**Errors:**
- `404 Not Found` - Vendor non trouvé

---

#### 3.3 Activer/Désactiver un vendor
**PATCH** `/api/vendors/{id}/toggle-status`

**Parameters:**
- `id` (path) - ID du vendor

**Response:** `200 OK`
```json
{
  "id": 1,
  "isActive": false,
  ...
}
```

---

## Modèles de données

### VendorRequestDTO (Création/Mise à jour)
```json
{
  "userId": "number (required)",
  "shopName": "string (required)",
  "shopAddress": "string (required)",
  "description": "string (optional)",
  "email": "string (required, email format)",
  "phone": "string (optional)",
  "city": "string (optional)",
  "country": "string (optional)",
  "postalCode": "string (optional)",
  "taxId": "string (optional)",
  "shopVerificationFilePath": "string (optional)",
  "declaredByUserId": "number (optional)",
  "latitude": "number (optional, -90 to 90)",
  "longitude": "number (optional, -180 to 180)"
}
```

### VendorResponseDTO (Réponse)
```json
{
  "id": "number",
  "userId": "number",
  "shopName": "string",
  "shopAddress": "string",
  "description": "string",
  "email": "string",
  "phone": "string",
  "city": "string",
  "country": "string",
  "postalCode": "string",
  "taxId": "string",
  "vendorStatus": "PENDING|VERIFIED|UNVERIFIED|REJECTED|SUSPENDED",
  "shopVerificationFilePath": "string",
  "verifiedByAdminId": "number",
  "verifiedAt": "datetime",
  "declaredByUserId": "number",
  "isActive": "boolean",
  "rating": "number (0.0 to 5.0)",
  "totalReviews": "number",
  "latitude": "number",
  "longitude": "number",
  "createdAt": "datetime",
  "updatedAt": "datetime"
}
```

---

## Codes d'erreur

| Code | Description |
|------|-------------|
| `200` | Succès |
| `201` | Créé avec succès |
| `204` | Supprimé avec succès (pas de contenu) |
| `400` | Requête invalide (validation échouée) |
| `404` | Ressource non trouvée |
| `409` | Conflit (email ou userId déjà existant) |
| `500` | Erreur serveur |

---

## Validation des champs

### Champs obligatoires (création)
- `userId` - Doit exister dans le microservice Auth
- `shopName` - Ne doit pas être vide
- `shopAddress` - Ne doit pas être vide
- `email` - Format email valide, unique

### Contraintes
- `userId` - Unique (un utilisateur = un shop)
- `email` - Unique
- `rating` - Entre 0.0 et 5.0
- `latitude` - Entre -90 et 90
- `longitude` - Entre -180 et 180

---

## Exemples de flux

### Flux 1 : Vendeur crée son shop
```
1. POST /api/vendors (avec shopVerificationFilePath)
   → Status: PENDING

2. Admin vérifie
   PATCH /api/vendors/{id}/verify?adminId=1
   → Status: VERIFIED

3. Shop visible comme vérifié
   GET /api/vendors/verified
```

### Flux 2 : Utilisateur déclare un shop
```
1. POST /api/vendors (avec declaredByUserId)
   → Status: UNVERIFIED

2. Shop visible avec badge "non vérifié"
   GET /api/vendors/status/UNVERIFIED
```

### Flux 3 : Admin rejette un shop
```
1. POST /api/vendors (avec shopVerificationFilePath)
   → Status: PENDING

2. Admin rejette
   PATCH /api/vendors/{id}/reject?adminId=1
   → Status: REJECTED, isActive: false
```

---

## CORS

Le service accepte les requêtes de :
- `http://localhost:3000`
- `http://localhost:4200`
- `http://localhost:8080`
- `http://localhost:8082`

Méthodes autorisées : GET, POST, PUT, PATCH, DELETE, OPTIONS

