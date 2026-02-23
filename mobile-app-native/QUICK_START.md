# Quick Start Guide - Native Android App

## 🎯 Goal

Get the ZoneZap app running in Android Studio **without any Gradle issues**.

## ⚡ 3-Step Setup

### Step 1: Open Project
```
1. Open Android Studio
2. File > Open
3. Select: mobile-app-native
4. Wait for Gradle sync (~1-2 minutes)
```

### Step 2: Verify Firebase
```
Check: mobile-app-native/app/google-services.json exists
If missing: Copy from mobile-app-new/android/app/google-services.json
```

### Step 3: Run
```
1. Connect device or start emulator
2. Click Run button (green play icon)
3. App installs and launches automatically
```

## ✅ Success Criteria

- ✅ Gradle sync completes without errors
- ✅ Build succeeds
- ✅ App runs on device/emulator
- ✅ Login screen appears

## 🆘 If Something Goes Wrong

1. **Gradle sync errors**: File > Invalidate Caches / Restart
2. **Build errors**: Build > Clean Project, then Build > Rebuild Project
3. **Firebase errors**: Verify google-services.json is in app/ directory

## 📱 What You'll See

1. **Login Screen** - Email/password authentication
2. **Home Screen** - Location tracking and reminders
3. **Panic Button** - Emergency alerts
4. **Reminders** - Manage your reminders

## 🎉 That's It!

This native Android app uses **standard Gradle configuration** - no React Native complexity, no custom init scripts, no Kotlin metadata issues.

**Just open and run!** 🚀
