# Running ZoneZap on Android Device

Guide to run the app on your connected Android device.

## 🔌 Device Connected!

Your Android device is connected. To run the app, we need to set up ADB (Android Debug Bridge).

## ⚠️ Current Issue

ADB is not in your system PATH. This is needed to communicate with your Android device.

## 🔧 Quick Fix Options

### Option 1: Add Android SDK to PATH (Recommended)

1. **Find Android SDK Location:**
   - Usually at: `C:\Users\ashwi\AppData\Local\Android\Sdk`
   - Or check Android Studio → SDK Manager → Android SDK Location

2. **Add to System PATH:**
   - Open System Properties → Environment Variables
   - Add to PATH:
     - `%LOCALAPPDATA%\Android\Sdk\platform-tools`
     - `%LOCALAPPDATA%\Android\Sdk\tools`
   - Or set `ANDROID_HOME` = `C:\Users\ashwi\AppData\Local\Android\Sdk`
   - Then add `%ANDROID_HOME%\platform-tools` to PATH

3. **Restart Terminal** after adding to PATH

4. **Verify:**
   ```bash
   adb devices
   ```
   Should show your device

### Option 2: Use Android Studio (Easier)

1. **Open Android Studio**
2. **Open Project:** `mobile-app/android`
3. **Wait for Gradle sync**
4. **Click Run** (green play button)
5. **Select your connected device**

### Option 3: Run ADB Directly

If you know Android SDK location:

```bash
# Replace with your actual SDK path
C:\Users\ashwi\AppData\Local\Android\Sdk\platform-tools\adb.exe devices
```

## ✅ Once ADB is Working

### Step 1: Verify Device Connection

```bash
adb devices
```

Should show:
```
List of devices attached
XXXXXXXX    device
```

### Step 2: Enable USB Debugging (if needed)

On your Android device:
1. Settings → About Phone
2. Tap "Build Number" 7 times
3. Go back → Developer Options
4. Enable "USB Debugging"

### Step 3: Run the App

```bash
cd mobile-app
npx react-native run-android
```

## 🚀 Quick Test (If ADB Works)

```bash
# Check device
adb devices

# Install and run app
cd mobile-app
npx react-native run-android
```

The app will:
1. Build the Android APK
2. Install on your device
3. Launch automatically
4. Connect to Metro bundler

## 📱 What to Expect

1. **Build Process:**
   - Gradle will download dependencies (first time takes a while)
   - APK will be built
   - App will install on device

2. **App Launch:**
   - ZoneZap app opens
   - Login screen appears
   - You can test all features!

## 🐛 Troubleshooting

### "Device unauthorized"
- Check device screen for USB debugging authorization prompt
- Click "Allow" or "Always allow"

### "Build failed"
- Check Android Studio is installed
- Verify JDK 11+ is installed
- Check `android/app/build.gradle` is correct

### "Metro bundler not found"
- Start Metro: `cd mobile-app && npm start`
- Keep it running in a separate terminal

## ✅ Success Checklist

- [ ] ADB detects device (`adb devices` shows device)
- [ ] USB debugging enabled on device
- [ ] Metro bundler running
- [ ] Firebase emulators running (optional)
- [ ] App builds successfully
- [ ] App installs on device
- [ ] App launches and shows login screen

---

**Once ADB is set up, you're ready to test the app on your device!**
