@echo off
chcp 65001 >nul
cd /d %~dp0
echo ============================================================
echo  AI Mall One-Click Startup
echo  1. Make sure MySQL is running and init.sql is imported
echo  2. Configure DB password and DeepSeek API Key in application.yml
echo ============================================================
echo Opening backend window...
start "AI Mall Backend" cmd /k "cd /d %~dp0backend && start-backend.bat"
echo Opening frontend window...
start "AI Mall Frontend" cmd /k "cd /d %~dp0frontend && start-frontend.bat"
echo.
echo Startup complete:
echo   Backend: http://localhost:8080
echo   Frontend: http://localhost:5173
pause
