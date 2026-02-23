# Running ZoneZap Locally

Complete guide to run ZoneZap with Firebase emulators for local testing.

## 🚀 Quick Start

### Step 1: Start Firebase Emulators (Already Running!)

The Firebase emulators are running in the background. They provide:
- **Firestore** on port `8080`
- **UI Dashboard** on port `4000`
- **Functions** on port `5001` (if enabled)

**Access the Emulator UI:** http://localhost:4000

### Step 2: Start Metro Bundler

Open a **new terminal** and run:

```bash
cd "c:\Users\ashwi\OneDrive - School of Management Studies, Vels University(VISTAS), (Estd. u s 3 of the UGC Act 1956)\Desktop\Project - Shiv\mobile-app"
npm start
```

This starts the React Native Metro bundler. Keep this terminal open.

### Step 3: Run Android App

Open **another terminal** and run:

```bash
cd "c:\Users\ashwi\OneDrive - School of Management Studies, Vels University(VISTAS), (Estd. u s 3 of the UGC Act 1956)\Desktop\Project - Shiv\mobile-app"
npx react-native run-android
```

**Note:** Make sure you have:
- Android Studio installed
- Android emulator running OR physical device connected
- USB debugging enabled (for physical device)

## 📱 What to Expect

1. **Metro Bundler** will compile your JavaScript code
2. **Android app** will install and launch on your device/emulator
3. **Login screen** should appear
4. You can create accounts and test features locally

## 🔧 Connecting to Local Emulators

By default, React Native Firebase connects to the **production Firebase project**. To connect to local emulators, you may need to update the connection settings.

### Option 1: Use Production Firebase (Current Setup)

Your app is currently configured to use the production Firebase project (`zonezap-a6953`). This works fine for testing authentication and Firestore.

### Option 2: Connect to Local Emulators (Advanced)

If you want to use local emulators, you'll need to configure the connection in your code. However, for most testing, using production Firebase is fine.

## ✅ Testing Checklist

Once the app is running:

- [ ] App launches without crashes
- [ ] Login screen appears
- [ ] Can create a new account
- [ ] Can login with existing account
- [ ] Home screen loads
- [ ] Location permission requested
- [ ] Panic button works
- [ ] Reminders can be created

## 🐛 Troubleshooting

### Metro Bundler Issues

```bash
# Clear cache and restart
cd mobile-app
npm start -- --reset-cache
```

### Android Build Issues

```bash
# Clean build
cd mobile-app/android
./gradlew clean
cd ..
npx react-native run-android
```

### Port Already in Use

If port 8081 (Metro) is busy:
```bash
# Kill process on port 8081
netstat -ano | findstr :8081
taskkill /PID <PID> /F
```

### Firebase Connection Issues

- Check internet connection
- Verify Firebase project is active
- Check `firebase.js` has correct config

## 📊 Monitoring

**Firebase Emulator UI:** http://localhost:4000
- View Firestore data
- Monitor authentication
- Check function logs

**Metro Bundler:** http://localhost:8081
- View bundle status
- Check for errors

## 🎯 Next Steps

1. **Test Authentication:**
   - Create a test account
   - Login/logout
   - Check Firebase Console → Authentication

2. **Test Features:**
   - Location tracking
   - Panic button
   - Reminders

3. **Check Data:**
   - Firebase Emulator UI → Firestore
   - Verify data is being saved

---

**You're all set! The emulators are running. Now start Metro and run the Android app!**
