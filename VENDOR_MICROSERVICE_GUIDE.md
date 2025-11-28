# Guide du Microservice Vendor - SoukScan

## 📋 Vue d'ensemble

Le microservice **Vendor** gère les vendeurs et leurs shops dans l'écosystème SoukScan. Il est intégré avec le microservice d'authentification où les utilisateurs peuvent créer des comptes vendeurs.

## 🏗️ Architecture

### Entité Vendor

L'entité `Vendor` représente un shop avec les informations suivantes :

- **Informations de base** : shopName, shopAddress, description, email, phone
- **Localisation** : city, country, postalCode, latitude, longitude
- **Vérification** : vendorStatus, shopVerificationFilePath, verifiedByAdminId, verifiedAt
- **Déclaration communautaire** : declaredByUserId (pour les shops déclarés comme Waze)
- **Liens** : userId (référence vers le microservice d'authentification)
- **Métadonnées** : rating, totalReviews, isActive, createdAt, updatedAt

### Statuts de Vérification (VendorStatus)

| Statut | Description |
|--------|-------------|
| **PENDING** | Shop créé par un vendeur avec document, en attente de vérification admin |
| **VERIFIED** | Shop vérifié et approuvé par l'admin avec document |
| **UNVERIFIED** | Shop déclaré par un utilisateur normal (comme Waze) sans document |
| **REJECTED** | Shop rejeté par l'admin |
| **SUSPENDED** | Shop suspendu |

## 🔄 Flux de travail

### 1. Création d'un Shop par un Vendeur (avec document)

```http
POST /api/vendors
Content-Type: application/json

{
  "userId": 1,
  "shopName": "Épicerie du Coin",
  "shopAddress": "123 Rue Mohammed V, Casablanca",
  "description": "Épicerie de quartier",
  "email": "contact@epicerie.ma",
  "phone": "+212 6 12 34 56 78",
  "city": "Casablanca",
  "country": "Maroc",
  "postalCode": "20000",
  "shopVerificationFilePath": "/documents/verification_123.pdf",
  "latitude": 33.5731,
  "longitude": -7.5898
}
```

**Résultat** : Statut = `PENDING` (en attente de vérification admin)

### 2. Déclaration d'un Shop par un Utilisateur (comme Waze)

```http
POST /api/vendors
Content-Type: application/json

{
  "userId": 2,
  "shopName": "Supérette Express",
  "shopAddress": "456 Avenue Hassan II, Rabat",
  "email": "info@superette.ma",
  "phone": "+212 6 98 76 54 32",
  "city": "Rabat",
  "country": "Maroc",
  "declaredByUserId": 10,
  "latitude": 34.0209,
  "longitude": -6.8416
}
```

**Résultat** : Statut = `UNVERIFIED` (déclaré par la communauté)

### 3. Vérification par l'Admin

```http
PATCH /api/vendors/{id}/verify?adminId={adminId}
```

**Résultat** : Statut = `VERIFIED` (vérifié et approuvé)

### 4. Rejet par l'Admin

```http
PATCH /api/vendors/{id}/reject?adminId={adminId}
```

**Résultat** : Statut = `REJECTED`, isActive = `false`

## 🛠️ API Endpoints

### Gestion des Vendors

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/vendors` | Créer un nouveau vendor |
| GET | `/api/vendors` | Récupérer tous les vendors |
| GET | `/api/vendors/{id}` | Récupérer un vendor par ID |
| GET | `/api/vendors/user/{userId}` | Récupérer le vendor d'un utilisateur |
| PUT | `/api/vendors/{id}` | Mettre à jour un vendor |
| DELETE | `/api/vendors/{id}` | Supprimer un vendor |
| PATCH | `/api/vendors/{id}/toggle-status` | Activer/Désactiver un vendor |

### Filtres et Recherche

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/vendors/active` | Vendors actifs |
| GET | `/api/vendors/verified` | Vendors vérifiés |
| GET | `/api/vendors/pending` | Vendors en attente de vérification |
| GET | `/api/vendors/status/{status}` | Vendors par statut |
| GET | `/api/vendors/city/{city}` | Vendors par ville |
| GET | `/api/vendors/search?name={name}` | Recherche par nom de shop |

### Gestion Admin

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| PATCH | `/api/vendors/{id}/verify?adminId={adminId}` | Vérifier un vendor |
| PATCH | `/api/vendors/{id}/reject?adminId={adminId}` | Rejeter un vendor |

## 🗄️ Configuration de la Base de Données

### Application Properties

Ajoutez ces propriétés dans `application.properties` :

```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/vendor_db
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Server Configuration
server.port=8081
```

## 🔗 Intégration avec le Microservice d'Authentification

### Communication entre Microservices

Le microservice Vendor utilise le `userId` pour référencer les utilisateurs du microservice d'authentification.

**Exemple de flux complet :**

1. **Utilisateur s'inscrit** (Microservice Auth) → Reçoit `userId`
2. **Utilisateur crée son shop** (Microservice Vendor) → Envoie `userId` + informations du shop
3. **Admin vérifie le shop** (Microservice Vendor) → Change statut à `VERIFIED`
4. **Shop apparaît comme vérifié** dans l'application

### Validation Future (Optionnelle)

Pour une intégration plus robuste, vous pouvez ajouter :
- Un appel REST au microservice Auth pour valider que le `userId` existe
- Un système de webhooks pour notifier les changements de statut
- Une synchronisation des emails entre les deux microservices

## 📊 Exemple de Données

### Shop Vérifié (créé par un vendeur)

```json
{
  "id": 1,
  "userId": 5,
  "shopName": "Marché Bio Casablanca",
  "shopAddress": "12 Rue des Orangers, Casablanca",
  "description": "Produits biologiques locaux",
  "email": "bio@marche.ma",
  "phone": "+212 5 22 12 34 56",
  "city": "Casablanca",
  "country": "Maroc",
  "postalCode": "20250",
  "vendorStatus": "VERIFIED",
  "shopVerificationFilePath": "/documents/patente_123.pdf",
  "verifiedByAdminId": 1,
  "verifiedAt": "2025-11-10T10:30:00",
  "declaredByUserId": null,
  "isActive": true,
  "rating": 4.5,
  "totalReviews": 120,
  "latitude": 33.5731,
  "longitude": -7.5898,
  "createdAt": "2025-11-01T08:00:00",
  "updatedAt": "2025-11-10T10:30:00"
}
```

### Shop Non Vérifié (déclaré par la communauté)

```json
{
  "id": 2,
  "userId": 15,
  "shopName": "Hanout Quartier",
  "shopAddress": "Avenue Hassan II, Rabat",
  "email": "hanout@example.ma",
  "phone": "+212 6 11 22 33 44",
  "city": "Rabat",
  "country": "Maroc",
  "vendorStatus": "UNVERIFIED",
  "shopVerificationFilePath": null,
  "verifiedByAdminId": null,
  "verifiedAt": null,
  "declaredByUserId": 8,
  "isActive": true,
  "rating": 3.8,
  "totalReviews": 25,
  "latitude": 34.0209,
  "longitude": -6.8416,
  "createdAt": "2025-11-05T14:20:00",
  "updatedAt": "2025-11-05T14:20:00"
}
```

## 🚀 Prochaines Étapes

1. **Tests** : Créer des tests unitaires et d'intégration
2. **Upload de documents** : Implémenter le stockage des documents de vérification
3. **Notifications** : Notifier les vendeurs lors des changements de statut
4. **API Gateway** : Intégrer avec un API Gateway pour la communication inter-microservices
5. **Sécurité** : Ajouter l'authentification JWT et les autorisations basées sur les rôles
6. **Recherche géographique** : Implémenter la recherche par proximité (latitude/longitude)

## 📝 Notes Importantes

- Un `userId` ne peut avoir qu'un seul shop (contrainte `unique` sur `userId`)
- Les shops avec statut `REJECTED` sont automatiquement désactivés
- Les shops `UNVERIFIED` peuvent être convertis en `VERIFIED` si le vendeur fournit un document
- Le champ `declaredByUserId` permet de tracer qui a déclaré le shop (pour la gamification)

