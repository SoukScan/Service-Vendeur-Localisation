# Guide de configuration de la base de données Neon pour le microservice Vendor

## Étapes pour créer la base de données sur Neon

### 1. Connexion à Neon Console
- Allez sur https://console.neon.tech/
- Connectez-vous avec votre compte

### 2. Créer une nouvelle base de données
Vous avez deux options :

#### Option A : Créer une nouvelle base de données dans le même projet
1. Sélectionnez votre projet existant
2. Allez dans l'onglet "Databases"
3. Cliquez sur "New Database"
4. Nom de la base de données : `vendor_db`
5. Cliquez sur "Create"

#### Option B : Créer un nouveau projet (recommandé pour production)
1. Cliquez sur "New Project"
2. Nom du projet : "SoukScan Vendor Service"
3. Région : Europe Central 1 (même région que Product)
4. Nom de la base de données : `vendor_db`
5. Cliquez sur "Create Project"

### 3. Récupérer les informations de connexion
Après la création, vous obtiendrez :
- Host/Endpoint
- Database name
- Username
- Password

### 4. Mettre à jour application.properties
Mettez à jour le fichier `src/main/resources/application.properties` avec vos informations :

```properties
spring.datasource.url=jdbc:postgresql://[VOTRE_ENDPOINT]:5432/vendor_db?sslmode=require
spring.datasource.username=[VOTRE_USERNAME]
spring.datasource.password=[VOTRE_PASSWORD]
```

### 5. Vérification de la configuration
Le microservice créera automatiquement la table `vendors` au premier démarrage grâce à :
```properties
spring.jpa.hibernate.ddl-auto=update
```

## Structure de la table qui sera créée automatiquement

```sql
CREATE TABLE vendors (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(255),
    address VARCHAR(255),
    city VARCHAR(255),
    country VARCHAR(255),
    postal_code VARCHAR(255),
    tax_id VARCHAR(255),
    is_active BOOLEAN DEFAULT true,
    rating DOUBLE PRECISION,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

## Commandes SQL utiles (optionnel)

### Vérifier la table créée
```sql
SELECT * FROM vendors;
```

### Créer un index sur l'email pour améliorer les performances
```sql
CREATE INDEX idx_vendors_email ON vendors(email);
CREATE INDEX idx_vendors_city ON vendors(city);
CREATE INDEX idx_vendors_is_active ON vendors(is_active);
```

### Insérer des données de test
```sql
INSERT INTO vendors (name, description, email, phone, address, city, country, postal_code, tax_id, is_active, rating, created_at, updated_at)
VALUES 
('SoukScan Suppliers', 'Fournisseur principal', 'contact@soukscan.com', '+212522000000', 'Zone Industrielle', 'Casablanca', 'Maroc', '20250', 'MAR123456', true, 4.8, NOW(), NOW()),
('Atlas Trading', 'Importateur et distributeur', 'info@atlas-trading.ma', '+212537000000', 'Avenue Hassan II', 'Rabat', 'Maroc', '10000', 'MAR987654', true, 4.2, NOW(), NOW());
```

## Notes importantes

1. **Sécurité** : Ne jamais commit les credentials dans Git
2. **SSL** : Neon requiert SSL, d'où le paramètre `sslmode=require`
3. **Connection Pooling** : Neon fournit un pooler automatique
4. **Quotas** : Vérifiez les limites de votre plan Neon (nombre de connexions, stockage)

## Troubleshooting

### Erreur de connexion SSL
Si vous avez une erreur SSL, ajoutez `&channel_binding=require` :
```
jdbc:postgresql://[ENDPOINT]:5432/vendor_db?sslmode=require&channel_binding=require
```

### Timeout de connexion
Vérifiez que votre IP est autorisée dans les paramètres de sécurité de Neon.

### Table non créée
Vérifiez que :
- `spring.jpa.hibernate.ddl-auto=update` est bien configuré
- Les logs de démarrage n'affichent pas d'erreurs
- L'utilisateur a les permissions nécessaires

