# PowerShell script to start AI Engine training and prediction service
# This trains the model on Firebase data and sets up prediction monitoring

Write-Host "Starting ZoneZap AI Engine..." -ForegroundColor Cyan
Write-Host ""

# Navigate to ai-engine directory
$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptPath

# Check if Python is installed
$pythonInstalled = Get-Command python -ErrorAction SilentlyContinue
if (-not $pythonInstalled) {
    Write-Host "Python not found. Please install Python 3.8+ first." -ForegroundColor Red
    exit 1
}

# Check if virtual environment exists
if (-not (Test-Path ".venv")) {
    Write-Host "Creating Python virtual environment..." -ForegroundColor Yellow
    python -m venv .venv
}

# Activate virtual environment
Write-Host "Activating virtual environment..." -ForegroundColor Cyan
& ".venv\Scripts\Activate.ps1"

# Check if dependencies are installed
if (-not (Test-Path ".venv\Lib\site-packages\sklearn")) {
    Write-Host "Installing Python dependencies..." -ForegroundColor Yellow
    pip install -r requirements.txt
}

Write-Host ""
Write-Host "AI Engine Options:" -ForegroundColor Green
Write-Host "  1. Train model on Firebase data (Recommended)" -ForegroundColor Cyan
Write-Host "  2. Train model on CSV file" -ForegroundColor Cyan
Write-Host "  3. Run predictions (requires trained model)" -ForegroundColor Cyan
Write-Host "  4. Train with Firebase emulator (local testing)" -ForegroundColor Cyan
Write-Host ""

$choice = Read-Host "Select option (1-4)"

switch ($choice) {
    "1" {
        Write-Host ""
        Write-Host "Training on Firebase data..." -ForegroundColor Green
        Write-Host "Make sure you have firebase-service-account.json in this directory" -ForegroundColor Yellow
        Write-Host ""
        python train_with_firebase.py
    }
    "2" {
        Write-Host ""
        $csvFile = Read-Host "Enter CSV file path (or press Enter for movement.csv)"
        if ([string]::IsNullOrWhiteSpace($csvFile)) {
            $csvFile = "movement.csv"
        }
        Write-Host "Training on CSV: $csvFile" -ForegroundColor Green
        python train.py --csv $csvFile
    }
    "3" {
        Write-Host ""
        if (-not (Test-Path "model.pkl")) {
            Write-Host "Error: model.pkl not found. Please train the model first." -ForegroundColor Red
            exit 1
        }
        Write-Host "Running predictions..." -ForegroundColor Green
        python predict.py
    }
    "4" {
        Write-Host ""
        Write-Host "Training with Firebase emulator..." -ForegroundColor Green
        Write-Host "Make sure Firebase emulators are running on localhost:8080" -ForegroundColor Yellow
        $env:FIRESTORE_EMULATOR_HOST = "localhost:8080"
        python train.py
    }
    default {
        Write-Host "Invalid option. Exiting." -ForegroundColor Red
        exit 1
    }
}

Write-Host ""
Write-Host "AI Engine process completed!" -ForegroundColor Green
