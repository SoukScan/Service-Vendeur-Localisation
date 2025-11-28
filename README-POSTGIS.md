# Vendor Service avec PostGIS - Guide Complet

## 🗺️ Vue d'ensemble

Ce microservice gère les vendeurs/fournisseurs avec **géolocalisation PostGIS**, permettant de :
- Localiser les vendeurs sur une carte
- Trouver les vendeurs les plus proches
- Comparer les prix par zone géographique
- Analyser les rapports de prix avec contexte spatial

---

## 📊 Architecture des Tables

### 1. Table `locations` - Emplacements des Vendeurs

```sql
CREATE TABLE locations (
    vendor_id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address TEXT,
    geom GEOMETRY(Point, 4326)  -- Coordonnées GPS (latitude, longitude)
);
```

**Caractéristiques** :
- SRID 4326 = WGS 84 (système GPS standard)
- Index spatial GIST pour requêtes géographiques ultra-rapides
- Support des fonctions PostGIS (ST_DWithin, ST_Distance, etc.)

### 2. Table `price_reports` - Rapports de Prix

```sql
CREATE TABLE price_reports (
    report_id SERIAL PRIMARY KEY,
    product_id VARCHAR(50) NOT NULL,
    vendor_id VARCHAR(50) REFERENCES locations(vendor_id),
    price DECIMAL(10, 2) NOT NULL,
    reported_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
```

**Fonctionnalités** :
- Chaque vendor peut rapporter des prix pour différents produits
- Horodatage avec timezone pour suivre l'évolution des prix
- Lien avec la table locations pour requêtes géographiques

### 3. Table `price_avg` - Prix Moyens Calculés

```sql
CREATE TABLE price_avg (
    product_id VARCHAR(50) PRIMARY KEY,
    average_price DECIMAL(10, 2) NOT NULL,
    report_count INTEGER DEFAULT 0,
    last_updated TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
```

**Usage** :
- Mise à jour automatique à chaque nouveau rapport de prix
- Permet de comparer rapidement les prix moyens
- Optimisé pour les requêtes de recherche

---

## 🚀 Installation et Configuration

### 1. Prérequis

- Java 21
- PostgreSQL 12+ avec extension PostGIS
- Maven 3.6+
- Base de données sur Neon avec PostGIS activé

### 2. Activer PostGIS sur Neon

Connectez-vous à votre console Neon et exécutez :

```sql
CREATE EXTENSION IF NOT EXISTS postgis;
SELECT PostGIS_Version();  -- Vérifier l'installation
```

### 3. Créer les Tables

Deux options :

**Option A : Exécuter le script SQL**
```bash
psql -h your-neon-host -d vendor_db -U neondb_owner -f init-database.sql
```

**Option B : Les tables existent déjà**
Si vous avez déjà créé les tables, configurez simplement :
```properties
spring.jpa.hibernate.ddl-auto=validate
```

### 4. Configuration application.properties

```properties
spring.datasource.url=jdbc:postgresql://your-host:5432/vendor_db?sslmode=require
spring.datasource.username=neondb_owner
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.properties.hibernate.dialect=org.hibernate.spatial.dialect.postgis.PostgisPG10Dialect
```

### 5. Démarrer l'Application

```bash
mvnw.cmd spring-boot:run
```

---

## 📍 API Endpoints

### Locations (Vendeurs avec Géolocalisation)

#### Créer une location
```http
POST /api/locations
Content-Type: application/json

{
  "vendorId": "vendor-001",
  "name": "Marché Central",
  "address": "Boulevard Mohammed V, Casablanca",
  "latitude": 33.5731,
  "longitude": -7.5898
}
```

#### Trouver les vendeurs à proximité
```http
GET /api/locations/nearby?latitude=33.5731&longitude=-7.5898&radius=5
```
- `radius` : Rayon en kilomètres

#### Trouver les N vendeurs les plus proches
```http
GET /api/locations/nearest?latitude=33.5731&longitude=-7.5898&limit=10
```

#### Récupérer toutes les locations
```http
GET /api/locations
```

#### Récupérer une location spécifique
```http
GET /api/locations/{vendorId}
```

#### Mettre à jour une location
```http
PUT /api/locations/{vendorId}
```

#### Supprimer une location
```http
DELETE /api/locations/{vendorId}
```

