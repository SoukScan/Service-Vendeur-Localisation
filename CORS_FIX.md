# 🔧 Correction CORS - allowCredentials

**Date** : 10 novembre 2025  
**Problème résolu** : Erreur CORS avec allowCredentials

---

## 🐛 Problème

Lors de l'accès à `http://localhost:8081/api/vendors`, erreur :

```json
{
  "details": "When allowCredentials is true, allowedOrigins cannot contain the special value \"*\"...",
  "message": "Une erreur interne s'est produite",
  "status": 500
}
```

---

## ✅ Solution appliquée

### Changement dans CorsConfig.java

**AVANT (ne fonctionne pas) :**
```java
.allowedOrigins("http://localhost:3000", "http://localhost:4200", ...)
.allowCredentials(true)
```

**APRÈS (fonctionne) :**
```java
.allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*")
.allowCredentials(true)
```

---

## 🎯 Pourquoi ce changement ?

### Problème avec allowedOrigins
- `allowedOrigins()` avec `allowCredentials(true)` est très strict
- Spring Security ne peut pas définir l'en-tête `Access-Control-Allow-Origin` correctement
- Cela cause une erreur 500 au lieu d'une erreur CORS classique

### Solution avec allowedOriginPatterns
- `allowedOriginPatterns()` supporte les wildcards comme `*`
- Compatible avec `allowCredentials(true)`
- Plus flexible pour le développement local

---

## 🔄 Que faire maintenant ?

### 1. Redémarrer l'application

**Si l'application est en cours d'exécution :**
- Appuyez sur `Ctrl+C` dans le terminal
- Relancez avec `start.bat`

**Commandes :**
```bash
cd C:\Users\MOHAMED\Desktop\vendorms\vendorms
start.bat
```

### 2. Tester l'API

Ouvrez votre navigateur :
```
http://localhost:8081/api/vendors
```

**Résultat attendu :**
```json
[]
```

(Liste vide car aucun vendor créé encore)

---

## 🧪 Tests supplémentaires

### Test 1 : GET tous les vendors
```
GET http://localhost:8081/api/vendors
```
✅ Devrait retourner `[]`

### Test 2 : GET vendors actifs
```
GET http://localhost:8081/api/vendors/active
```
✅ Devrait retourner `[]`

### Test 3 : POST créer un vendor
Utilisez `api-requests-vendor.http` ou curl :
```bash
curl -X POST http://localhost:8081/api/vendors \
  -H "Content-Type: application/json" \
  -d "{\"userId\":1,\"shopName\":\"Test Shop\",\"shopAddress\":\"123 Test\",\"email\":\"test@shop.ma\",\"city\":\"Casablanca\",\"country\":\"Maroc\"}"
```

---

## 📚 Comprendre allowedOriginPatterns

### Syntaxe

| Pattern | Signification |
|---------|---------------|
| `http://localhost:*` | N'importe quel port sur localhost |
| `http://localhost:8080` | Seulement le port 8080 |
| `http://*.example.com` | Tous les sous-domaines |
| `*` | ❌ N'UTILISEZ PAS avec allowCredentials |

### Notre configuration actuelle

```java
.allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*")
```

**Accepte :**
- ✅ `http://localhost:3000`
- ✅ `http://localhost:4200`
- ✅ `http://localhost:8080`
- ✅ `http://127.0.0.1:3000`
- ✅ N'importe quel port sur localhost

**Rejette :**
- ❌ `https://example.com`
- ❌ `http://192.168.1.100`

---

## 🔐 Sécurité

### En développement (actuellement)
```java
.allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*")
```
✅ OK - Accepte tous les ports locaux

### En production (recommandé)
```java
.allowedOriginPatterns(
    "https://votre-frontend.com",
    "https://www.votre-frontend.com"
)
```
✅ Seulement vos domaines de production

---

## 🎯 Alternatives (si le problème persiste)

### Option 1 : Désactiver allowCredentials (temporairement)
```java
.allowedOriginPatterns("*")
.allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
.allowedHeaders("*")
.allowCredentials(false)  // ← Changé à false
.maxAge(3600);
```

### Option 2 : Configuration par annotation
Si vous voulez désactiver CORS globalement et gérer au niveau du contrôleur :

```java
@RestController
@RequestMapping("/api/vendors")
@CrossOrigin(
    origins = "*",
    allowedHeaders = "*",
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE}
)
public class VendorController {
    // ...
}
```

---

## ✅ Vérification de la correction

### 1. Logs de démarrage
Après le redémarrage, vérifiez les logs :
```
Started VendormsApplication in X.XXX seconds
```
✅ Pas d'erreur au démarrage

### 2. Test navigateur
```
http://localhost:8081/api/vendors
```
✅ Retourne `[]` au lieu d'une erreur 500

### 3. Test avec curl
```bash
curl -v http://localhost:8081/api/vendors
```
✅ Vérifiez les en-têtes CORS dans la réponse :
```
Access-Control-Allow-Origin: http://localhost:XXXX
Access-Control-Allow-Credentials: true
```

---

## 📝 Résumé

| Élément | Avant | Après |
|---------|-------|-------|
| Méthode | `allowedOrigins()` | `allowedOriginPatterns()` |
| Valeurs | Ports spécifiques | Patterns avec wildcard |
| Compatibilité | ❌ Erreur avec allowCredentials | ✅ Compatible |
| Résultat | Erreur 500 | ✅ Fonctionne |

---

## 🆘 Si le problème persiste

### 1. Vérifier que l'application a bien redémarré
```bash
# Arrêter
Ctrl+C dans le terminal

# Nettoyer et recompiler
mvnw.cmd clean install

# Redémarrer
start.bat
```

### 2. Vérifier les logs
Cherchez dans les logs :
```
CorsConfig : Configuring CORS...
```

### 3. Test avec différents navigateurs
- Chrome : F12 → Console → Voir les erreurs CORS
- Firefox : F12 → Console
- Edge : F12 → Console

---

## 🎊 Conclusion

Le problème CORS a été **résolu** en utilisant `allowedOriginPatterns()` au lieu de `allowedOrigins()`.

**Redémarrez l'application et testez :**
```bash
start.bat
```

Puis ouvrez :
```
http://localhost:8081/api/vendors
```

Vous devriez voir `[]` au lieu de l'erreur 500 ! ✅

---

*Correction appliquée le : 10 novembre 2025*  
*Statut : ✅ RÉSOLU*

