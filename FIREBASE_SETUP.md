# Firebase Setup Guide for ZoneZap

Complete step-by-step guide to configure Firebase for your ZoneZap project.

## 🎯 Step 1: Create Firebase Project

1. **Go to Firebase Console:**
   - Visit: https://console.firebase.google.com/
   - Sign in with your Google account

2. **Create New Project:**
   - Click "Add project" or "Create a project"
   - Project name: `ZoneZap` (or your preferred name)
   - Click "Continue"
   - **Disable Google Analytics** (optional, for simplicity) or enable if you want analytics
   - Click "Create project"
   - Wait for project creation (30-60 seconds)
   - Click "Continue"

---

## 🔐 Step 2: Enable Firebase Authentication

1. **Navigate to Authentication:**
   - In Firebase Console, click "Authentication" in left sidebar
   - Click "Get started"

2. **Enable Email/Password Sign-in:**
   - Click "Sign-in method" tab
   - Click "Email/Password"
   - Toggle "Enable" to ON
   - Click "Save"

✅ **Authentication is now enabled!**

---

## 💾 Step 3: Set Up Cloud Firestore

1. **Navigate to Firestore:**
   - Click "Firestore Database" in left sidebar
   - Click "Create database"

2. **Choose Security Rules:**
   - Select "Start in test mode" (we'll update rules later)
   - Click "Next"

3. **Choose Location:**
   - Select a location close to you (e.g., `us-central`, `asia-south1`)
   - Click "Enable"
   - Wait for database creation (30-60 seconds)

✅ **Firestore is now set up!**

---

## 📱 Step 4: Register Your Mobile App

### For Android:

1. **Add Android App:**
   - Click the gear icon ⚙️ → "Project settings"
   - Scroll to "Your apps" section
   - Click "Add app" → Select Android icon

2. **Register App:**
   - **Android package name:** `com.zonezapapp` (or your preferred package name)
   - **App nickname (optional):** ZoneZap Android
   - **Debug signing certificate SHA-1 (optional):** Leave blank for now
   - Click "Register app"

3. **Download Configuration File:**
   - Download `google-services.json`
   - **IMPORTANT:** Save this file - you'll need it!

4. **Place the File:**
   - Copy `google-services.json` to: `mobile-app/android/app/google-services.json`
   - (You may need to create the `android/app/` directories if they don't exist)

### For iOS (if developing for iOS):

1. **Add iOS App:**
   - Click "Add app" → Select iOS icon

2. **Register App:**
   - **iOS bundle ID:** `com.zonezapapp` (or your preferred bundle ID)
   - **App nickname:** ZoneZap iOS
   - Click "Register app"

3. **Download Configuration File:**
   - Download `GoogleService-Info.plist`
   - Place in: `mobile-app/ios/GoogleService-Info.plist`

---

## 🔑 Step 5: Get Web App Configuration (for React Native)

1. **Add Web App:**
   - In Project Settings → "Your apps"
   - Click "Add app" → Select Web icon (</>)

2. **Register Web App:**
   - **App nickname:** ZoneZap Web Config
   - Click "Register app"

3. **Copy Firebase Config:**
   - You'll see a config object like this:
   ```javascript
   const firebaseConfig = {
     apiKey: "AIzaSy...",
     authDomain: "zonezap-xxxxx.firebaseapp.com",
     projectId: "zonezap-xxxxx",
     storageBucket: "zonezap-xxxxx.appspot.com",
     messagingSenderId: "123456789",
     appId: "1:123456789:web:abcdef123456"
   };
   ```
   - **Copy this entire config object**

4. **Update Your Code:**
   - Open: `mobile-app/src/config/firebase.js`
   - Replace the placeholder values with your actual config

---

## 🔔 Step 6: Enable Firebase Cloud Messaging (FCM)

1. **Navigate to Cloud Messaging:**
   - Click "Cloud Messaging" in left sidebar
   - It's automatically enabled when you register your app

2. **Get Server Key (Optional, for advanced use):**
   - Project Settings → Cloud Messaging tab
   - Note the Server key (you may need this later)

✅ **FCM is now enabled!**

---

## ✅ Step 7: Update Your Code

### Update `mobile-app/src/config/firebase.js`:

Replace the placeholder config with your actual Firebase config:

```javascript
import { initializeApp } from '@react-native-firebase/app';
import auth from '@react-native-firebase/auth';
import firestore from '@react-native-firebase/firestore';
import messaging from '@react-native-firebase/messaging';

// Firebase configuration
// Replace with YOUR actual Firebase project credentials
const firebaseConfig = {
  apiKey: "YOUR_ACTUAL_API_KEY",
  authDomain: "YOUR_PROJECT_ID.firebaseapp.com",
  projectId: "YOUR_PROJECT_ID",
  storageBucket: "YOUR_PROJECT_ID.appspot.com",
  messagingSenderId: "YOUR_MESSAGING_SENDER_ID",
  appId: "YOUR_APP_ID"
};

// Initialize Firebase (if not already initialized)
if (!initializeApp.apps.length) {
  initializeApp.initializeApp(firebaseConfig);
}

export { auth, firestore, messaging };
export default initializeApp;
```

---

## 🔒 Step 8: Update Firestore Security Rules

1. **Navigate to Firestore Rules:**
   - Click "Firestore Database" → "Rules" tab

2. **Update Rules:**
   - The rules in `backend/firestore.rules` are already configured
   - Copy the content from `backend/firestore.rules`
   - Paste into Firebase Console Rules editor
   - Click "Publish"

---

## 🚀 Step 9: Initialize Firebase CLI

1. **Login to Firebase:**
   ```bash
   firebase login
   ```
   - This will open a browser window
   - Sign in with your Google account
   - Grant permissions

2. **Initialize Firebase Project:**
   ```bash
   cd backend
   firebase init
   ```

3. **Select Options:**
   - ✅ Firestore: Configure security rules and indexes
   - Use an existing project: Select your ZoneZap project
   - For Firestore rules: Use `firestore.rules`
   - For Firestore indexes: Use `firestore.indexes.json`

---

## ✅ Verification Checklist

After completing all steps, verify:

- [ ] Firebase project created
- [ ] Authentication enabled (Email/Password)
- [ ] Firestore database created
- [ ] Android app registered (`google-services.json` downloaded)
- [ ] Web app registered (config copied)
- [ ] `mobile-app/src/config/firebase.js` updated with real config
- [ ] `google-services.json` placed in `mobile-app/android/app/`
- [ ] Firestore rules published
- [ ] Firebase CLI logged in (`firebase login`)
- [ ] Firebase project initialized (`firebase init`)

---

## 🧪 Test Your Setup

1. **Start Firebase Emulators:**
   ```bash
   cd backend
   firebase emulators:start
   ```

2. **Run Mobile App:**
   ```bash
   cd mobile-app
   npx react-native run-android
   ```

3. **Test Authentication:**
   - Try creating an account in the app
   - Check Firebase Console → Authentication → Users
   - You should see the new user!

---

## 📝 Quick Reference

**Firebase Console:** https://console.firebase.google.com/

**Your Project Settings:**
- Click gear icon ⚙️ → Project settings

**Important Files:**
- `mobile-app/src/config/firebase.js` - Web config
- `mobile-app/android/app/google-services.json` - Android config
- `backend/firestore.rules` - Security rules
- `backend/firestore.indexes.json` - Database indexes

---

## 🆘 Troubleshooting

### "Firebase not configured" error
- Check `firebase.js` has real values (not placeholders)
- Verify `google-services.json` is in correct location

### "Permission denied" error
- Check Firestore rules are published
- Verify authentication is enabled


---

**You're all set! 🎉**

Once Firebase is configured, you can run the app and start testing!
