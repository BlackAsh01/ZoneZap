# PowerShell script to start Firebase backend services
# This starts Firebase emulators for local development

Write-Host "Starting Firebase Backend Services..." -ForegroundColor Cyan
Write-Host ""

# Check if Firebase CLI is installed
$firebaseInstalled = Get-Command firebase -ErrorAction SilentlyContinue
if (-not $firebaseInstalled) {
    Write-Host "Firebase CLI not found. Installing..." -ForegroundColor Yellow
    npm install -g firebase-tools
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Failed to install Firebase CLI. Please install manually:" -ForegroundColor Red
        Write-Host "npm install -g firebase-tools" -ForegroundColor Yellow
        exit 1
    }
}

# Check if logged in to Firebase
Write-Host "Checking Firebase login status..." -ForegroundColor Cyan
$loginCheck = firebase projects:list 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "Not logged in to Firebase. Please login:" -ForegroundColor Yellow
    Write-Host "firebase login" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Or use emulator mode (no login required):" -ForegroundColor Cyan
    Write-Host "firebase emulators:start --only firestore,functions" -ForegroundColor Green
    exit 1
}

# Navigate to backend directory
$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptPath

# Check if functions dependencies are installed
if (-not (Test-Path "functions\node_modules")) {
    Write-Host "Installing Cloud Functions dependencies..." -ForegroundColor Yellow
    Set-Location functions
    npm install
    Set-Location ..
}

Write-Host ""
Write-Host "Starting Firebase Emulators..." -ForegroundColor Green
Write-Host "  - Firestore: http://localhost:8080" -ForegroundColor Cyan
Write-Host "  - Functions: http://localhost:5001" -ForegroundColor Cyan
Write-Host "  - UI Dashboard: http://localhost:4000" -ForegroundColor Cyan
Write-Host ""
Write-Host "Press Ctrl+C to stop the emulators" -ForegroundColor Yellow
Write-Host ""

# Start emulators
firebase emulators:start
