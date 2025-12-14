@echo off
echo ========================================
echo   Demarrage du microservice Vendor
echo   Port: 8081
echo ========================================
echo.

cd /d "%~dp0"

echo Compilation et demarrage de l'application...
echo.

call mvnw.cmd clean spring-boot:run

pause

