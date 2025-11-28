# Vendor-Product Integration

## Nouvelle fonctionnalité ajoutée

Le microservice Vendor peut maintenant communiquer avec le microservice Product pour permettre aux vendors d'ajouter des produits depuis le catalogue général.

## Architecture

```
Vendor Service (8081) <---REST---> Product Service (8082)
         |
         |--- vendor_products (table de liaison)
```

## Entités créées

- **VendorProduct** : Table de liaison entre vendors et produits
  - Champs : vendorId, productId, price, stockQuantity, isAvailable

## Endpoints disponibles

### 1. Catalogue de produits
- `GET /api/vendors/{vendorId}/products/catalog` - Voir tous les produits
- `GET /api/vendors/{vendorId}/products/catalog/search?name=` - Rechercher

### 2. Gestion des produits du vendor
- `POST /api/vendors/{vendorId}/products` - Ajouter un produit
- `GET /api/vendors/{vendorId}/products` - Liste des produits
- `PUT /api/vendors/{vendorId}/products/{vendorProductId}` - Modifier
- `DELETE /api/vendors/{vendorId}/products/{vendorProductId}` - Retirer

## Exemple d'utilisation

### Ajouter un produit au vendor
```json
POST /api/vendors/1/products
{
  "productId": 5,
  "price": 15.50,
  "stockQuantity": 100
}
```

### Voir les produits d'un vendor
```json
GET /api/vendors/1/products
Response: [
  {
    "vendorProductId": 1,
    "productId": 5,
    "productName": "Tomate",
    "price": 15.50,
    "stockQuantity": 100,
    "isAvailable": true
  }
]
```

## Configuration requise

`application.properties` :
```properties
product.service.url=http://localhost:8082
```

## Fichiers créés

1. `entity/VendorProduct.java`
2. `repository/VendorProductRepository.java`
3. `client/ProductClient.java`
4. `service/VendorProductService.java`
5. `controller/VendorProductController.java`
6. `dto/AddProductRequest.java`
7. `dto/VendorProductResponse.java`
8. `api-requests-vendor-products.http`

## Fonctionnalités existantes conservées

✅ Gestion des vendors (CRUD)
✅ Déclaration de shops par utilisateurs
✅ Vérification des shops
✅ Géolocalisation
✅ Gestion des prix
✅ Tous les endpoints existants

