@echo off
echo =======================================================
echo  QUICK SHOP REPORT API - NEW SHOP CREATION GUIDE
echo =======================================================
echo.
echo This script demonstrates how to create a new shop and 
echo report a product price using the Quick Shop Report API.
echo.

echo 1. STEP 1: Check for nearby shops (optional)
echo    This verifies no shops exist in your area
echo.
curl -X GET "http://localhost:8081/api/quick-report/nearby-shops?productId=5&lat=50.1234&lon=3.5678&radius=100" ^
  -H "Content-Type: application/json"

echo.
echo.
echo 2. STEP 2: Create new shop with product price report
echo    vendorId: null (this creates a new shop)
echo    Include shop details and coordinates
echo.
curl -X POST "http://localhost:8081/api/quick-report" ^
  -H "Content-Type: application/json" ^
  -d "{\"productId\": 5, \"price\": 129.99, \"latitude\": 50.1234, \"longitude\": 3.5678, \"userId\": 700, \"vendorId\": null, \"searchRadiusMeters\": 50, \"shopName\": \"New Electronics Store\", \"shopAddress\": \"456 Tech Avenue\", \"city\": \"Innovation City\", \"country\": \"Morocco\"}"

echo.
echo.
echo 3. STEP 3: Verify price report was created
echo    Check the price_reports table
echo.
curl -X GET "http://localhost:8081/api/price-reports/product/5" ^
  -H "Content-Type: application/json"

echo.
echo.
echo 4. STEP 4: Check price averages
echo    Verify price_avg table was updated
echo.
curl -X GET "http://localhost:8081/api/price-averages/5" ^
  -H "Content-Type: application/json"

echo.
echo.
echo =======================================================
echo  KEY REQUIREMENTS FOR NEW SHOP CREATION:
echo =======================================================
echo - vendorId: null (required for new shop)
echo - productId: valid product ID (1-10 for testing)
echo - price: decimal price value
echo - latitude/longitude: exact coordinates
echo - userId: user reporting the shop
echo - searchRadiusMeters: proximity check radius
echo - shopName: optional shop name
echo - shopAddress: optional address
echo - city: optional city
echo - country: optional country
echo.
echo The system will:
echo 1. Verify no existing shops within radius
echo 2. Create new vendor in 'vendors' table
echo 3. Create corresponding location in 'locations' table
echo 4. Add product to 'vendor_products' table
echo 5. Create price report in 'price_reports' table
echo 6. Update average in 'price_avg' table
echo.
pause