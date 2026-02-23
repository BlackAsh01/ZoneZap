# Script to add Android Studio's Java to PATH for Firebase emulators
# Run this script, then restart PowerShell

Write-Host "Adding Android Studio Java to PATH..." -ForegroundColor Cyan

$javaPath = "C:\Program Files\Android\Android Studio\jbr\bin"

# Verify Java exists
if (-not (Test-Path "$javaPath\java.exe")) {
    Write-Host "Error: Java not found at $javaPath" -ForegroundColor Red
    exit 1
}

Write-Host "Found Java at: $javaPath" -ForegroundColor Green

# Test Java version
& "$javaPath\java.exe" -version

# Add to PATH for current session
Write-Host "`nAdding to PATH for current session..." -ForegroundColor Yellow
$env:PATH = "$javaPath;$env:PATH"

# Verify it works
Write-Host "`nTesting Java command..." -ForegroundColor Yellow
java -version

Write-Host "`n✅ Java added to PATH for this session!" -ForegroundColor Green
Write-Host "`nTo make this permanent:" -ForegroundColor Yellow
Write-Host "1. Press Win + X → System → Advanced system settings" -ForegroundColor Cyan
Write-Host "2. Click 'Environment Variables'" -ForegroundColor Cyan
Write-Host "3. Under 'System variables', find 'Path' and click 'Edit'" -ForegroundColor Cyan
Write-Host "4. Click 'New' and add: $javaPath" -ForegroundColor Cyan
Write-Host "5. Click OK and restart PowerShell" -ForegroundColor Cyan

Write-Host "`nOr run this command as Administrator to add permanently:" -ForegroundColor Yellow
Write-Host "[System.Environment]::SetEnvironmentVariable('Path', [System.Environment]::GetEnvironmentVariable('Path', 'Machine') + ';$javaPath', 'Machine')" -ForegroundColor Cyan
