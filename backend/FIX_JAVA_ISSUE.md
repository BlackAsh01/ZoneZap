# Fix: Java Not Found Error

## ✅ Solution Found!

Java is installed with Android Studio at:
```
C:\Program Files\Android\Android Studio\jbr\bin\java.exe
```

It just needs to be added to your PATH.

## 🚀 Quick Fix (Temporary - Current Session Only)

Run these commands in PowerShell:

```powershell
# Add Java to PATH for this session
$env:PATH = "C:\Program Files\Android\Android Studio\jbr\bin;$env:PATH"

# Verify it works
java -version

# Now start emulators
cd "C:\Users\ashwi\OneDrive - School of Management Studies, Vels University(VISTAS), (Estd. u s 3 of the UGC Act 1956)\Desktop\Project - Shiv\backend"
firebase emulators:start
```

## 🔧 Permanent Fix (Recommended)

Add Java to PATH permanently so you don't have to do this every time:

### Method 1: Using PowerShell (Run as Administrator)

```powershell
# Add Java to system PATH permanently
$javaPath = "C:\Program Files\Android\Android Studio\jbr\bin"
$currentPath = [System.Environment]::GetEnvironmentVariable("Path", "Machine")
if ($currentPath -notlike "*$javaPath*") {
    [System.Environment]::SetEnvironmentVariable("Path", "$currentPath;$javaPath", "Machine")
    Write-Host "✅ Java added to PATH permanently" -ForegroundColor Green
    Write-Host "Please restart PowerShell for changes to take effect" -ForegroundColor Yellow
} else {
    Write-Host "Java already in PATH" -ForegroundColor Green
}
```

**Important:** Restart PowerShell after running this!

### Method 2: Manual GUI Method

1. Press `Win + X` → **System**
2. Click **Advanced system settings**
3. Click **Environment Variables**
4. Under **System variables**, find **Path** and click **Edit**
5. Click **New**
6. Add: `C:\Program Files\Android\Android Studio\jbr\bin`
7. Click **OK** on all dialogs
8. **Restart PowerShell**

## ✅ Verify It Works

After adding to PATH (and restarting PowerShell):

```powershell
java -version
```

You should see:
```
openjdk version "21.0.8" ...
```

Then start emulators:
```powershell
cd "C:\Users\ashwi\OneDrive - School of Management Studies, Vels University(VISTAS), (Estd. u s 3 of the UGC Act 1956)\Desktop\Project - Shiv\backend"
firebase emulators:start
```

## 🎯 Using the Updated Script

The `start-emulators.ps1` script now automatically adds Java to PATH, so you can just run:

```powershell
cd "C:\Users\ashwi\OneDrive - School of Management Studies, Vels University(VISTAS), (Estd. u s 3 of the UGC Act 1956)\Desktop\Project - Shiv\backend"
.\start-emulators.ps1
```

It will handle Java automatically!

## 📝 Summary

- ✅ Java is installed (Android Studio includes it)
- ✅ Just needs to be added to PATH
- ✅ Use temporary fix for now, or permanent fix for convenience
- ✅ Updated script handles it automatically
