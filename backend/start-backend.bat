@echo off
chcp 65001 >nul
cd /d %~dp0
echo ============================================================
echo  AI Mall Backend Startup
echo  First run will auto-download Maven (please wait)
echo  Optional: set OPENAI_API_KEY and OPENAI_MODEL=gpt-5.6 before starting
echo ============================================================
echo [1/3] Cleaning...
call mvnw.cmd clean
if errorlevel 1 (
  echo [ERROR] Clean failed.
  pause
  exit /b 1
)
echo [2/3] Building jar (skip tests)...
call mvnw.cmd package -DskipTests
if errorlevel 1 (
  echo [ERROR] Package failed.
  pause
  exit /b 1
)
echo [3/3] Starting backend...
java -jar target\ai-mall-backend-1.0.0.jar
echo.
echo Backend exited. If startup failed, check the error above.
pause
