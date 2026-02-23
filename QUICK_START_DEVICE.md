# Quick Start: Run on Your Android Device

Your Android device is connected! Here's how to run ZoneZap on it.

## 🎯 Fastest Method: Use Android Studio

### Step 1: Open Project in Android Studio

1. **Open Android Studio**
2. **File → Open**
3. Navigate to: `Project - Shiv\mobile-app\android`
4. Click "OK"
5. Wait for Gradle sync to complete

### Step 2: Run the App

1. **Ensure your device is connected** (USB debugging enabled)
2. **Click the green "Run" button** (▶️) in Android Studio
3. **Select your device** from the device dropdown
4. **Click OK**

Android Studio will:
- Build the APK
- Install on your device
- Launch the app

## 🔧 Alternative: Set Up ADB in PATH

If you prefer command line:

### Find Android SDK

1. Open Android Studio
2. **File → Settings** (or Preferences on Mac)
3. **Appearance & Behavior → System Settings → Android SDK**
4. Note the "Android SDK Location" (usually `C:\Users\ashwi\AppData\Local\Android\Sdk`)

### Add to PATH

**Windows:**
1. Search "Environment Variables" in Windows
2. Click "Environment Variables"
3. Under "System Variables", find "Path" → Edit
4. Add:
   - `C:\Users\ashwi\AppData\Local\Android\Sdk\platform-tools`
   - `C:\Users\ashwi\AppData\Local\Android\Sdk\tools`
5. Click OK on all dialogs
6. **Restart your terminal/command prompt**

### Verify ADB

```bash
adb devices
```

Should show your device:
```
List of devices attached
XXXXXXXX    device
```

### Run the App

```bash
cd mobile-app
npx react-native run-android
```

## 📱 Enable USB Debugging (If Needed)

On your Android device:

1. **Settings → About Phone**
2. Tap **"Build Number"** 7 times (enables Developer Options)
3. Go back → **Developer Options**
4. Enable **"USB Debugging"**
5. Connect device via USB
6. When prompted, click **"Allow"** or **"Always allow from this computer"**

## ✅ What Happens When You Run

1. **Metro Bundler starts** (if not already running)
2. **Gradle builds** the Android APK
3. **APK installs** on your device
4. **App launches** automatically
5. **Login screen** appears

## 🧪 Testing Checklist

Once the app is running:

- [ ] App launches without crashes
- [ ] Login screen appears
- [ ] Can create a new account
- [ ] Can login
- [ ] Home screen loads
- [ ] Location permission requested
- [ ] Panic button works
- [ ] Reminders can be created

## 🐛 Common Issues

### "Device not found"
- Check USB cable connection
- Enable USB debugging on device
- Try different USB port
- Check device shows "USB debugging connected" notification

### "Build failed"
- Open Android Studio first (ensures SDK is set up)
- Check JDK is installed (Java 11+)
- Try: `cd android && gradlew clean`

### "Metro bundler error"
- Start Metro manually: `cd mobile-app && npm start`
- Keep it running in a separate terminal

## 🚀 Quick Commands

```bash
# Check device (after ADB setup)
adb devices

# Run app
cd mobile-app
npx react-native run-android

# Or use Android Studio (easier!)
# Just click Run button
```

---

**Recommended: Use Android Studio - it's the easiest way!**

Just open `mobile-app/android` in Android Studio and click Run! 🎉
