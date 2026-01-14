@echo off
chcp 65001 >nul
echo ========================================
echo   Запуск сервиса сокращения ссылок
echo ========================================
echo.

if not exist target\classes (
    echo ❌ Проект не скомпилирован!
    echo Запустите compile.bat для компиляции
    echo.
    pause
    exit /b 1
)

echo Запуск приложения...
echo.
java -cp target/classes UrlShortenerApp

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ Ошибка при запуске приложения!
    pause
    exit /b 1
)

pause
