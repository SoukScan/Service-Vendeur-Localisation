# 🔧 Guide de dépannage - Erreur de schéma

## Problème rencontré

```
Schema-validation: wrong column type encountered in column [report_id] in table [price_reports]; 
found [serial (Types#INTEGER)], but expecting [bigint (Types#BIGINT)]
```

## Cause

La base de données `vendor_db` contient déjà des tables avec un schéma ancien/différent. Hibernate en mode `validate` vérifie strictement que le schéma de la base correspond aux entités Java.

---

## ✅ Solution 1 : Laisser Hibernate mettre à jour automatiquement (DÉJÀ APPLIQUÉE)

J'ai modifié `application.properties` :

```properties
# AVANT (mode strict)
spring.jpa.hibernate.ddl-auto=validate

# APRÈS (mode mise à jour automatique)
spring.jpa.hibernate.ddl-auto=update
```

**Essayez de relancer l'application maintenant.**

---

## 🔄 Solution 2 : Corriger manuellement le schéma (si Solution 1 ne fonctionne pas)

Si Hibernate ne peut pas modifier automatiquement la colonne, exécutez le script SQL :

### Option A : Via psql (ligne de commande)
```bash
psql "postgresql://neondb_owner:npg_9zLEfBC3ZlDs@ep-purple-violet-agbhwbie-pooler.c-2.eu-central-1.aws.neon.tech:5432/vendor_db?sslmode=require" -f fix-schema.sql
```

### Option B : Via la console Neon
1. Connectez-vous à https://console.neon.tech
2. Sélectionnez votre projet
3. Allez dans l'onglet SQL Editor
4. Copiez-collez le contenu de `fix-schema.sql`
5. Exécutez le script

### Option C : Manuellement via SQL
```sql
-- Corriger le type de report_id
ALTER TABLE price_reports ALTER COLUMN report_id TYPE BIGINT;
```

---

## 🗑️ Solution 3 : Réinitialiser complètement la base (ATTENTION : perte de données)

Si vous voulez repartir de zéro :

```sql
-- Supprimer toutes les tables
DROP TABLE IF EXISTS price_reports CASCADE;
DROP TABLE IF EXISTS price_avg CASCADE;
DROP TABLE IF EXISTS locations CASCADE;
DROP TABLE IF EXISTS vendors CASCADE;

-- Relancer l'application, Hibernate créera les tables avec le bon schéma
```

---

## 🔍 Vérifier l'état actuel de la base

Pour voir le schéma actuel de la table problématique :

```sql
-- Voir la structure de price_reports
\d price_reports

-- Ou
SELECT column_name, data_type, character_maximum_length
FROM information_schema.columns
WHERE table_name = 'price_reports';
```

---

## 📝 Modes Hibernate ddl-auto

| Mode | Description | Usage recommandé |
|------|-------------|------------------|
| `none` | Ne fait rien | Production (gestion manuelle) |
| `validate` | Vérifie strictement le schéma | Production (après migration) |
| `update` | Met à jour le schéma si nécessaire | **Développement** ✅ |
| `create` | Supprime et recrée les tables au démarrage | Tests |
| `create-drop` | Crée au démarrage, supprime à l'arrêt | Tests |

---

## 🚀 Prochaines étapes

1. **Relancer l'application** avec `start.bat`
2. Si ça fonctionne → Vous êtes prêt ! ✅
3. Si ça ne fonctionne pas → Appliquer Solution 2 ou 3

---

## 💡 Conseils pour éviter ce problème à l'avenir

1. **Développement** : Utilisez `ddl-auto=update`
2. **Production** : Utilisez des migrations (Flyway ou Liquibase)
3. **Base de données séparées** : Dev, Test, Prod avec des données différentes
4. **Scripts de migration** : Versionnez vos changements de schéma

---

## 🐛 Autres erreurs possibles

### Erreur : "password authentication failed"
→ Vérifiez vos credentials dans `application.properties`

### Erreur : "database does not exist"
→ Créez la base `vendor_db` sur Neon

### Erreur : PostGIS not available
→ La base utilise PostGIS pour les autres tables (locations), mais pas pour vendors

---

## 📞 Besoin d'aide ?

Si le problème persiste, vérifiez :
- Que la connexion à Neon fonctionne
- Que vous avez les droits ALTER TABLE
- Les logs complets de l'application

Relancez avec :
```bash
mvn spring-boot:run -X
```

Pour voir plus de détails.

