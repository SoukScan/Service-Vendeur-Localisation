# Guide de Contribution - Vendor Service

Merci de votre intérêt pour contribuer au projet SoukScan Vendor Service ! 🎉

---

## 📋 Table des Matières

1. [Code de Conduite](#code-de-conduite)
2. [Comment Contribuer](#comment-contribuer)
3. [Standards de Code](#standards-de-code)
4. [Process de Pull Request](#process-de-pull-request)
5. [Convention de Commit](#convention-de-commit)
6. [Tests](#tests)
7. [Documentation](#documentation)

---

## 📜 Code de Conduite

Ce projet adhère à un code de conduite professionnel. En participant, vous êtes tenu de respecter ce code.

### Comportements Attendus
- ✅ Utiliser un langage accueillant et inclusif
- ✅ Respecter les points de vue différents
- ✅ Accepter les critiques constructives
- ✅ Se concentrer sur ce qui est meilleur pour la communauté

---

## 🤝 Comment Contribuer

### Signaler un Bug 🐛

Avant de créer un rapport de bug, vérifiez qu'il n'existe pas déjà.

**Format du rapport:**
```markdown
## Description
[Description claire du bug]

## Étapes pour Reproduire
1. Aller à '...'
2. Cliquer sur '...'
3. Voir l'erreur

## Comportement Attendu
[Ce qui devrait se passer]

## Comportement Actuel
[Ce qui se passe réellement]

## Environnement
- OS: [Windows/Linux/Mac]
- Java Version: [21]
- Spring Boot Version: [3.5.7]
```

### Proposer une Fonctionnalité 💡

**Format de proposition:**
```markdown
## Problème
[Quel problème cette fonctionnalité résout-elle?]

## Solution Proposée
[Comment voulez-vous résoudre ce problème?]

## Alternatives
[Quelles alternatives avez-vous envisagées?]

## Contexte Additionnel
[Informations supplémentaires]
```

---

## 📝 Standards de Code

### Style Java

Suivre les conventions Java standard:

```java
// ✅ BON
public class VendorService {
    
    private final VendorRepository vendorRepository;
    
    public VendorResponseDTO createVendor(VendorRequestDTO request) {
        // Implementation
    }
}

// ❌ MAUVAIS
public class vendorservice {
    
    private VendorRepository repo;
    
    public VendorResponseDTO CreateVendor(VendorRequestDTO req) {
        // Implementation
    }
}
```

### Conventions de Nommage

| Type | Convention | Exemple |
|------|------------|---------|
| Classe | PascalCase | `VendorService` |
| Méthode | camelCase | `createVendor` |
| Variable | camelCase | `vendorRepository` |
| Constante | UPPER_SNAKE_CASE | `MAX_VENDORS` |
| Package | lowercase | `com.soukscan.vendorms` |

### Annotations Spring

```java
// Ordre des annotations
@Service
@RequiredArgsConstructor
@Slf4j
public class VendorService {
    // ...
}

// Controller
@RestController
@RequestMapping("/api/vendors")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VendorController {
    // ...
}
```

### Gestion des Exceptions

```java
// ✅ BON - Exception spécifique
if (!vendorRepository.existsById(id)) {
    throw new ResourceNotFoundException("Vendeur non trouvé avec l'ID: " + id);
}

// ❌ MAUVAIS - Exception générique
if (!vendorRepository.existsById(id)) {
    throw new RuntimeException("Not found");
}
```

### Logging

```java
// ✅ BON
log.info("Création d'un nouveau vendeur avec l'email: {}", request.getEmail());
log.error("Erreur lors de la création du vendeur: {}", e.getMessage(), e);

// ❌ MAUVAIS
System.out.println("Creating vendor");
e.printStackTrace();
```

---

## 🔄 Process de Pull Request

### 1. Fork & Clone

```bash
# Fork le repository sur GitHub
# Puis clone votre fork
git clone https://github.com/VOTRE_USERNAME/vendorms.git
cd vendorms
```

### 2. Créer une Branche

```bash
# Format: type/description-courte
git checkout -b feature/add-vendor-statistics
git checkout -b fix/email-validation
git checkout -b docs/update-readme
```

### 3. Développer

- Écrire du code propre
- Suivre les standards
- Ajouter des tests
- Mettre à jour la documentation

### 4. Tester

```bash
# Compiler
mvnw.cmd clean package

# Exécuter les tests
mvnw.cmd test

# Vérifier le code
mvnw.cmd verify
```

### 5. Commit

Suivre la [Convention de Commit](#convention-de-commit)

### 6. Push

```bash
git push origin feature/add-vendor-statistics
```

### 7. Créer une Pull Request

- Titre clair et descriptif
- Description détaillée des changements
- Référencer les issues liées
- Screenshots si applicable

**Template de PR:**
```markdown
## Description
[Description des changements]

## Type de Changement
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Checklist
- [ ] Code suit les standards du projet
- [ ] Tests ajoutés/mis à jour
- [ ] Documentation mise à jour
- [ ] Pas de warnings de compilation
- [ ] Tests passent localement

## Screenshots (si applicable)
[Ajouter des captures d'écran]

## Issues Liées
Fixes #123
```

---

## 📌 Convention de Commit

Utiliser [Conventional Commits](https://www.conventionalcommits.org/):

### Format

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types

- `feat`: Nouvelle fonctionnalité
- `fix`: Correction de bug
- `docs`: Documentation uniquement
- `style`: Formatage (pas de changement de code)
- `refactor`: Refactoring du code
- `test`: Ajout/modification de tests
- `chore`: Maintenance (dependencies, etc.)

### Exemples

```bash
# Feature
git commit -m "feat(vendor): add pagination support"

# Bug fix
git commit -m "fix(validation): correct email validation regex"

# Documentation
git commit -m "docs(readme): update API endpoints section"

# Refactoring
git commit -m "refactor(service): simplify vendor creation logic"

# Breaking change
git commit -m "feat(api): change response format

BREAKING CHANGE: Response now includes metadata object"
```

---

## 🧪 Tests

### Tests Unitaires

```java
@Test
void shouldCreateVendor() {
    // Given
    VendorRequestDTO request = new VendorRequestDTO();
    request.setName("Test Vendor");
    request.setEmail("test@vendor.com");
    
    // When
    VendorResponseDTO response = vendorService.createVendor(request);
    
    // Then
    assertNotNull(response.getId());
    assertEquals("Test Vendor", response.getName());
}
```

### Tests d'Intégration

```java
@SpringBootTest
@AutoConfigureMockMvc
class VendorControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void shouldCreateVendorViaAPI() throws Exception {
        mockMvc.perform(post("/api/vendors")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"Test\",\"email\":\"test@test.com\"}"))
            .andExpect(status().isCreated());
    }
}
```

### Couverture de Code

Viser une couverture de >80% pour le nouveau code.

---

## 📚 Documentation

### Javadoc

```java
/**
 * Crée un nouveau vendeur dans le système.
 *
 * @param request Les données du vendeur à créer
 * @return Le vendeur créé avec son ID généré
 * @throws DuplicateResourceException si l'email existe déjà
 */
@Transactional
public VendorResponseDTO createVendor(VendorRequestDTO request) {
    // Implementation
}
```

### Documentation API

Mettre à jour `README.md` et `api-requests.http` pour tout nouveau endpoint.

### Changelog

Ajouter une entrée dans `CHANGELOG.md` pour chaque changement significatif.

---

## 🏗️ Structure des Branches

```
main (production)
  │
  ├── develop (développement)
  │     │
  │     ├── feature/new-feature
  │     ├── feature/another-feature
  │     └── fix/bug-fix
  │
  └── hotfix/urgent-fix
```

### Règles
- `main`: Code de production stable
- `develop`: Intégration des fonctionnalités
- `feature/*`: Nouvelles fonctionnalités
- `fix/*`: Corrections de bugs
- `hotfix/*`: Corrections urgentes en production

---

## ✅ Checklist Avant Soumission

- [ ] Code compile sans erreurs
- [ ] Tests passent (`mvnw test`)
- [ ] Pas de warnings
- [ ] Code formaté correctement
- [ ] Documentation mise à jour
- [ ] Commits suivent la convention
- [ ] Branch à jour avec main/develop
- [ ] PR description complète

---

## 🎯 Priorités de Contribution

### High Priority 🔴
- Corrections de bugs critiques
- Problèmes de sécurité
- Améliorations de performance

### Medium Priority 🟡
- Nouvelles fonctionnalités
- Améliorations UX
- Refactoring

### Low Priority 🟢
- Documentation
- Optimisations mineures
- Nettoyage du code

---

## 📞 Questions?

Si vous avez des questions:
1. Consultez la documentation existante
2. Recherchez dans les issues existantes
3. Créez une nouvelle issue avec le tag `question`

---

## 🙏 Remerciements

Merci à tous les contributeurs qui aident à améliorer SoukScan Vendor Service !

---

**Happy Contributing! 🚀**

