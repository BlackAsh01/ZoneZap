# Start Firebase Emulators with Data Persistence
# This script imports existing data and exports on exit

Write-Host "Starting Firebase Emulators with data persistence..." -ForegroundColor Green
Write-Host "Data will be imported from: .firebase/emulator-export" -ForegroundColor Yellow
Write-Host "Data will be exported on exit to: .firebase/emulator-export" -ForegroundColor Yellow
Write-Host ""

# Create export directory if it doesn't exist
if (-not (Test-Path ".firebase/emulator-export")) {
    New-Item -ItemType Directory -Path ".firebase/emulator-export" -Force | Out-Null
    Write-Host "Created export directory: .firebase/emulator-export" -ForegroundColor Cyan
}

# Start emulators with import and export
firebase emulators:start --import=.firebase/emulator-export --export-on-exit=.firebase/emulator-export
