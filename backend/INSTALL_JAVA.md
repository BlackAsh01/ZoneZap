# Installing Java for Firebase Emulators

Firebase emulators require Java to run. Here's how to install and configure it.

## 🚀 Quick Solution

### Option 1: Install Java JDK (Recommended)

**Download Java JDK 17 or 21:**
1. Go to: https://adoptium.net/ (or https://www.oracle.com/java/technologies/downloads/)
2. Download **JDK 17** or **JDK 21** for Windows x64
3. Run the installer
4. **Important:** Check "Add to PATH" during installation

**Verify installation:**
```powershell
java -version
```

You should see something like:
```
openjdk version "17.0.x" 2024-xx-xx
```

### Option 2: Use Android Studio's Java (If Already Installed)

If you have Android Studio installed, it includes Java. We just need to add it to PATH.

**Find Android Studio's Java:**
```powershell
# Check common locations
$androidStudioPaths = @(
    "$env:LOCALAPPDATA\Android\Sdk",
    "$env:ProgramFiles\Android\Android Studio\jbr",
    "$env:ProgramFiles\Android\Android Studio\jre",
    "$env:ProgramFiles (x86)\Android\Android Studio\jbr"
)

foreach ($path in $androidStudioPaths) {
    if (Test-Path "$path\bin\java.exe") {
        Write-Host "Found Java at: $path\bin\java.exe"
    }
}
```

**Add to PATH temporarily (current session):**
```powershell
# Replace with actual path found above
$javaPath = "C:\Program Files\Android\Android Studio\jbr\bin"
$env:PATH = "$javaPath;$env:PATH"
java -version
```

**Add to PATH permanently:**
1. Press `Win + X` → System → Advanced system settings
2. Click "Environment Variables"
3. Under "System variables", find "Path" and click "Edit"
4. Click "New" and add: `C:\Program Files\Android\Android Studio\jbr\bin`
5. Click OK on all dialogs
6. **Restart PowerShell** and verify: `java -version`

## 📋 Step-by-Step Installation (Java JDK)

### Step 1: Download Java
- **Eclipse Temurin (Recommended):** https://adoptium.net/
- **Oracle JDK:** https://www.oracle.com/java/technologies/downloads/
- Choose **JDK 17** or **JDK 21** for **Windows x64**

### Step 2: Install
1. Run the downloaded installer
2. **Important:** Check "Add to PATH" option
3. Follow installation wizard
4. Complete installation

### Step 3: Verify
Open **NEW** PowerShell window:
```powershell
java -version
javac -version
```

You should see version information.

### Step 4: Set JAVA_HOME (Optional but Recommended)
```powershell
# Find Java installation path (usually)
$javaHome = "C:\Program Files\Eclipse Adoptium\jdk-17.x.x-hotspot"

# Set JAVA_HOME permanently
[System.Environment]::SetEnvironmentVariable("JAVA_HOME", $javaHome, "Machine")

# Add to PATH if not already added
$currentPath = [System.Environment]::GetEnvironmentVariable("Path", "Machine")
if ($currentPath -notlike "*$javaHome\bin*") {
    [System.Environment]::SetEnvironmentVariable("Path", "$currentPath;$javaHome\bin", "Machine")
}
```

**Restart PowerShell** after setting environment variables.

## ✅ Verify Everything Works

After installing Java:

```powershell
# Check Java version
java -version

# Check JAVA_HOME (if set)
echo $env:JAVA_HOME

# Try starting Firebase emulators
cd "C:\Users\ashwi\OneDrive - School of Management Studies, Vels University(VISTAS), (Estd. u s 3 of the UGC Act 1956)\Desktop\Project - Shiv\backend"
firebase emulators:start
```

## 🐛 Troubleshooting

### "java: command not found" after installation
- **Solution:** Restart PowerShell/terminal
- Or manually add to PATH (see Option 2 above)

### "JAVA_HOME not set"
- Not required, but helpful
- Set it using instructions in Step 4 above

### Wrong Java version
- Firebase emulators work with Java 8+, but Java 17+ is recommended
- Check version: `java -version`

### Still getting errors
- Make sure you restarted PowerShell after installation
- Verify PATH: `$env:PATH -split ';' | Select-String -Pattern "java"`
- Try full path: `"C:\Program Files\Java\jdk-17\bin\java.exe" -version`

## 🎯 Quick Fix Script

Save this as `setup-java.ps1` and run as Administrator:

```powershell
# Check if Java is already available
if (Get-Command java -ErrorAction SilentlyContinue) {
    Write-Host "Java is already installed:" -ForegroundColor Green
    java -version
    exit 0
}

Write-Host "Java not found. Please install Java JDK 17 or 21:" -ForegroundColor Yellow
Write-Host "1. Download from: https://adoptium.net/" -ForegroundColor Cyan
Write-Host "2. Install with 'Add to PATH' option checked" -ForegroundColor Cyan
Write-Host "3. Restart PowerShell and run this script again" -ForegroundColor Cyan
```

## 📝 After Java Installation

Once Java is installed and verified:

```powershell
cd "C:\Users\ashwi\OneDrive - School of Management Studies, Vels University(VISTAS), (Estd. u s 3 of the UGC Act 1956)\Desktop\Project - Shiv\backend"
firebase emulators:start
```

The emulators should start successfully!
