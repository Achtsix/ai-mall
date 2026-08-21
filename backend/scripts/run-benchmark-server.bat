@echo off
setlocal
chcp 65001 >nul

if not exist "%~dp0..\local-env.bat" (
  echo Missing backend\local-env.bat. Configure local credentials before running the benchmark server.
  exit /b 1
)

call "%~dp0..\local-env.bat"
set "SPRING_DATASOURCE_URL=jdbc:mysql://127.0.0.1:3306/ai_mall_benchmark?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true"
set "SERVER_PORT=18080"
set "RUN_BENCHMARK=true"
set "RUN_AI_BENCHMARK=false"

set "MAVEN_CMD=mvn.cmd"
where mvn.cmd >nul 2>nul
if errorlevel 1 set "MAVEN_CMD=%USERPROFILE%\.m2\wrapper\apache-maven-3.9.9\bin\mvn.cmd"

echo Rebuilding isolated benchmark database...
call "%MAVEN_CMD%" -q -Dtest=BenchmarkIntegrationTest test
if errorlevel 1 exit /b 1

call "%MAVEN_CMD%" -q -DskipTests compile dependency:copy-dependencies -DoutputDirectory=target/benchmark-dependency
if errorlevel 1 exit /b 1
java -cp "%~dp0..\target\classes;%~dp0..\target\benchmark-dependency\*" com.aimall.AimallApplication
