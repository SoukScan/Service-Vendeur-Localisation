@echo off
echo ========================================
echo   Compilation du microservice Vendor
echo ========================================
echo.

cd /d "%~dp0"

echo Compilation en cours...
call mvnw.cmd clean package -DskipTests

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo   Compilation reussie!
    echo   Fichier JAR: target\vendorms-0.0.1-SNAPSHOT.jar
    echo ========================================
) else (
    echo.
    echo ========================================
    echo   Erreur de compilation!
    echo ========================================
)

pause

