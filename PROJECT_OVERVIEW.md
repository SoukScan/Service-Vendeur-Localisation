# 📦 Microservice Vendor - SoukScan

## Vue d'ensemble du projet

Microservice de gestion des vendeurs/fournisseurs pour le projet SoukScan, développé avec Spring Boot et PostgreSQL.

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     SoukScan Architecture                    │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌─────────────────┐              ┌─────────────────┐       │
│  │  Product Service│              │  Vendor Service │       │
│  │   Port: 8082    │◄────────────►│   Port: 8081    │       │
│  └────────┬────────┘              └────────┬────────┘       │
│           │                                 │                │
│           │                                 │                │
│  ┌────────▼────────┐              ┌────────▼────────┐       │
│  │  product_map_db │              │   vendor_db     │       │
│  │   (PostgreSQL)  │              │   (PostgreSQL)  │       │
│  └─────────────────┘              └─────────────────┘       │
│                                                               │
│            Neon PostgreSQL Cloud Database                    │
└─────────────────────────────────────────────────────────────┘
```

---

## 📂 Structure du Projet

```
vendorms/
├── src/
│   ├── main/
│   │   ├── java/com/soukscan/vendorms/
│   │   │   ├── config/
│   │   │   │   ├── CorsConfig.java           # Configuration CORS
│   │   │   │   └── RestClientConfig.java     # Client REST pour inter-services
│   │   │   ├── controller/
│   │   │   │   └── VendorController.java     # API REST endpoints
│   │   │   ├── dto/
│   │   │   │   ├── VendorRequestDTO.java     # DTO pour les requêtes
│   │   │   │   └── VendorResponseDTO.java    # DTO pour les réponses
│   │   │   ├── entity/
│   │   │   │   └── Vendor.java               # Entité JPA
│   │   │   ├── exception/
│   │   │   │   ├── DuplicateResourceException.java
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   ├── repository/
│   │   │   │   └── VendorRepository.java     # Repository JPA
│   │   │   ├── service/
│   │   │   │   └── VendorService.java        # Logique métier
│   │   │   └── VendormsApplication.java      # Classe principale
│   │   └── resources/
│   │       ├── application.properties         # Config principale
│   │       ├── application-dev.properties     # Config développement
│   │       └── application-prod.properties    # Config production
│   └── test/
│       └── java/...
├── api-requests.http                          # Requêtes HTTP de test
├── build.bat                                  # Script de compilation
├── start.bat                                  # Script de démarrage
├── DATABASE_SETUP.md                          # Guide config DB
├── TESTING_GUIDE.md                           # Guide de test
├── QUICKSTART.md                              # Démarrage rapide
├── README.md                                  # Documentation complète
├── Dockerfile                                 # Configuration Docker
├── docker-compose.yml                         # Orchestration Docker
└── pom.xml                                    # Configuration Maven
```

---

## 🎯 Fonctionnalités Principales

### ✅ CRUD Complet
- Créer un vendeur
- Lire tous les vendeurs / un vendeur
- Mettre à jour un vendeur
- Supprimer un vendeur

### ✅ Fonctionnalités Avancées
- Filtrage par statut (actif/inactif)
- Recherche par ville
- Recherche par nom (insensible à la casse)
- Basculer le statut actif/inactif
- Validation des données (email, champs obligatoires)
- Gestion des doublons (email unique)

### ✅ Gestion des Erreurs
- 404 Not Found - Ressource inexistante
- 409 Conflict - Email déjà utilisé
- 400 Bad Request - Validation échouée
- 500 Internal Server Error - Erreur serveur

---

## 🔌 API Endpoints

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/api/vendors` | Créer un vendeur |
| GET | `/api/vendors` | Récupérer tous les vendeurs |
| GET | `/api/vendors/{id}` | Récupérer un vendeur par ID |
| GET | `/api/vendors/active` | Récupérer les vendeurs actifs |
| GET | `/api/vendors/city/{city}` | Récupérer par ville |
| GET | `/api/vendors/search?name={name}` | Rechercher par nom |
| PUT | `/api/vendors/{id}` | Mettre à jour un vendeur |
| PATCH | `/api/vendors/{id}/toggle-status` | Basculer le statut |
| DELETE | `/api/vendors/{id}` | Supprimer un vendeur |

---

## 💾 Modèle de Données - Entité Vendor

