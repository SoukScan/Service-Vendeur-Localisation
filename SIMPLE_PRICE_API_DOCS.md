# Simple Price Reporting API Documentation

## Overview

This API provides a streamlined interface for price reporting that matches your exact requirements. Users can report product prices at specific locations with automatic proximity validation.

## Core Requirements Met

✅ **Location** - From map click (latitude/longitude)  
✅ **Product** - Pre-selected by user (productId)  
✅ **Price** - User-entered price  
✅ **Shop Name** - Optional field  
✅ **Proximity Validation** - Users must be within 50m of shop to report prices  

## API Endpoint

### POST `/api/price-report`

Report a product price at a specific location.

#### Request Body

```json
{
  "productId": 123,           // Required: ID of the product (pre-selected)
  "price": 15.50,             // Required: Price of the product
  "latitude": 33.5898,        // Required: Location from map click
  "longitude": -7.6038,       // Required: Location from map click  
  "userId": 456,              // Required: ID of the user making the report
  "shopName": "Local Market"  // Optional: Shop name (auto-generated if not provided)
}
```

#### Success Response (200/201)

```json
{
  "vendorId": 789,
  "shopName": "Local Market",
  "latitude": 33.5898,
  "longitude": -7.6038,
  "productId": 123,
  "price": 15.50,
  "isNewShop": false,         // true if new shop was created
  "isNewProduct": true,       // true if product was added to existing shop
  "message": "Price updated for existing product",
  "distanceFromUser": 12.5    // Distance in meters for verification
}
```

#### Error Response (400)

```json
{
  "error": "You are too far from the shop (75.3m away). You must be within 50m to report prices.",
  "timestamp": "2025-11-30T18:06:48.123Z"
}
```

## Behavior Logic

### 1. New Shop Creation
- If no shop exists within 50m of the clicked location
- Creates new shop at exact user coordinates
- Sets user as the first declarant

### 2. Existing Shop Update
- If shop exists within 50m of user location
- **Validates user proximity** - must be within 50m to report
- Updates product price or adds new product to existing shop

### 3. Proximity Validation
- **Maximum distance**: 50 meters
- Applied to both existing shop reporting and new shop creation validation
- Prevents fraudulent remote reporting

## Frontend Integration

```javascript
// Example frontend call
const reportPrice = async (mapClickLocation, selectedProduct, userEnteredPrice, userId, shopName = null) => {
  const response = await fetch('/api/price-report', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      productId: selectedProduct.id,
      price: userEnteredPrice,
      latitude: mapClickLocation.lat,
      longitude: mapClickLocation.lng,
      userId: userId,
      shopName: shopName
    })
  });
  
  if (response.ok) {
    const result = await response.json();
    console.log('Price reported successfully:', result);
    // Update map with new/updated shop
  } else {
    const error = await response.json();
    console.error('Error:', error.error);
    // Show error to user
  }
};
```

## Security Features

- **Location Validation**: Physical proximity required for reporting
- **Product Validation**: Ensures product exists and is active
- **User Tracking**: Users are recorded as declarants for verification

## Health Check

### GET `/api/price-report/health`

Returns service status for monitoring.

```json
{
  "status": "UP",
  "service": "Price Reporting Service"
}
```

## Notes

- The API automatically handles both new shop creation and existing shop updates
- Shop names are auto-generated if not provided: "Shop at 33.5898,-7.6038"
- All shops start with "UNVERIFIED" status and can be verified later
- Users can report multiple products at the same shop location
- Distance calculations use the Haversine formula for accuracy