---

### Price Reports (Rapports de Prix)

#### Créer un rapport de prix
```http
POST /api/price-reports
Content-Type: application/json

{
  "productId": "product-001",
  "vendorId": "vendor-001",
  "price": 25.50
}
```

**Note** : La création d'un rapport de prix met automatiquement à jour le prix moyen dans `price_avg`.

#### Récupérer les prix d'un produit
```http
GET /api/price-reports/product/{productId}
```

#### Récupérer les prix d'un vendor
```http
GET /api/price-reports/vendor/{vendorId}
```

#### Récupérer les prix récents (7 derniers jours)
```http
GET /api/price-reports/product/{productId}/recent?days=7
```

#### **🔥 Recherche Géographique** - Prix à proximité
```http
GET /api/price-reports/product/{productId}/nearby?latitude=33.5731&longitude=-7.5898&radius=10
```

Cette requête combine :
- Filtrage par produit
- Filtrage géographique (rayon en km)
- Tri par distance

---

### Price Averages (Prix Moyens)

#### Récupérer tous les prix moyens
```http
GET /api/price-averages
```

#### Récupérer le prix moyen d'un produit
```http
GET /api/price-averages/{productId}
```

#### Produits dans une fourchette de prix
```http
GET /api/price-averages/range?min=20.00&max=50.00
```

#### Les produits les moins chers
```http
GET /api/price-averages/cheapest?limit=10
```

---

## 🧪 Exemples d'Utilisation

### Scénario 1 : Ajouter des vendeurs au Maroc

```bash
# Casablanca
curl -X POST http://localhost:8081/api/locations \
  -H "Content-Type: application/json" \
  -d '{
    "vendorId": "vendor-001",
    "name": "Marché Central Casablanca",
    "address": "Boulevard Mohammed V",
    "latitude": 33.5731,
    "longitude": -7.5898
  }'

# Rabat
curl -X POST http://localhost:8081/api/locations \
  -H "Content-Type: application/json" \
  -d '{
    "vendorId": "vendor-002",
    "name": "Souk Rabat",
    "address": "Avenue Hassan II",
    "latitude": 34.0209,
    "longitude": -6.8416
  }'
```

### Scénario 2 : Rapporter des prix

```bash
# Prix du pain à Casablanca
curl -X POST http://localhost:8081/api/price-reports \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "pain-blanc",
    "vendorId": "vendor-001",
    "price": 1.50
  }'

# Prix du pain à Rabat
curl -X POST http://localhost:8081/api/price-reports \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "pain-blanc",
    "vendorId": "vendor-002",
    "price": 1.60
  }'
```

### Scénario 3 : Trouver les meilleurs prix près de moi

```bash
# Je suis à Casablanca, je cherche le pain dans un rayon de 5 km
curl "http://localhost:8081/api/price-reports/product/pain-blanc/nearby?latitude=33.5731&longitude=-7.5898&radius=5"
```

---

## 🔍 Requêtes PostGIS Utiles

### Trouver les vendeurs dans un rayon

```sql
SELECT 
    vendor_id, 
    name,
    ST_Distance(
        geom::geography, 
        ST_SetSRID(ST_MakePoint(-7.5898, 33.5731), 4326)::geography
    ) / 1000 as distance_km
FROM locations
WHERE ST_DWithin(
    geom::geography, 
    ST_SetSRID(ST_MakePoint(-7.5898, 33.5731), 4326)::geography, 
    5000  -- 5 km en mètres
)
ORDER BY distance_km;
```

### Calculer la distance entre deux points

```sql
SELECT ST_Distance(
    ST_SetSRID(ST_MakePoint(-7.5898, 33.5731), 4326)::geography,  -- Casablanca
    ST_SetSRID(ST_MakePoint(-6.8416, 34.0209), 4326)::geography   -- Rabat
) / 1000 as distance_km;
```

### Prix moyens par zone géographique

```sql
SELECT 
    pr.product_id,
    AVG(pr.price) as avg_price,
    COUNT(*) as report_count
FROM price_reports pr
JOIN locations l ON pr.vendor_id = l.vendor_id
WHERE ST_DWithin(
    l.geom::geography,
    ST_SetSRID(ST_MakePoint(-7.5898, 33.5731), 4326)::geography,
    10000  -- 10 km
)
GROUP BY pr.product_id;
```

