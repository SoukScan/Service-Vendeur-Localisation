# 🚀 Guide de Démarrage Rapide - Microservice Vendor

## Étapes d'installation et de démarrage

### 1️⃣ Prérequis
Assurez-vous d'avoir installé :
- ✅ Java 21 (JDK)
- ✅ Maven 3.6+ (ou utilisez le wrapper Maven inclus)
- ✅ Base de données PostgreSQL créée sur Neon

### 2️⃣ Configuration de la Base de Données

**Option A : Utiliser la même base de données que Product**
- La configuration actuelle utilise les mêmes credentials
- Créez simplement une nouvelle base `vendor_db` dans le même projet Neon

**Option B : Créer un nouveau projet Neon**
1. Allez sur https://console.neon.tech/
2. Créez un nouveau projet "SoukScan Vendor"
3. Notez vos credentials
4. Mettez à jour `src/main/resources/application.properties`

Voir le fichier `DATABASE_SETUP.md` pour plus de détails.

### 3️⃣ Vérifier la Configuration

Ouvrez le fichier `src/main/resources/application.properties` et vérifiez :

```properties
server.port=8081                          # Port du microservice
spring.datasource.url=jdbc:postgresql://... # URL de votre base Neon
spring.datasource.username=neondb_owner    # Votre username
spring.datasource.password=...             # Votre password
```

### 4️⃣ Compiler le Projet

**Option A : Avec Maven Wrapper (Recommandé)**
```bash
mvnw.cmd clean package
```

**Option B : Avec le script fourni**
Double-cliquez sur `build.bat`

**Option C : Avec Maven installé**
```bash
mvn clean package
```

### 5️⃣ Démarrer l'Application

**Option A : Mode Développement (avec hot reload)**
```bash
mvnw.cmd spring-boot:run
```

**Option B : Avec le script fourni**
Double-cliquez sur `start.bat`

**Option C : Avec le JAR compilé**
```bash
java -jar target\vendorms-0.0.1-SNAPSHOT.jar
```

### 6️⃣ Vérifier que l'Application Fonctionne

L'application démarre sur **http://localhost:8081**

Testez avec cette requête :
```bash
curl http://localhost:8081/api/vendors
```

Ou ouvrez votre navigateur : http://localhost:8081/api/vendors

### 7️⃣ Tester l'API

**Option A : Avec le fichier HTTP (IntelliJ/VS Code)**
- Ouvrez `api-requests.http`
- Cliquez sur "Run" à côté de chaque requête

**Option B : Avec Postman**
- Importez les requêtes du fichier `api-requests.http`

**Option C : Avec curl**
```bash
# Créer un vendeur
curl -X POST http://localhost:8081/api/vendors ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"Test Vendor\",\"email\":\"test@vendor.com\"}"

# Récupérer tous les vendeurs
curl http://localhost:8081/api/vendors
```

Voir le fichier `TESTING_GUIDE.md` pour tous les scénarios de test.

---

## 📊 Vérification de la Base de Données

Après le premier démarrage, la table `vendors` sera créée automatiquement.

Vérifiez dans votre console Neon :
```sql
SELECT * FROM vendors;
```

---

## 🔧 Dépannage

### ❌ Erreur : "Port 8081 already in use"
**Solution :** 
1. Arrêtez l'application qui utilise le port 8081
2. Ou changez le port dans `application.properties` :
   ```properties
   server.port=8083
   ```

### ❌ Erreur : "Unable to connect to database"
**Solutions :**
1. Vérifiez vos credentials dans `application.properties`
2. Vérifiez que la base `vendor_db` existe sur Neon
3. Vérifiez votre connexion Internet
4. Vérifiez les paramètres SSL : `sslmode=require`

### ❌ Erreur : "JAVA_HOME not found"
**Solution :**
1. Installez Java 21 JDK
2. Configurez la variable d'environnement JAVA_HOME
3. Ou utilisez le Maven Wrapper : `mvnw.cmd`

### ❌ La table vendors n'est pas créée
**Solution :**
Vérifiez dans `application.properties` :
```properties
spring.jpa.hibernate.ddl-auto=update
```

---

## 🌐 Communication avec le Microservice Product

Le microservice Vendor peut communiquer avec Product via :
- **URL Product Service :** http://localhost:8082/api/products
- Configuré dans : `application.properties`

---

## 📁 Fichiers Importants

| Fichier | Description |
|---------|-------------|
| `pom.xml` | Configuration Maven et dépendances |
| `application.properties` | Configuration principale |
| `api-requests.http` | Requêtes HTTP de test |
| `README.md` | Documentation complète |
| `DATABASE_SETUP.md` | Guide de configuration DB |
| `TESTING_GUIDE.md` | Guide de test détaillé |
| `start.bat` | Script de démarrage rapide |
| `build.bat` | Script de compilation |

---

## ✅ Checklist Post-Installation

- [ ] Java 21 installé
- [ ] Base de données `vendor_db` créée sur Neon
- [ ] Configuration `application.properties` mise à jour
- [ ] Compilation réussie (`mvnw.cmd clean package`)
- [ ] Application démarrée (voir "Started VendormsApplication")
- [ ] Table `vendors` créée dans la base de données
- [ ] Endpoint `/api/vendors` accessible
- [ ] Premier vendeur créé avec succès

---

## 🎯 Prochaines Étapes

1. Créez quelques vendeurs de test avec `api-requests.http`
2. Consultez `TESTING_GUIDE.md` pour tester tous les endpoints
3. Intégrez avec le microservice Product (port 8082)
4. Développez votre frontend pour consommer l'API

---

## 📞 Support

Si vous rencontrez des problèmes :
1. Vérifiez les logs de l'application dans la console
2. Consultez `TESTING_GUIDE.md` et `DATABASE_SETUP.md`
3. Vérifiez que tous les prérequis sont installés

---

**Bon développement ! 🎉**

