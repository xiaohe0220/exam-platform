@echo off
chcp 65001 >nul
cd /d "%~dp0.."
start "exam-backend" cmd /k "cd /d %cd%\backend && mvn spring-boot:run"
timeout /t 2 /nobreak >nul
start "exam-frontend" cmd /k "cd /d %cd%\frontend && npm run dev"
echo.
echo 已尝试打开两个窗口：后端 8080、前端 5173
echo 若窗口一闪而过，请先安装 JDK17、Maven、Node.js 并加入 PATH。
pause
