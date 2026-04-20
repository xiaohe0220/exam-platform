@echo off
chcp 65001 >nul
cd /d "%~dp0..\backend"
echo Starting Spring Boot on http://localhost:8080
echo.
mvn spring-boot:run
pause
