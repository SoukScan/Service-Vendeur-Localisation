# Changelog - Vendor Service

Toutes les modifications notables de ce projet seront documentées dans ce fichier.

Le format est basé sur [Keep a Changelog](https://keepachangelog.com/fr/1.0.0/),
et ce projet adhère au [Semantic Versioning](https://semver.org/lang/fr/).

---

## [0.0.1-SNAPSHOT] - 2025-11-07

### ✨ Ajouté (Added)

#### Structure du Projet
- Initialisation du projet Spring Boot 3.5.7
- Configuration Maven avec toutes les dépendances nécessaires
- Structure en couches (Entity, Repository, Service, Controller)

#### Entités & Modèle de Données
- Création de l'entité `Vendor` avec 14 champs
- Timestamps automatiques (createdAt, updatedAt)
- Contrainte UNIQUE sur le champ email
- Support pour notation (rating) des vendeurs

#### API REST - Endpoints
- `POST /api/vendors` - Créer un nouveau vendeur
- `GET /api/vendors` - Récupérer tous les vendeurs
- `GET /api/vendors/{id}` - Récupérer un vendeur par ID
- `GET /api/vendors/active` - Récupérer les vendeurs actifs
- `GET /api/vendors/city/{city}` - Récupérer par ville
- `GET /api/vendors/search?name={name}` - Rechercher par nom
- `PUT /api/vendors/{id}` - Mettre à jour un vendeur
- `PATCH /api/vendors/{id}/toggle-status` - Basculer le statut
- `DELETE /api/vendors/{id}` - Supprimer un vendeur

#### DTOs (Data Transfer Objects)
- `VendorRequestDTO` - Validation des requêtes entrantes
- `VendorResponseDTO` - Format standardisé des réponses

#### Repository
- `VendorRepository` avec méthodes de recherche personnalisées:
  - `findByEmail(String email)`
  - `findByIsActive(Boolean isActive)`
  - `findByCity(String city)`
  - `findByCountry(String country)`
  - `findByNameContainingIgnoreCase(String name)`
  - `existsByEmail(String email)`

#### Service Layer
- Logique métier complète dans `VendorService`
- Transactions gérées avec @Transactional
- Validation des doublons (email)
- Logs structurés avec SLF4J

#### Gestion des Erreurs
- `ResourceNotFoundException` - Ressources non trouvées (404)
- `DuplicateResourceException` - Doublons détectés (409)
- `GlobalExceptionHandler` - Gestion centralisée des erreurs
- Réponses d'erreur JSON structurées avec timestamps

#### Validation
- Validation Jakarta Bean pour les champs obligatoires
- Validation du format email
- Messages d'erreur en français
- Gestion des erreurs de validation (400)

#### Configuration
- Configuration pour port 8081 (Product sur 8082)
- Profils Spring: dev, prod
- CORS configuré pour développement frontend
- RestTemplate configuré pour communication inter-services
- Configuration PostgreSQL/Neon avec SSL

#### Base de Données
- Connexion à PostgreSQL via Neon Cloud
- Auto-création de la table `vendors`
- Support SSL/TLS
- DDL auto-update en développement

#### Documentation
- **README.md** - Documentation API complète
- **QUICKSTART.md** - Guide de démarrage rapide
- **DATABASE_SETUP.md** - Configuration base de données
- **TESTING_GUIDE.md** - Guide de test avec scénarios
- **PROJECT_OVERVIEW.md** - Architecture et vue d'ensemble
- **STRUCTURE.md** - Structure visuelle du projet
- **COMPLETION_SUMMARY.md** - Récapitulatif complet
- **CHANGELOG.md** - Ce fichier

#### Scripts & Outils
- `start.bat` - Script de démarrage Windows
- `build.bat` - Script de compilation Windows
- `api-requests.http` - Collection de requêtes HTTP de test
- `.env.example` - Template pour variables d'environnement

#### Docker
- `Dockerfile` - Build multi-stage optimisé
- `docker-compose.yml` - Configuration pour orchestration

#### Fichiers de Configuration
- `.gitignore` - Règles d'exclusion Git complètes
- `application.properties` - Configuration principale
- `application-dev.properties` - Configuration développement
- `application-prod.properties` - Configuration production

---

## [Unreleased] - Fonctionnalités Prévues

### 🔮 À Venir (Upcoming)

#### Phase 2 - Relation Vendor-Product
- [ ] Table de liaison vendor_products
- [ ] Endpoint: GET /api/vendors/{id}/products
- [ ] Endpoint: POST /api/vendors/{id}/products/{productId}
- [ ] Endpoint: DELETE /api/vendors/{id}/products/{productId}
- [ ] DTO VendorProductDTO

#### Phase 3 - Sécurité
- [ ] Spring Security
- [ ] Authentification JWT
- [ ] Autorisation basée sur les rôles
- [ ] Endpoints sécurisés
- [ ] Rate limiting

#### Phase 4 - Améliorations
- [ ] Pagination pour GET /api/vendors
- [ ] Tri dynamique (sort by name, rating, etc.)
- [ ] Filtres avancés
- [ ] Cache Redis pour performances
- [ ] Upload d'images pour vendeurs
- [ ] Système de notation détaillé

#### Phase 5 - Tests
- [ ] Tests unitaires (JUnit 5)
- [ ] Tests d'intégration
- [ ] Tests de contrôleur (MockMvc)
- [ ] Couverture de code >80%

#### Phase 6 - Monitoring
- [ ] Spring Boot Actuator
- [ ] Endpoints de health check
- [ ] Métriques Prometheus
- [ ] Dashboards Grafana
- [ ] Logs centralisés (ELK Stack)

#### Phase 7 - Documentation API
- [ ] Swagger/OpenAPI 3.0
- [ ] Interface UI Swagger
- [ ] Documentation interactive
- [ ] Exemples de requêtes/réponses

---

## Notes de Version

### Version 0.0.1-SNAPSHOT
**Date de Release**: 7 novembre 2025  
**Type**: Initial Release  
**Status**: Development

#### Technologies Utilisées
- Java 21
- Spring Boot 3.5.7
- Spring Data JPA
- PostgreSQL (Neon)
- Lombok
- Maven

#### Configuration
- Port: 8081
- Base de données: vendor_db
- Profils: dev, prod

#### Breaking Changes
- Aucun (première version)

#### Migration Notes
- Créer la base de données vendor_db avant le premier démarrage
- Configurer les credentials dans application.properties

#### Known Issues
- Aucun problème connu

---

## Format des Versions

Le projet utilise [Semantic Versioning](https://semver.org/):
- **MAJOR**: Changements incompatibles avec les versions précédentes
- **MINOR**: Nouvelles fonctionnalités rétro-compatibles
- **PATCH**: Corrections de bugs rétro-compatibles

Exemple: `1.2.3`
- `1` = Version majeure
- `2` = Version mineure
- `3` = Patch

---

## Types de Changements

- **Added** (Ajouté) - Nouvelles fonctionnalités
- **Changed** (Modifié) - Modifications de fonctionnalités existantes
- **Deprecated** (Déprécié) - Fonctionnalités à supprimer
- **Removed** (Supprimé) - Fonctionnalités supprimées
- **Fixed** (Corrigé) - Corrections de bugs
- **Security** (Sécurité) - Correctifs de sécurité

---

**Maintenu par**: Équipe SoukScan  
**Dernière mise à jour**: 7 novembre 2025

