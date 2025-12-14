# Vendor Microservice - SoukScan 🏪

Microservice de gestion des vendeurs et leurs shops pour le projet SoukScan. Ce service gère les vendeurs vérifiés et les shops déclarés par la communauté (comme Waze).

## 🌟 Nouveautés

- ✅ **Système de vérification** : Statuts PENDING, VERIFIED, UNVERIFIED, REJECTED, SUSPENDED
- ✅ **Shops communautaires** : Les utilisateurs peuvent déclarer des shops (comme Waze)
- ✅ **Intégration Auth** : Lien avec le microservice d'authentification via `userId`
- ✅ **Géolocalisation** : Support de latitude/longitude pour recherche par proximité
- ✅ **Rating système** : Rating et nombre de reviews

## 🚀 Configuration

### Prérequis
- Java 21
- Maven 3.6+
- PostgreSQL (Neon)

### Port
- Le microservice Vendor fonctionne sur le port **8081**
- Le microservice Product fonctionne sur le port **8082**
- Le microservice Auth fonctionne sur le port **8080** (à configurer)

### Base de données
- **Database**: `vendor_db` (PostgreSQL sur Neon)
- Configuration dans `application.properties`
- Script d'initialisation : `init-vendor-database.sql`

## 📋 Endpoints API

### 🆕 Créer un shop avec document (vérifié)
```http
POST http://localhost:8081/api/vendors
Content-Type: application/json

{
    "userId": 1,
    "shopName": "Épicerie Bio",
    "shopAddress": "123 Rue Mohammed V, Casablanca",
    "description": "Produits biologiques",
    "email": "bio@shop.ma",
    "phone": "+212600000000",
    "city": "Casablanca",
    "country": "Maroc",
    "postalCode": "20000",
    "taxId": "TAX123456",
    "shopVerificationFilePath": "/documents/patente.pdf",
    "latitude": 33.5731,
    "longitude": -7.5898
}
```

### 🆕 Créer un shop déclaré (comme Waze)
```http
POST http://localhost:8081/api/vendors
Content-Type: application/json

{
    "userId": 2,
    "shopName": "Hanout Quartier",
    "shopAddress": "Avenue Hassan II, Rabat",
    "email": "hanout@example.ma",
    "city": "Rabat",
    "country": "Maroc",
    "declaredByUserId": 10,
    "latitude": 34.0209,
    "longitude": -6.8416
}
```

### Récupérer tous les vendeurs
```http
GET http://localhost:8081/api/vendors
```

### Récupérer un vendeur par ID
```http
GET http://localhost:8081/api/vendors/{id}
```

### 🆕 Récupérer le vendeur d'un utilisateur
```http
GET http://localhost:8081/api/vendors/user/{userId}
```

### Récupérer les vendeurs actifs
```http
GET http://localhost:8081/api/vendors/active
```

### 🆕 Récupérer les vendeurs vérifiés
```http
GET http://localhost:8081/api/vendors/verified
```

### 🆕 Récupérer les vendeurs en attente de vérification
```http
GET http://localhost:8081/api/vendors/pending
```

### 🆕 Récupérer les vendeurs par statut
```http
GET http://localhost:8081/api/vendors/status/{status}
```

### Récupérer les vendeurs par ville
```http
GET http://localhost:8081/api/vendors/city/{city}
```

### Rechercher des vendeurs par nom
```http
GET http://localhost:8081/api/vendors/search?name={name}
```

### Mettre à jour un vendeur
```http
PUT http://localhost:8081/api/vendors/{id}
```

### 🆕 Vérifier un vendeur (Admin)
```http
PATCH http://localhost:8081/api/vendors/{id}/verify?adminId={adminId}
```

### 🆕 Rejeter un vendeur (Admin)
```http
PATCH http://localhost:8081/api/vendors/{id}/reject?adminId={adminId}
```

### Basculer le statut d'un vendeur (actif/inactif)
```http
PATCH http://localhost:8081/api/vendors/{id}/toggle-status
```

### Supprimer un vendeur
```http
DELETE http://localhost:8081/api/vendors/{id}
```

## 📚 Documentation complète

- **Guide détaillé** : Voir `VENDOR_MICROSERVICE_GUIDE.md`
- **Résumé des changements** : Voir `VENDOR_UPDATE_SUMMARY.md`
- **Exemples de requêtes** : Voir `api-requests-vendor.http`
- **Script SQL** : Voir `init-vendor-database.sql`

## 🚀 Démarrage rapide

### 1. Initialiser la base de données
```bash
psql -U postgres -d vendor_db -f init-vendor-database.sql
```

### 2. Compiler et lancer
```bash
# Windows
start.bat

# Ou avec Maven
mvn clean install
mvn spring-boot:run
```

### 3. Tester l'API
```bash
# Le service sera disponible sur http://localhost:8081
# Utilisez les exemples dans api-requests-vendor.http
```

## 🏗️ Structure du projet

```
vendorms/
├── src/
│   ├── main/
│   │   ├── java/com/soukscan/vendorms/
│   │   │   ├── controller/
│   │   │   │   └── VendorController.java
│   │   │   ├── dto/
│   │   │   │   ├── VendorRequestDTO.java
│   │   │   │   ├── VendorResponseDTO.java
│   │   │   │   └── VendorVerificationDTO.java
│   │   │   ├── entity/
│   │   │   │   ├── Vendor.java
│   │   │   │   └── VendorStatus.java
│   │   │   ├── exception/
│   │   │   │   ├── DuplicateResourceException.java
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   └── ResourceNotFoundException.java
│   │   │   ├── repository/
│   │   │   │   └── VendorRepository.java
│   │   │   ├── service/
│   │   │   │   └── VendorService.java
│   │   │   └── VendormsApplication.java
│   │   └── resources/
│   │       └── application.properties
└── pom.xml
```

## 📦 Entité Vendor

| Champ | Type | Description |
|-------|------|-------------|
| id | Long | Identifiant unique (auto-généré) |
| name | String | Nom du vendeur (obligatoire) |
| description | String | Description du vendeur |
| email | String | Email (obligatoire, unique) |
| phone | String | Numéro de téléphone |
| address | String | Adresse |
| city | String | Ville |
| country | String | Pays |
| postalCode | String | Code postal |
| taxId | String | Numéro d'identification fiscale |
| isActive | Boolean | Statut actif/inactif |
| rating | Double | Note du vendeur |
| createdAt | LocalDateTime | Date de création |
| updatedAt | LocalDateTime | Date de dernière modification |

## 🔧 Dépendances principales

- Spring Boot 3.5.7
- Spring Data JPA
- Spring Web
- Spring Validation
- PostgreSQL Driver
- Lombok

## 🚦 Démarrer le microservice

### Avec Maven
```bash
mvnw.cmd spring-boot:run
```

### Ou compiler et exécuter
```bash
mvnw.cmd clean package
java -jar target/vendorms-0.0.1-SNAPSHOT.jar
```

## 🔗 Communication inter-microservices

Le microservice Vendor peut communiquer avec le microservice Product via :
- **Product Service URL**: `http://localhost:8082/api/products`

## ⚠️ Gestion des erreurs

Le microservice implémente une gestion globale des erreurs :
- **404 Not Found**: Ressource non trouvée
- **409 Conflict**: Email déjà existant
- **400 Bad Request**: Erreurs de validation
- **500 Internal Server Error**: Erreurs internes

## 📝 Notes

- Les timestamps (createdAt, updatedAt) sont gérés automatiquement par Hibernate
- La validation des données est effectuée avec Jakarta Validation
- CORS est activé pour tous les origins (à configurer selon les besoins en production)

