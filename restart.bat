@echo off
echo ========================================
echo   REDEMARRAGE - Microservice Vendor
echo   Correction CORS appliquee
echo ========================================
echo.
echo Arret des processus Java en cours...
taskkill /F /IM java.exe 2>nul
timeout /t 2 /nobreak >nul
echo.
echo ========================================
echo   Demarrage de l'application...
echo ========================================
echo.

cd /d "%~dp0"

call mvnw.cmd spring-boot:run

pause

