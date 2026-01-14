@echo off
chcp 65001 >nul
echo ========================================
echo   Компиляция проекта
echo ========================================
echo.

REM Создание директории для классов
if not exist target\classes (
    echo Создание директории target\classes...
    mkdir target\classes
)

echo Компиляция проекта...
javac -d target/classes -encoding UTF-8 -sourcepath src/main src/main/*.java src/main/model/*.java src/main/service/*.java src/main/util/*.java

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ Ошибка компиляции!
    pause
    exit /b 1
)

echo.
echo ✅ Компиляция успешна!
echo Для запуска используйте: java -cp target/classes UrlShortenerApp
echo.
pause
