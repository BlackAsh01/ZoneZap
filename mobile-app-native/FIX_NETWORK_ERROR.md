# Fix: Network Error - Firebase Connection Issues

## 🔍 Problem

You're seeing errors like:
```
A network error (such as timeout, interrupted connection or unreachable host) has occurred.
RecaptchaCallWrapper: Initial task failed
```

This means the app can't connect to Firebase servers.

## 🚀 Solution Options

### Option 1: Use Firebase Emulators (Recommended for Local Testing)

If you have Firebase emulators running, configure the app to use them:

1. **Edit `app/src/main/java/com/zonezapapp/config/FirebaseConfig.kt`**

2. **Uncomment the emulator code** (around line 20-28):

```kotlin
init {
    Log.d(TAG, "Firebase initialized")
    
    // Connect to Firebase emulators
    try {
        // For Android Emulator, use 10.0.2.2 to access host machine's localhost
        auth.useEmulator("10.0.2.2", 9099)  // Auth emulator port
        firestore.useEmulator("10.0.2.2", 8080)  // Firestore emulator port
        Log.d(TAG, "Connected to Firebase emulators")
    } catch (e: Exception) {
        Log.e(TAG, "Failed to connect to emulators: ${e.message}")
    }
}
```

3. **Start Firebase emulators:**
```powershell
cd backend
firebase emulators:start
```

4. **Restart the app** - It will now use local emulators

### Option 2: Use Production Firebase (Requires Internet)

If you want to use production Firebase:

1. **Check internet connection** on device/emulator
2. **Verify Firebase project settings:**
   - Go to: https://console.firebase.google.com/
   - Select project: `zonezap-a6953`
   - Check Authentication is enabled
   - Check Firestore is enabled

3. **Check Firebase project status:**
   - Make sure billing is not required (or enabled)
   - Verify API keys are valid

### Option 3: Disable reCAPTCHA (For Testing Only)

reCAPTCHA requires internet. For local testing with emulators, you can disable it:

1. **In Firebase Console:**
   - Authentication → Settings → reCAPTCHA
   - Disable reCAPTCHA for testing (not recommended for production)

## 🔧 Quick Fix Steps

### Step 1: Check Emulators Are Running

```powershell
# Check if emulators are running
netstat -an | findstr "9099 8080 4000"
```

If not running, start them:
```powershell
cd backend
firebase emulators:start
```

### Step 2: Enable Emulator Mode in App

Edit `FirebaseConfig.kt` and uncomment emulator connection code (see Option 1 above).

### Step 3: Rebuild App

In Android Studio:
- **Build → Clean Project**
- **Build → Rebuild Project**
- **Run** the app again

## 📱 Testing After Fix

1. **Try signup again**
2. **Check Logcat** for:
   ```
   D/FirebaseConfig: Connected to Firebase emulators
   D/LoginActivity: Attempting signup for: test@example.com
   D/LoginActivity: Signup successful!
   ```
3. **Check Emulator UI:** http://localhost:4000
   - Should see new user in Authentication tab

## 🐛 Troubleshooting

### "Still getting network errors"

**Check:**
- Emulators are actually running (check terminal)
- App is using emulator IP (10.0.2.2 for Android emulator)
- For physical device, use your computer's IP address instead of 10.0.2.2

### "Can't find emulator IP"

**For Android Emulator:**
- Use: `10.0.2.2` (this is Android emulator's way to access host machine)

**For Physical Device:**
- Find your computer's IP: `ipconfig` (Windows) or `ifconfig` (Mac/Linux)
- Use that IP instead of `10.0.2.2`
- Make sure device and computer are on same WiFi network

### "Emulators not starting"

See `backend/START_EMULATORS.md` for emulator setup instructions.

## ✅ Success Indicators

After fixing, you should see:
- ✅ No network errors in Logcat
- ✅ "Connected to Firebase emulators" log message
- ✅ Signup/login works without errors
- ✅ Users appear in Emulator UI (http://localhost:4000)

## 🎯 Recommended Setup

For local development:
1. ✅ Use Firebase emulators
2. ✅ Enable emulator connection in app
3. ✅ Test without internet connection
4. ✅ All data stays local

For production:
1. ✅ Use production Firebase
2. ✅ Ensure internet connection
3. ✅ Verify Firebase project settings
4. ✅ Keep emulator code commented out
