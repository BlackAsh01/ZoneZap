# How to Run the ZoneZap App

## 🚀 Quick Run (3 Steps)

### 1. Open Project
- **Android Studio** → **File** → **Open** → Select `mobile-app-native`
- Wait for Gradle sync (~1-2 minutes)

### 2. Setup Device
- **Option A (Emulator)**: **Tools** → **Device Manager** → **Create Device** → Start emulator
- **Option B (Physical)**: Connect USB → Enable USB Debugging → Allow on device

### 3. Run App
- Select device from dropdown (top toolbar)
- Click **Run** button (▶️ green play icon) or press **Shift+F10**
- App builds and launches automatically

## ✅ What You'll See

1. **Login Screen** - Email/password authentication
2. **Home Screen** - After login, shows location and navigation
3. **Panic Button** - Emergency alerts
4. **Reminders** - Manage reminders

## 🐛 Common Issues

### Gradle Sync Fails
```
File → Invalidate Caches / Restart → Invalidate and Restart
File → Sync Project with Gradle Files
```

### Build Fails
```
Build → Clean Project
Build → Rebuild Project
```

### App Crashes
- Check **Logcat** (bottom panel) for errors
- Verify `google-services.json` exists in `app/` directory
- Check Firebase configuration

## 📱 Testing Checklist

- [ ] App opens without crashes
- [ ] Can create account and login
- [ ] Location permission requested
- [ ] Home screen shows location
- [ ] Panic button sends alert
- [ ] Can add reminders
- [ ] Reminders appear in list

## 🎯 Expected Behavior

- ✅ Smooth app launch
- ✅ Firebase authentication works
- ✅ Location tracking works (after permission)
- ✅ Emergency alerts save to Firestore
- ✅ Reminders persist correctly

---

**That's it! The app should run smoothly in Android Studio.** 🎉
