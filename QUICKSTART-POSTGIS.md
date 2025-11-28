# 🚀 Démarrage Rapide - Vendor Service PostGIS

## En 5 Minutes ⏱️

### 1️⃣ Activer PostGIS (30 secondes)

Connectez-vous à votre console Neon et exécutez :

```sql
CREATE EXTENSION IF NOT EXISTS postgis;
```

Vérifiez :
```sql
SELECT PostGIS_Version();
```

### 2️⃣ Créer les Tables (1 minute)

**Vos tables existent déjà ?** ✅ Passez à l'étape 3

**Sinon**, exécutez le script :

```bash
psql -h ep-purple-violet-a-pooler.eu-central-1.aws.neon.tech \
     -d vendor_db \
     -U neondb_owner \
     -f init-database.sql
```

Ou copiez-collez depuis `init-database.sql` dans la console Neon.

### 3️⃣ Configurer (30 secondes)

Ouvrez `src/main/resources/application.properties` :

```properties
# Mettez VOTRE URL Neon
spring.datasource.url=jdbc:postgresql://VOTRE-HOST:5432/VOTRE-DB?sslmode=require
spring.datasource.username=VOTRE-USERNAME
spring.datasource.password=VOTRE-PASSWORD
```

### 4️⃣ Compiler (1 minute)

```bash
mvnw.cmd clean package
```

### 5️⃣ Démarrer (30 secondes)

```bash
mvnw.cmd spring-boot:run
```

Attendez le message :
```
Started VendormsApplication in X seconds
```

### 6️⃣ Tester (1 minute)

#### Test 1 : Vérifier que ça marche

```bash
curl http://localhost:8081/api/locations
```

Résultat attendu : `[]` (liste vide au début)

#### Test 2 : Créer un vendeur

```bash
curl -X POST http://localhost:8081/api/locations \
  -H "Content-Type: application/json" \
  -d '{
    "vendorId": "test-001",
    "name": "Test Market",
    "address": "123 Test Street",
    "latitude": 33.5731,
    "longitude": -7.5898
  }'
```

#### Test 3 : Recherche à proximité

```bash
curl "http://localhost:8081/api/locations/nearby?latitude=33.5731&longitude=-7.5898&radius=10"
```

---

## ✅ Vous Êtes Prêt !

Le microservice fonctionne maintenant sur **http://localhost:8081**

### 📍 Endpoints Principaux

| Endpoint | Méthode | Description |
|----------|---------|-------------|
| `/api/locations` | GET | Tous les vendeurs |
| `/api/locations/nearby?lat=X&lon=Y&radius=Z` | GET | 🔥 Vendeurs à proximité |
| `/api/price-reports` | POST | Rapporter un prix |
| `/api/price-averages` | GET | Prix moyens |

### 📚 Documentation Complète

- **README-POSTGIS.md** - Guide complet
- **api-requests-postgis.http** - Tests HTTP
- **POSTGIS-MIGRATION-COMPLETE.md** - Récapitulatif

---

## 🐛 Problèmes Courants

### Le serveur ne démarre pas

```
Erreur : Could not connect to database
```

**Solution** :
1. Vérifiez vos credentials dans `application.properties`
2. Vérifiez que la DB existe sur Neon
3. Testez la connexion : `psql -h VOTRE-HOST -d vendor_db -U neondb_owner`

### Extension PostGIS non trouvée

```
Erreur : PostGIS extension not available
```

**Solution** :
```sql
CREATE EXTENSION postgis;
```

### Tables introuvables

```
Erreur : Table "locations" doesn't exist
```

**Solution** : Exécutez `init-database.sql`

---

## 🎯 Prochaines Étapes

1. ✅ Testez avec `api-requests-postgis.http`
2. ✅ Ajoutez des vendeurs de test
3. ✅ Testez la recherche géographique
4. ✅ Intégrez avec votre frontend

---

**Bon développement ! 🚀**

