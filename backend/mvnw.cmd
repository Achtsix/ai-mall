@echo off
setlocal EnableDelayedExpansion
chcp 65001 >nul

REM ============================================================
REM  AI Mall Maven Wrapper
REM  First run will auto-download Maven 3.9.9
REM ============================================================

set "MAVEN_VERSION=3.9.9"
set "MAVEN_BASE=%USERPROFILE%\.m2\wrapper"
set "MAVEN_HOME=%MAVEN_BASE%\apache-maven-%MAVEN_VERSION%"
set "MAVEN_BIN=%MAVEN_HOME%\bin\mvn.cmd"
set "MAVEN_ZIP=%MAVEN_BASE%\apache-maven-%MAVEN_VERSION%-bin.zip"

REM ---- Check Java ----
where java >nul 2>&1
if errorlevel 1 (
  echo [ERROR] Java not found. Please install JDK 17 or 21 first.
  pause
  exit /b 1
)

REM ---- Auto download Maven ----
if not exist "%MAVEN_BIN%" (
  echo [INFO] Downloading Maven %MAVEN_VERSION%, first run only...
  if not exist "%MAVEN_BASE%" mkdir "%MAVEN_BASE%"
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; [Net.ServicePointManager]::SecurityProtocol=[Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://archive.apache.org/dist/maven/maven-3/%MAVEN_VERSION%/binaries/apache-maven-%MAVEN_VERSION%-bin.zip' -OutFile '%MAVEN_ZIP%'"
  if errorlevel 1 (
    echo [ERROR] Maven download failed. Check your network and retry.
    pause
    exit /b 1
  )
  echo [INFO] Extracting Maven...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; Expand-Archive -Path '%MAVEN_ZIP%' -DestinationPath '%MAVEN_BASE%' -Force"
  if errorlevel 1 (
    echo [ERROR] Maven extract failed.
    pause
    exit /b 1
  )
)

REM ---- Auto detect JAVA_HOME ----
set "FOUND_JAVA="
if exist "%JAVA_HOME%\bin\java.exe" (
  set "FOUND_JAVA=1"
) else (
  for /f "usebackq delims=" %%i in (`powershell -NoProfile -Command "(Get-Command java).Source"`) do set "JAVA_EXE=%%i"
  for %%i in ("%JAVA_EXE%") do set "JAVA_BIN_DIR=%%~dpi"
  for %%i in ("%JAVA_BIN_DIR%..") do set "JAVA_HOME=%%~fi"
  if exist "%JAVA_HOME%\bin\java.exe" set "FOUND_JAVA=1"
)

if not defined FOUND_JAVA (
  echo [ERROR] JAVA_HOME is invalid. Please install JDK 17 or 21.
  pause
  exit /b 1
)

set "PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%"

echo [INFO] JAVA_HOME = %JAVA_HOME%
echo [INFO] Maven    = %MAVEN_HOME%
echo [INFO] Running: mvn %*
echo.

call "%MAVEN_BIN%" -U %*
set "EXIT_CODE=%errorlevel%"
echo.
if not "%EXIT_CODE%"=="0" (
  echo [ERROR] Maven command failed, exit code: %EXIT_CODE%
)
exit /b %EXIT_CODE%
