# Starting Firebase Emulators - Step by Step

## Prerequisites Check

### 1. Check if Firebase CLI is installed:
```powershell
firebase --version
```

If not installed, install it:
```powershell
npm install -g firebase-tools
```

### 2. Check if you're logged in:
```powershell
firebase login
```

If not logged in, it will open a browser for authentication.

### 3. Navigate to backend directory:
```powershell
cd "C:\Users\ashwi\OneDrive - School of Management Studies, Vels University(VISTAS), (Estd. u s 3 of the UGC Act 1956)\Desktop\Project - Shiv\backend"
```

## Start Emulators

### Option 1: Start All Emulators (Recommended)
```powershell
firebase emulators:start
```

This starts:
- Firestore on port 8080
- Functions on port 5001
- UI Dashboard on port 4000

### Option 2: Start Specific Emulators Only
```powershell
firebase emulators:start --only firestore,functions
```

### Option 3: Start with Custom Ports
```powershell
firebase emulators:start --only firestore:8080,functions:5001
```

## Verify It's Running

After starting, you should see output like:
```
✔  firestore: Emulator started at http://localhost:8080
✔  functions: Emulator started at http://localhost:5001
✔  ui: Emulator UI started at http://localhost:4000
```

Then open: **http://localhost:4000**

## Troubleshooting

### Port Already in Use

If you get "port already in use" error:

**Check what's using the port:**
```powershell
netstat -ano | findstr :4000
netstat -ano | findstr :8080
netstat -ano | findstr :5001
```

**Kill the process (replace PID with actual process ID):**
```powershell
taskkill /PID <PID> /F
```

**Or use different ports:**
```powershell
firebase emulators:start --only firestore:8081,functions:5002
```

### Firebase Not Initialized

If you get "Firebase not initialized" error:

```powershell
firebase init emulators
```

Select:
- Firestore: Yes
- Functions: Yes
- UI: Yes

### Functions Dependencies Not Installed

```powershell
cd functions
npm install
cd ..
firebase emulators:start
```

## Quick Start Script

Save this as `start-emulators.ps1` in the backend folder:

```powershell
# Navigate to backend directory
$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptPath

# Check Firebase CLI
$firebaseInstalled = Get-Command firebase -ErrorAction SilentlyContinue
if (-not $firebaseInstalled) {
    Write-Host "Installing Firebase CLI..." -ForegroundColor Yellow
    npm install -g firebase-tools
}

# Check if logged in
Write-Host "Checking Firebase login..." -ForegroundColor Cyan
firebase projects:list 2>&1 | Out-Null
if ($LASTEXITCODE -ne 0) {
    Write-Host "Please login to Firebase:" -ForegroundColor Yellow
    firebase login
}

# Install function dependencies if needed
if (-not (Test-Path "functions\node_modules")) {
    Write-Host "Installing dependencies..." -ForegroundColor Yellow
    Set-Location functions
    npm install
    Set-Location ..
}

# Start emulators
Write-Host "Starting Firebase Emulators..." -ForegroundColor Green
Write-Host "UI Dashboard: http://localhost:4000" -ForegroundColor Cyan
firebase emulators:start
```

Run it:
```powershell
cd backend
.\start-emulators.ps1
```