| Champ | Type | Contraintes | Description |
|-------|------|-------------|-------------|
| id | Long | PK, Auto | Identifiant unique |
| name | String | NOT NULL | Nom du vendeur |
| description | String | - | Description |
| email | String | NOT NULL, UNIQUE | Email de contact |
| phone | String | - | Téléphone |
| address | String | - | Adresse postale |
| city | String | - | Ville |
| country | String | - | Pays |
| postalCode | String | - | Code postal |
| taxId | String | - | Numéro fiscal |
| isActive | Boolean | Default: true | Statut actif |
| rating | Double | - | Note du vendeur |
| createdAt | LocalDateTime | Auto | Date de création |
| updatedAt | LocalDateTime | Auto | Dernière modification |

---

## 🛠️ Technologies Utilisées

| Technologie | Version | Usage |
|-------------|---------|-------|
| Java | 21 | Langage de programmation |
| Spring Boot | 3.5.7 | Framework backend |
| Spring Data JPA | - | Persistence des données |
| Spring Web | - | API REST |
| Spring Validation | - | Validation des données |
| PostgreSQL | - | Base de données |
| Lombok | - | Réduction du boilerplate |
| Maven | - | Gestion des dépendances |
| Neon | - | PostgreSQL Cloud |

---

## 🚀 Commandes Principales

```bash
# Compiler le projet
mvnw.cmd clean package

# Démarrer en mode dev
mvnw.cmd spring-boot:run

# Démarrer avec profil spécifique
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev

# Exécuter le JAR
java -jar target\vendorms-0.0.1-SNAPSHOT.jar

# Build Docker
docker build -t vendor-service .

# Run Docker
docker-compose up
```

---

## 🔗 Communication Inter-Services

Le microservice Vendor peut communiquer avec le microservice Product :

```java
@Autowired
private RestTemplate restTemplate;

@Value("${product.service.url}")
private String productServiceUrl;

// Exemple d'appel au service Product
public Product getProduct(Long productId) {
    return restTemplate.getForObject(
        productServiceUrl + "/" + productId, 
        Product.class
    );
}
```

---

## 📊 Exemple de Requête/Réponse

### Créer un Vendeur

**Requête :**
```http
POST http://localhost:8081/api/vendors
Content-Type: application/json

{
  "name": "SoukScan Suppliers",
  "email": "contact@soukscan.com",
  "phone": "+212522000000",
  "city": "Casablanca",
  "country": "Maroc"
}
```

**Réponse :**
```json
{
  "id": 1,
  "name": "SoukScan Suppliers",
  "email": "contact@soukscan.com",
  "phone": "+212522000000",
  "city": "Casablanca",
  "country": "Maroc",
  "isActive": true,
  "createdAt": "2025-11-07T10:30:00",
  "updatedAt": "2025-11-07T10:30:00"
}
```

---

## 🔐 Sécurité

### Implémenté
- ✅ Validation des données d'entrée
- ✅ Contrainte UNIQUE sur email
- ✅ Connexion SSL à la base de données
- ✅ CORS configuré

### À Implémenter (Phase 2)
- ⏳ Authentification JWT
- ⏳ Autorisation basée sur les rôles
- ⏳ Rate limiting
- ⏳ Chiffrement des données sensibles

---

## 📈 Évolutions Futures

1. **Authentification & Autorisation**
   - Spring Security
   - JWT tokens
   - Rôles utilisateurs

2. **Relation avec Products**
   - Association vendeur-produits
   - Gestion des stocks par vendeur

3. **Fonctionnalités Métier**
   - Système de notation
   - Historique des commandes
   - Statistiques des vendeurs

4. **Performance**
   - Cache Redis
   - Pagination des résultats
   - Indexation de la base de données

5. **Monitoring**
   - Spring Boot Actuator
   - Prometheus & Grafana
   - Logs centralisés

---

## 📚 Documentation

- **QUICKSTART.md** - Guide de démarrage rapide
- **DATABASE_SETUP.md** - Configuration de la base de données
- **TESTING_GUIDE.md** - Guide de test complet
- **api-requests.http** - Collection de requêtes HTTP

---

## 👥 Informations Projet

- **Projet :** SoukScan
- **Microservice :** Vendor Service
- **Version :** 0.0.1-SNAPSHOT
- **Port :** 8081
- **Base de données :** vendor_db (PostgreSQL/Neon)

---

**Développé avec ❤️ pour SoukScan**

