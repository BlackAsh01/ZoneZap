# Testing ZoneZap on Android Device

## ✅ Current Status

- ✅ Android device connected
- ✅ Android project structure: Complete
- ✅ Firebase config: Ready
- ✅ Metro bundler: Running
- ⚠️ ADB: Not in PATH (needs setup)

## 🎯 Two Ways to Run

### Method 1: Android Studio (EASIEST - Recommended!)

1. **Open Android Studio**
2. **File → Open** → Select `mobile-app/android` folder
3. Wait for Gradle sync
4. **Click Run button** (green ▶️)
5. **Select your connected device**
6. **Done!** App will build and install

**Advantages:**
- No PATH setup needed
- Visual feedback
- Easy debugging
- Handles all build issues

### Method 2: Command Line (After ADB Setup)

1. **Add Android SDK to PATH** (see instructions below)
2. **Verify device:** `adb devices`
3. **Run:** `cd mobile-app && npx react-native run-android`

## 📋 Setup ADB for Command Line

### Step 1: Find Android SDK Location

**In Android Studio:**
- File → Settings → Appearance & Behavior → System Settings → Android SDK
- Copy the "Android SDK Location" path

**Common locations:**
- `C:\Users\ashwi\AppData\Local\Android\Sdk`
- `%LOCALAPPDATA%\Android\Sdk`

### Step 2: Add to System PATH

1. **Windows Search:** "Environment Variables"
2. **System Properties → Environment Variables**
3. **System Variables → Path → Edit**
4. **New → Add:**
   ```
   C:\Users\ashwi\AppData\Local\Android\Sdk\platform-tools
   ```
5. **OK → OK → OK**
6. **Restart terminal/command prompt**

### Step 3: Verify

```bash
adb devices
```

Should show:
```
List of devices attached
[DEVICE_ID]    device
```

## 🚀 Running the App

### Using Android Studio:

1. Open `mobile-app/android` in Android Studio
2. Click Run (▶️)
3. Select your device
4. Wait for build and install

### Using Command Line:

```bash
# Terminal 1: Metro (if not running)
cd mobile-app
npm start

# Terminal 2: Run app
cd mobile-app
npx react-native run-android
```

## 📱 What to Test

Once app is running:

1. **Login Screen**
   - [ ] Screen loads
   - [ ] Email/password fields work
   - [ ] Can create account
   - [ ] Can login

2. **Home Screen**
   - [ ] Shows after login
   - [ ] Location permission requested
   - [ ] Current location displayed
   - [ ] Reminders shown

3. **Panic Button**
   - [ ] Navigate to Panic screen
   - [ ] Click PANIC button
   - [ ] Alert created in Firebase

4. **Reminders**
   - [ ] Can add reminder
   - [ ] Reminders list shows
   - [ ] Can complete/delete

5. **Firebase Connection**
   - [ ] Check Firebase Console → Authentication
   - [ ] Check Firestore → Data
   - [ ] Verify data is saving

## 🔍 Monitoring

**Firebase Console:** https://console.firebase.google.com/
- Check Authentication → Users
- Check Firestore → Data

**Metro Bundler:** http://localhost:8081
- View bundle status
- See any errors

**Device Logs:**
```bash
adb logcat | grep -i react
```

## ✅ Success Indicators

You'll know it's working when:
- ✅ App installs on device
- ✅ App launches without crashes
- ✅ Login screen appears
- ✅ Can create account
- ✅ Data appears in Firebase Console
- ✅ All features work

---

**Ready to test! Use Android Studio for the easiest experience!** 🎉
