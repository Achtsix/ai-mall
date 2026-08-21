@echo off
chcp 65001 >nul
cd /d %~dp0
echo ============================================================
echo  AI Mall Frontend Startup
echo ============================================================

where node >nul 2>&1
if errorlevel 1 (
  echo [ERROR] Node.js not found. Please install Node.js 18 or newer.
  echo Download: https://nodejs.org/
  pause
  exit /b 1
)

if not exist node_modules (
  echo [INFO] Installing frontend dependencies, first run may take minutes...
  call npm install
  if errorlevel 1 (
    echo [ERROR] npm install failed. Check your network.
    pause
    exit /b 1
  )
)

echo [INFO] Starting frontend dev server: http://localhost:5173
call npm run dev
pause
