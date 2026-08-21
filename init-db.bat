@echo off
chcp 65001 >nul
cd /d %~dp0

echo ============================================================
echo  AI Mall Database Init
echo  This will import init.sql and catalog-expansion.sql
echo ============================================================

where mysql >nul 2>&1
if errorlevel 1 (
  echo [ERROR] mysql command not found in PATH.
  echo Please use Navicat or add MySQL bin to PATH.
  pause
  exit /b 1
)

set /p MYSQL_PWD=Enter MySQL root password: 

mysql -u root -p%MYSQL_PWD% < "backend\src\main\resources\sql\init.sql"

if errorlevel 1 goto :import_failed

mysql -u root -p%MYSQL_PWD% < "backend\src\main\resources\sql\catalog-expansion.sql"

if errorlevel 1 goto import_failed

echo [OK] Database imported successfully.
goto import_done

:import_failed
echo [ERROR] Database import failed.

:import_done
pause