---

## 📐 Concepts PostGIS Clés

### SRID 4326 (WGS 84)
- Système de coordonnées GPS standard
- Latitude : -90° à 90° (Y)
- Longitude : -180° à 180° (X)

### Point Geometry
```sql
-- Créer un point : ST_MakePoint(longitude, latitude)
ST_SetSRID(ST_MakePoint(-7.5898, 33.5731), 4326)
```

**⚠️ Important** : L'ordre est (X, Y) = (longitude, latitude)

### Geography vs Geometry

- **Geography** : Calculs précis en mètres sur un globe
- **Geometry** : Calculs rapides sur un plan 2D

```sql
-- Utiliser geography pour des distances précises
geom::geography
```

### Fonctions Principales

| Fonction | Description | Exemple |
|----------|-------------|---------|
| `ST_MakePoint(x, y)` | Créer un point | `ST_MakePoint(-7.5898, 33.5731)` |
| `ST_Distance(g1, g2)` | Distance entre 2 points | En mètres avec geography |
| `ST_DWithin(g1, g2, d)` | Point dans un rayon | `ST_DWithin(geom, point, 5000)` |
| `ST_AsText(geom)` | Texte lisible | `POINT(-7.5898 33.5731)` |

---

## 🎯 Cas d'Usage

### 1. Application Mobile - "Trouve le meilleur prix près de toi"

```javascript
// Frontend envoie la position de l'utilisateur
const userLat = 33.5731;
const userLon = -7.5898;
const productId = "pain-blanc";

fetch(`/api/price-reports/product/${productId}/nearby?latitude=${userLat}&longitude=${userLon}&radius=2`)
  .then(res => res.json())
  .then(prices => {
    // Afficher les prix triés par distance
    prices.forEach(p => {
      console.log(`${p.vendorName}: ${p.price} MAD`);
    });
  });
```

### 2. Dashboard Admin - Carte des Vendeurs

```javascript
// Récupérer tous les vendeurs
fetch('/api/locations')
  .then(res => res.json())
  .then(locations => {
    // Afficher sur Google Maps / Leaflet
    locations.forEach(loc => {
      addMarker(loc.latitude, loc.longitude, loc.name);
    });
  });
```

### 3. Analyse de Prix par Zone

```sql
-- Prix moyen par région (rayon de 50 km autour des grandes villes)
WITH city_centers AS (
    SELECT 'Casablanca' as city, ST_SetSRID(ST_MakePoint(-7.5898, 33.5731), 4326)::geography as center
    UNION ALL
    SELECT 'Rabat', ST_SetSRID(ST_MakePoint(-6.8416, 34.0209), 4326)::geography
)
SELECT 
    cc.city,
    pr.product_id,
    AVG(pr.price) as avg_price
FROM city_centers cc
CROSS JOIN price_reports pr
JOIN locations l ON pr.vendor_id = l.vendor_id
WHERE ST_DWithin(l.geom::geography, cc.center, 50000)
GROUP BY cc.city, pr.product_id;
```

---

## 🛠️ Dépannage

### Extension PostGIS non trouvée
```sql
-- Vérifier si PostGIS est installé
SELECT * FROM pg_available_extensions WHERE name = 'postgis';

-- Installer PostGIS (nécessite les droits superuser)
CREATE EXTENSION postgis;
```

### Erreur "geometry type not recognized"
- Vérifiez que `hibernate-spatial` est dans le pom.xml
- Vérifiez le dialect : `org.hibernate.spatial.dialect.postgis.PostgisPG10Dialect`

### Les distances sont incorrectes
- Utilisez `::geography` pour des calculs précis en mètres
- Vérifiez l'ordre : `ST_MakePoint(longitude, latitude)`

---

## 📚 Ressources

- [PostGIS Documentation](https://postgis.net/documentation/)
- [Hibernate Spatial](https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html#spatial)
- [Neon PostGIS Guide](https://neon.tech/docs/extensions/postgis)

---

## 🎉 Prêt à Démarrer !

1. Activez PostGIS sur Neon
2. Exécutez `init-database.sql`
3. Lancez `mvnw.cmd spring-boot:run`
4. Testez avec `api-requests-postgis.http`

**Bonne géolocalisation ! 🗺️**

