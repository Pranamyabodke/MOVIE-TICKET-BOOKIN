@echo off
title Push CineShow to GitHub
color 0B

REM Add MinGit to PATH for this session
set "PATH=%PATH%;%LOCALAPPDATA%\Programs\MinGit\cmd"

echo =================================================================
echo           PUSHING CINESHOW PROJECT TO GITHUB
echo =================================================================
echo.
echo Target Repository:
echo https://github.com/Pranamyabodke/MOVIE-TICKET-BOOKIN
echo.
echo Pushing branch 'main' to GitHub...
echo (If a browser window or login prompt appears, please sign in)
echo.

git push -u origin main

echo.
if %errorlevel% equ 0 (
    echo =================================================================
    echo  SUCCESS! All files are now hosted on your GitHub:
    echo  https://github.com/Pranamyabodke/MOVIE-TICKET-BOOKIN
    echo =================================================================
) else (
    echo [NOTE] If you saw an authentication prompt, make sure you are signed in.
)

echo.
pause

