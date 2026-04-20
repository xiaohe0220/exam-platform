@echo off
chcp 65001 >nul
cd /d "%~dp0..\frontend"
echo Starting Vite on http://localhost:5173
echo.
call npm run dev
pause
