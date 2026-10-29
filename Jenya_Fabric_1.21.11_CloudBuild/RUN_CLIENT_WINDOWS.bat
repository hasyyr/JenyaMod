@echo off
chcp 65001 >nul
setlocal
cd /d "%~dp0"
where gradle >nul 2>nul
if errorlevel 1 (
  echo [ОШИБКА] Gradle не найден. Открой проект в IntelliJ IDEA и запусти задачу runClient.
  pause
  exit /b 1
)
gradle runClient --stacktrace
pause
