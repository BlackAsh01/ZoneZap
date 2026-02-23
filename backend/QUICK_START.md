# Quick Start - Firebase Emulators

## 🚀 Fastest Way (Copy & Paste)

Open PowerShell and run these commands one by one:

```powershell
# 1. Navigate to backend directory
cd "C:\Users\ashwi\OneDrive - School of Management Studies, Vels University(VISTAS), (Estd. u s 3 of the UGC Act 1956)\Desktop\Project - Shiv\backend"

# 2. Check Firebase CLI (install if needed)
firebase --version
# If error, run: npm install -g firebase-tools

# 3. Install function dependencies (if not done)
cd functions
npm install
cd ..

# 4. Start emulators
firebase emulators:start
```

## 📋 Step-by-Step Commands

### Step 1: Open PowerShell Terminal
Press `Win + X` and select "Windows PowerShell" or "Terminal"

### Step 2: Navigate to Backend Folder
```powershell
cd "C:\Users\ashwi\OneDrive - School of Management Studies, Vels University(VISTAS), (Estd. u s 3 of the UGC Act 1956)\Desktop\Project - Shiv\backend"
```

### Step 3: Check Firebase CLI
```powershell
firebase --version
```

**If you see an error**, install Firebase CLI:
```powershell
npm install -g firebase-tools
```

### Step 4: Login to Firebase (Optional - emulators work without login)
```powershell
firebase login
```

**Note:** You can skip login if you only want to use emulators locally.

### Step 5: Install Dependencies (if not already done)
```powershell
cd functions
npm install
cd ..
```

### Step 6: Start Emulators
```powershell
firebase emulators:start
```

### Step 7: Open Dashboard
Once you see:
```
✔  ui: Emulator UI started at http://localhost:4000
```

Open your browser and go to: **http://localhost:4000**

## 🎯 Using the Script (Easier)

Instead of manual commands, use the provided script:

```powershell
cd "C:\Users\ashwi\OneDrive - School of Management Studies, Vels University(VISTAS), (Estd. u s 3 of the UGC Act 1956)\Desktop\Project - Shiv\backend"
.\start-emulators.ps1
```

## ⚠️ Troubleshooting

### Error: "firebase: command not found"
**Solution:**
```powershell
npm install -g firebase-tools
```

### Error: "Port 4000 already in use"
**Solution:** Find and kill the process:
```powershell
netstat -ano | findstr :4000
taskkill /PID <PID_NUMBER> /F
```

### Error: "Cannot find module"
**Solution:**
```powershell
cd functions
npm install
cd ..
```

### Emulators start but UI doesn't load
**Solution:** 
- Check firewall settings
- Try: http://127.0.0.1:4000 instead of localhost:4000
- Check if port 4000 is blocked

## ✅ Success Indicators

When emulators start successfully, you'll see:
```
✔  firestore: Emulator started at http://localhost:8080
✔  functions: Emulator started at http://localhost:5001  
✔  ui: Emulator UI started at http://localhost:4000

┌─────────────────────────────────────────────────────────────┐
│ ✔  All emulators ready! It is now safe to connect.        │
└─────────────────────────────────────────────────────────────┘
```

Then open **http://localhost:4000** in your browser!

## 📝 What You'll See in the UI

- **Firestore**: View and edit database collections
- **Functions**: See function logs and executions
- **Logs**: Real-time logs from all emulators

## 🛑 Stopping Emulators

Press `Ctrl + C` in the terminal where emulators are running.
