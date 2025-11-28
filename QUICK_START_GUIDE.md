# 🚀 GUIDE DE DÉMARRAGE RAPIDE - Vendor Microservice

## 🎯 Objectif
Démarrer le microservice Vendor sans erreur après la correction du schéma.

---

## ✅ Ce qui a été corrigé

### Problème initial
```
ERROR: Schema-validation: wrong column type encountered in column [report_id]
```

### Solution appliquée
```properties
# Dans application.properties
spring.jpa.hibernate.ddl-auto=update  ← Hibernate peut maintenant modifier le schéma
```

---

## 📋 Checklist avant de démarrer

- [ ] Java 21 installé
- [ ] Maven installé (ou utiliser `mvnw.cmd`)
- [ ] Connexion Internet (pour accéder à Neon PostgreSQL)
- [ ] Port 8081 disponible

---

## 🚀 DÉMARRAGE EN 3 ÉTAPES

### Étape 1 : Ouvrir un terminal
```cmd
cd C:\Users\MOHAMED\Desktop\vendorms\vendorms
```

### Étape 2 : Lancer l'application
```cmd
start.bat
```

**OU** avec le nouveau script de test :
```cmd
test-start.bat
```

### Étape 3 : Attendre le message de succès
```
Started VendormsApplication in XX.XXX seconds (process running on port 8081)
```

👉 **Si vous voyez ce message → C'EST BON !** ✅

---

## 🎉 Que faire une fois démarré ?

### 1. Tester l'API

Ouvrez un navigateur :
```
http://localhost:8081/api/vendors
```

Résultat attendu :
```json
[]
```
(Liste vide car aucun vendor n'est encore créé)

### 2. Créer votre premier vendor

Ouvrez `api-requests-vendor.http` dans VS Code et testez les requêtes.

Ou utilisez curl :
```bash
curl -X POST http://localhost:8081/api/vendors \
  -H "Content-Type: application/json" \
  -d "{\"userId\":1,\"shopName\":\"Test Shop\",\"shopAddress\":\"123 Test\",\"email\":\"test@shop.ma\",\"city\":\"Casablanca\",\"country\":\"Maroc\"}"
```

### 3. Consulter les logs
Les logs SQL s'affichent dans le terminal pour voir ce que fait Hibernate.

---

## ❌ Si ça ne démarre toujours pas

### Erreur 1 : "Password authentication failed"
**Cause** : Credentials Neon incorrects  
**Solution** : Mettez à jour le mot de passe dans `application.properties`

### Erreur 2 : "Schema validation: wrong column type"
**Cause** : Hibernate ne peut pas modifier automatiquement  
**Solution** : Exécutez le script `fix-schema.sql` sur Neon

#### Comment exécuter fix-schema.sql ?

**Méthode 1 : Console Neon (RECOMMANDÉ)**
1. Allez sur https://console.neon.tech
2. Connectez-vous
3. Sélectionnez votre projet
4. Cliquez sur "SQL Editor"
5. Ouvrez le fichier `fix-schema.sql`
6. Copiez-collez tout le contenu
7. Cliquez sur "Run"

**Méthode 2 : psql (ligne de commande)**
```bash
psql "postgresql://neondb_owner:npg_9zLEfBC3ZlDs@ep-purple-violet-agbhwbie-pooler.c-2.eu-central-1.aws.neon.tech:5432/vendor_db?sslmode=require" -f fix-schema.sql
```

**Méthode 3 : Manuellement**
Exécutez juste cette ligne SQL :
```sql
ALTER TABLE price_reports ALTER COLUMN report_id TYPE BIGINT;
```

### Erreur 3 : "Port 8081 already in use"
**Cause** : Une autre application utilise le port  
**Solution** : Arrêtez l'autre application ou changez le port dans `application.properties`

---

## 🔍 Vérifier que tout fonctionne

### Test 1 : Health check
```bash
curl http://localhost:8081/api/vendors
```
✅ Doit retourner `[]` sans erreur

### Test 2 : Créer un vendor
Utilisez les exemples dans `api-requests-vendor.http`

### Test 3 : Vérifier la base de données
```sql
-- Via console Neon
SELECT * FROM vendors;
```
✅ Doit afficher les vendors créés

---

## 📚 Documentation disponible

| Fichier | Contenu |
|---------|---------|
| `README.md` | Documentation principale |
| `VENDOR_MICROSERVICE_GUIDE.md` | Guide complet d'utilisation |
| `API_DOCUMENTATION.md` | Documentation détaillée de l'API |
| `TROUBLESHOOTING.md` | Guide de dépannage |
| `SCHEMA_FIX_APPLIED.md` | Détails de la correction du schéma |
| `api-requests-vendor.http` | Exemples de requêtes HTTP |
| `fix-schema.sql` | Script de correction du schéma |
| `init-vendor-database.sql` | Script d'initialisation avec données de test |

---

## 🎯 Ordre d'exécution recommandé

```
1. start.bat                    ← Démarrer l'application
2. Vérifier logs                ← Voir si "Started VendormsApplication"
3. Tester http://localhost:8081 ← Vérifier que le serveur répond
4. Ouvrir api-requests-vendor.http ← Tester les endpoints
5. Créer des vendors            ← POST /api/vendors
6. Consulter les vendors        ← GET /api/vendors
```

---

## 💡 Astuces

### Arrêter l'application
Dans le terminal, appuyez sur `Ctrl + C`

### Voir les logs en temps réel
Les logs s'affichent automatiquement dans le terminal

### Relancer après modification du code
```bash
# Arrêter avec Ctrl+C
# Puis relancer
start.bat
```

### Nettoyer et recompiler
```bash
mvnw.cmd clean install
```

---

## 🆘 Besoin d'aide ?

### Logs complets
```bash
mvnw.cmd spring-boot:run -X
```

### Tester la connexion à la base
```bash
psql "postgresql://neondb_owner:npg_9zLEfBC3ZlDs@ep-purple-violet-agbhwbie-pooler.c-2.eu-central-1.aws.neon.tech:5432/vendor_db?sslmode=require"
```

### Vérifier les entités
Tous les fichiers `.java` dans `src/main/java/com/soukscan/vendorms/entity/`

---

## 🎊 Résumé

1. ✅ Configuration corrigée (`ddl-auto=update`)
2. ✅ Scripts de correction créés (`fix-schema.sql`)
3. ✅ Documentation complète disponible
4. ✅ Exemples de requêtes prêts

**Vous êtes prêt à démarrer !**

Exécutez simplement :
```bash
start.bat
```

Et consultez `TROUBLESHOOTING.md` si vous rencontrez un problème.

---

*Guide créé le : 10 novembre 2025*  
*Version : 1.0*  
*Statut : ✅ PRÊT*

