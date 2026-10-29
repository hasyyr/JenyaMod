@echo off
chcp 65001 >nul
setlocal
cd /d "%~dp0"
echo ==========================================
echo  JENYA MOD - BUILD (Fabric 1.21.11)
echo ==========================================
where java >nul 2>nul
if errorlevel 1 (
  echo [ОШИБКА] Java не найдена. Установи JDK 21 и добавь java в PATH.
  pause
  exit /b 1
)
where gradle >nul 2>nul
if errorlevel 1 (
  echo [ОШИБКА] Команда gradle не найдена.
  echo Открой проект через IntelliJ IDEA и выполни Gradle build,
  echo либо установи Gradle 8.x/совместимую версию для Loom из документации.
  echo Этот проект пока не содержит gradle-wrapper.jar.
  pause
  exit /b 1
)
python tools\validate_project.py
if errorlevel 1 (
  echo [ОШИБКА] Проверка ресурсов не пройдена. Смотри сообщения выше.
  pause
  exit /b 1
)
gradle clean build --stacktrace
if errorlevel 1 (
  echo [ОШИБКА] Сборка не прошла. Скопируй полный лог и пришли Жене.
  pause
  exit /b 1
)
echo.
echo [ГОТОВО] Проверь JAR в build\libs\
pause
