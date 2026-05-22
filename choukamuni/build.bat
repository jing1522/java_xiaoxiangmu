@echo off
chcp 65001 >nul
cd /d "%~dp0"
javac -encoding UTF-8 -d out src/gacha/*.java
if %errorlevel% neq 0 (
    echo Compile failed
    pause
    exit /b
)
start javaw -cp out gacha.GachaSimulator
