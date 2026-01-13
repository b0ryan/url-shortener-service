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
javac -d target/classes -encoding UTF-8 -sourcepath src/main/java src/main/java/com/urlshortener/*.java src/main/java/com/urlshortener/model/*.java src/main/java/com/urlshortener/service/*.java src/main/java/com/urlshortener/util/*.java

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ Ошибка компиляции!
    pause
    exit /b 1
)

echo.
echo ✅ Компиляция успешна!
echo Для запуска используйте: java -cp target/classes com.urlshortener.UrlShortenerApp
echo.
pause
