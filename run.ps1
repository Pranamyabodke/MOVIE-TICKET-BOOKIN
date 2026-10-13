Write-Host "=================================================================" -ForegroundColor Cyan
Write-Host "       🎬  CINESHOW - MOVIE TICKET BOOKING SYSTEM  🎬            " -ForegroundColor Yellow
Write-Host "          (College Micro-Project - 2nd Year)                     " -ForegroundColor Green
Write-Host "=================================================================" -ForegroundColor Cyan

# 1. Check javac
if (-not (Get-Command javac -ErrorAction SilentlyContinue)) {
    Write-Host "[ERROR] 'javac' (Java Compiler) was not found in PATH." -ForegroundColor Red
    Write-Host "Please ensure JDK 11, 17, or 21 is installed."
    Exit 1
}

# 2. Prepare bin folder
if (-not (Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

Write-Host "`n[1/3] Compiling Java backend classes..." -ForegroundColor Yellow
$javaFiles = Get-ChildItem -Recurse -Filter *.java -Path "src/main/java" | Select-Object -ExpandProperty FullName
javac -encoding UTF-8 -d bin $javaFiles

if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Compilation failed!" -ForegroundColor Red
    Exit 1
}

Write-Host "[2/3] Compilation successful!" -ForegroundColor Green
Write-Host "`n[3/3] Starting CineShow Web Server at http://localhost:8080 ..." -ForegroundColor Cyan

Start-Process "http://localhost:8080"
java -cp bin com.movieticket.Main

