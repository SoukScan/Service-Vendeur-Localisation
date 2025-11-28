@echo off
echo ========================================
echo   Test de demarrage - Microservice Vendor
echo ========================================
echo.
echo Verification de la configuration...
echo - Mode Hibernate: UPDATE (modification auto du schema)
echo - Port: 8081
echo - Base de donnees: vendor_db (Neon PostgreSQL)
echo.
echo ========================================
echo   Tentative de demarrage...
echo ========================================
echo.

cd /d "%~dp0"

call mvnw.cmd clean spring-boot:run

echo.
echo ========================================
if %ERRORLEVEL% EQU 0 (
    echo   SUCCES - Application demarree !
) else (
    echo   ERREUR - Voir TROUBLESHOOTING.md
    echo.
    echo Solutions possibles:
    echo 1. Verifier la connexion a la base de donnees
    echo 2. Executer fix-schema.sql sur Neon
    echo 3. Voir TROUBLESHOOTING.md pour plus de details
)
echo ========================================
echo.

pause

