# ZoneZap Setup Test Guide

Quick verification steps to test your Firebase setup.

## ✅ Pre-Test Checklist

- [x] Firebase config updated in `mobile-app/src/config/firebase.js`
- [x] `google-services.json` placed in `mobile-app/android/app/`
- [x] Firebase CLI installed (v15.4.0)
- [x] Node.js installed (v20.16.0)
- [x] Dependencies installed for mobile-app and backend

## 🧪 Test Steps

### Test 1: Verify Firebase Configuration

Your Firebase config looks correct:
- ✅ Project ID: `zonezap-a6953`
- ✅ API Key: Configured
- ✅ Auth Domain: Configured
- ✅ Storage Bucket: Configured

### Test 2: Check Firebase Login Status

```bash
cd backend
firebase login:list
```

### Test 3: Test Firebase Emulators (Local Testing)

```bash
cd backend
firebase emulators:start
```

This will start:
- Firestore emulator on port 8080
- UI on port 4000

### Test 4: Verify Mobile App Can Connect

1. **Start Metro Bundler:**
   ```bash
   cd mobile-app
   npm start
   ```

2. **Run Android App:**
   ```bash
   # In another terminal
   cd mobile-app
   npx react-native run-android
   ```

3. **Test Authentication:**
   - Open the app
   - Try to create an account
   - Check if it connects to Firebase

### Test 5: Verify Firestore Connection

After running the app:
1. Try creating a user account
2. Check Firebase Console → Authentication → Users
3. Check Firebase Console → Firestore → Data
4. You should see data being created

## 🔍 Quick Verification Commands

```bash
# Check Firebase project
cd backend
firebase projects:list

# Check if logged in
firebase login:list

# Test Firestore rules syntax
firebase deploy --only firestore:rules --dry-run
```

## 📱 Testing the Mobile App

### Expected Behavior:

1. **Login Screen:**
   - Should load without errors
   - Should allow email/password input

2. **After Login:**
   - Should navigate to Home screen
   - Should request location permission
   - Should show current location

3. **Panic Button:**
   - Should create alert in Firestore
   - Should be visible in Firebase Console

4. **Reminders:**
   - Should save to Firestore
   - Should be retrievable

## 🐛 Common Test Issues

### Issue: "Firebase not initialized"
**Solution:** Check `firebase.js` has correct config values

### Issue: "Permission denied"
**Solution:** 
- Check Firestore rules are published
- Verify authentication is enabled in Firebase Console

### Issue: "Network request failed"
**Solution:**
- Check internet connection
- Verify Firebase project is active
- Check if emulators are running (if using local)

### Issue: "App crashes on startup"
**Solution:**
- Check Metro bundler is running
- Verify all dependencies are installed
- Check Android emulator/device is connected

## ✅ Success Indicators

You'll know it's working when:
- ✅ App starts without crashes
- ✅ Login screen appears
- ✅ Can create account successfully
- ✅ User appears in Firebase Console → Authentication
- ✅ Location tracking works (if permission granted)
- ✅ Data saves to Firestore

## 🚀 Next Steps After Testing

Once everything works:
1. Test all features (panic button, reminders, location)
2. Deploy Firestore rules to production
3. Test on physical device
4. Prepare for demo/presentation

---

**Ready to test! Run the commands above to verify your setup.**
