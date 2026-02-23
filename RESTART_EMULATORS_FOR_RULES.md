# How to Restart Firebase Emulators to Load New Rules

The permission error you're seeing is because Firebase emulators need to be restarted to load the updated `firestore.rules` file.

## 🔄 Quick Fix: Restart Emulators

### Step 1: Stop Current Emulators

1. **Find the terminal/PowerShell window** where emulators are running
2. **Press `Ctrl+C`** to stop them
3. Wait for them to fully stop (you'll see "Emulator shutdown requested")

### Step 2: Restart Emulators

```powershell
cd "C:\Users\ashwi\OneDrive - School of Management Studies, Vels University(VISTAS), (Estd. u s 3 of the UGC Act 1956)\Desktop\Project - Shiv\backend"

# Add Java to PATH (if needed)
$env:PATH = "C:\Program Files\Android\Android Studio\jbr\bin;$env:PATH"

# Start emulators
firebase emulators:start
```

### Step 3: Wait for Startup

Wait until you see:
```
✔  auth: Emulator started at http://localhost:9099
✔  firestore: Emulator started at http://localhost:8080
✔  ui: Emulator UI started at http://localhost:4000
```

### Step 4: Rebuild App

1. **In Android Studio:**
   - **Build → Clean Project**
   - **Build → Rebuild Project**
   - **Run** the app again

## ✅ Verify Rules Are Loaded

After restarting, check the Emulator UI:
1. Open: **http://localhost:4000**
2. Go to **Firestore** tab
3. The rules should now be active

## 🐛 If Error Persists

### Check Rules File Syntax

The rules file should be at: `backend/firestore.rules`

Verify line 41 says:
```firestore
allow list: if request.auth != null;
```

### Check User Authentication

Make sure you're logged in before querying reminders:
- Check Logcat for: `D/LoginActivity: Login successful!`
- Verify `FirebaseConfig.auth.currentUser` is not null

### Manual Rule Reload

If restart doesn't work, try:
1. Stop emulators
2. Delete emulator data (optional):
   ```powershell
   # Emulator data is usually in: %USERPROFILE%\.firebase\emulators
   # You can delete it to start fresh
   ```
3. Restart emulators

## 📝 Current Rules Status

The rules file has been updated with:
- ✅ `allow list` for reminders (line 41)
- ✅ `allow list` for users (line 12)
- ✅ `allow list` for alerts (line 26)
- ✅ `allow list` for movement_logs (line 56)

All should allow authenticated users to query. The error you're seeing means the emulators are still using old cached rules.

## 🎯 After Restart

Once emulators restart with new rules:
- ✅ Reminders queries will work
- ✅ Users queries will work
- ✅ Alerts queries will work
- ✅ Movement logs queries will work

**The rules are correct - just need to restart emulators!**
