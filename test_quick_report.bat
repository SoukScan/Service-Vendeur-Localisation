@echo off
echo Testing Quick Shop Report API...

echo.
echo Step 1: Finding nearby shops
curl -X GET "http://localhost:8081/api/quick-report/nearby-shops?productId=1&lat=33.5899&lon=-7.6034&radius=50" ^
  -H "Content-Type: application/json"

echo.
echo.
echo Step 2: Creating a quick shop report
curl -X POST "http://localhost:8081/api/quick-report" ^
  -H "Content-Type: application/json" ^
  -d "{\"productId\": 1, \"price\": 25.75, \"latitude\": 33.5899, \"longitude\": -7.6034, \"userId\": 102, \"vendorId\": 7, \"searchRadiusMeters\": 50}"

echo.
echo.
echo Step 3: Checking if price report was created in price_reports table
curl -X GET "http://localhost:8081/api/price-reports/product/1" ^
  -H "Content-Type: application/json"

echo.
echo.
echo Step 4: Checking price averages
curl -X GET "http://localhost:8081/api/price-averages/1" ^
  -H "Content-Type: application/json"

pause