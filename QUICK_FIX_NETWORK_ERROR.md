# Quick Fix: Network Error When Signing Up

## 🔍 The Problem

You're getting network errors because the app is trying to connect to production Firebase, but:
- Either there's no internet connection
- Or you want to use local Firebase emulators

## ✅ Quick Fix (2 Steps)

### Step 1: Start Firebase Emulators

Open PowerShell and run:

```powershell
cd "C:\Users\ashwi\OneDrive - School of Management Studies, Vels University(VISTAS), (Estd. u s 3 of the UGC Act 1956)\Desktop\Project - Shiv\backend"

# Add Java to PATH (if needed)
$env:PATH = "C:\Program Files\Android\Android Studio\jbr\bin;$env:PATH"

# Start emulators
firebase emulators:start
```

**Wait until you see:**
```
✔  auth: Emulator started at http://localhost:9099
✔  firestore: Emulator started at http://localhost:8080
✔  ui: Emulator UI started at http://localhost:4000
```

### Step 2: Rebuild App

The app code is already updated to use emulators. Just rebuild:

1. **In Android Studio:**
   - **Build → Clean Project**
   - **Build → Rebuild Project**
   - **Run** the app again

2. **Try signup again** - Should work now!

## ✅ Verify It's Working

### Check Logcat:
Look for:
```
D/FirebaseConfig: ✅ Connected to Firebase emulators (10.0.2.2:9099, 10.0.2.2:8080)
D/LoginActivity: Attempting signup for: test@example.com
D/LoginActivity: Signup successful!
```

### Check Emulator UI:
Open http://localhost:4000 in browser
- Go to **Authentication** tab
- You should see your new user!

## 🐛 If Still Not Working

### Check Emulators Are Running:
```powershell
netstat -an | findstr "9099 8080 4000"
```

Should show ports are listening.

### Check App Logs:
In Android Studio Logcat, filter by `FirebaseConfig` and look for:
- ✅ "Connected to Firebase emulators" = Working!
- ❌ "Failed to connect" = Check emulators are running

### For Physical Device (Not Emulator):

If testing on a real phone, you need to use your computer's IP instead of `10.0.2.2`:

1. **Find your computer's IP:**
   ```powershell
   ipconfig
   ```
   Look for "IPv4 Address" (e.g., `192.168.1.100`)

2. **Update FirebaseConfig.kt:**
   Change `10.0.2.2` to your IP address:
   ```kotlin
   auth.useEmulator("192.168.1.100", 9099)  // Your IP
   firestore.useEmulator("192.168.1.100", 8080)
   ```

3. **Make sure phone and computer are on same WiFi**

## 📝 Summary

- ✅ Emulator code is already enabled in the app
- ✅ Just need to start Firebase emulators
- ✅ Rebuild and run the app
- ✅ Signup/login will work locally!

**The network errors will disappear once emulators are running!** 🎉
