@echo off
title CineShow - Movie Ticket Booking System
color 0A

echo =================================================================
echo        CINESHOW - MOVIE TICKET BOOKING SYSTEM
echo           (College Micro-Project - 2nd Year)
echo =================================================================
echo.

REM 1. Verify Java Installation
where javac >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] 'javac' (Java Compiler) was not found in your PATH!
    echo Please ensure Java JDK 11, 17, or 21 is installed.
    pause
    exit /b 1
)

REM 2. Create bin folder if not exists
if not exist "bin" mkdir bin

echo [1/3] Compiling Java source files...
javac -encoding UTF-8 -d bin src\main\java\com\movieticket\util\SimpleJson.java src\main\java\com\movieticket\model\*.java src\main\java\com\movieticket\storage\DataStore.java src\main\java\com\movieticket\server\*.java src\main\java\com\movieticket\Main.java

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Compilation failed. Please inspect the errors above.
    pause
    exit /b 1
)

echo [2/3] Compilation successful!
echo.
echo [3/3] Starting CineShow Web Server on http://localhost:8080 ...
echo.
echo Opening browser in 2 seconds...
start "" http://localhost:8080

java -cp bin com.movieticket.Main

pause


