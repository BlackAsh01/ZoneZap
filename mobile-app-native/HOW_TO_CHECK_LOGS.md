# How to Check Logs and Verify Authentication

This guide shows you how to verify that signup/login is actually working.

## 🔍 Method 1: Android Logcat (Recommended)

### In Android Studio:

1. **Open Logcat:**
   - Bottom panel → **Logcat** tab
   - Or: **View → Tool Windows → Logcat**

2. **Filter logs:**
   - In the search box, type: `LoginActivity` or `FirebaseConfig`
   - Or use filter: `tag:LoginActivity`

3. **What to look for:**
   ```
   D/LoginActivity: Attempting signup for: test@example.com
   D/LoginActivity: Signup successful! User ID: abc123...
   D/LoginActivity: User email: test@example.com
   ```

### Using ADB (Command Line):

```powershell
# View all logs
adb logcat

# Filter for authentication logs
adb logcat | findstr "LoginActivity"

# Filter for Firebase logs
adb logcat | findstr "Firebase"

# Clear logs and watch new ones
adb logcat -c && adb logcat | findstr "LoginActivity"
```

## 🔍 Method 2: Firebase Console (Production)

If using production Firebase (not emulators):

1. **Go to Firebase Console:**
   - https://console.firebase.google.com/
   - Select your project: `zonezap-a6953`

2. **Check Authentication:**
   - Left sidebar → **Authentication**
   - Click **Users** tab
   - You'll see all created accounts here!

3. **Check Firestore:**
   - Left sidebar → **Firestore Database**
   - View collections: `users`, `alerts`, `reminders`, `movement_logs`

## 🔍 Method 3: Firebase Emulator UI (Local)

If using Firebase emulators:

1. **Start emulators:**
   ```powershell
   cd backend
   firebase emulators:start
   ```

2. **Open Emulator UI:**
   - Browser: http://localhost:4000

3. **Check Authentication:**
   - Click **Authentication** in left sidebar
   - See all users created in emulator

4. **Check Firestore:**
   - Click **Firestore** in left sidebar
   - View collections and documents

## 📱 Method 4: App Toast Messages

The app shows toast messages:
- ✅ "Account created successfully!" - Signup worked
- ✅ "Login successful!" - Login worked
- ❌ "Sign up failed: [error]" - Shows the error
- ❌ "Login failed: [error]" - Shows the error

## 🧪 Testing Steps

### Test Signup:

1. **Open app** → Login screen appears
2. **Enter email:** `test@example.com`
3. **Enter password:** `password123` (6+ characters)
4. **Tap "Sign Up" button**
5. **Watch Logcat** for:
   ```
   D/LoginActivity: Attempting signup for: test@example.com
   D/LoginActivity: Signup successful! User ID: [some-id]
   ```
6. **Check Firebase Console** → Authentication → Users
7. **Should see:** New user with email `test@example.com`

### Test Login:

1. **Enter same email/password**
2. **Tap "Login" button**
3. **Watch Logcat** for:
   ```
   D/LoginActivity: Attempting login for: test@example.com
   D/LoginActivity: Login successful! User ID: [same-id]
   ```
4. **Should navigate to:** Home screen

## 🐛 Common Issues & What Logs Show

### "Email already exists"
```
E/LoginActivity: Signup failed: The email address is already in use
```
**Solution:** Use Login instead, or use different email

### "Password too short"
```
E/LoginActivity: Signup failed: Password should be at least 6 characters
```
**Solution:** Use password with 6+ characters

### "Network error"
```
E/LoginActivity: Signup failed: A network error has occurred
```
**Solution:** 
- Check internet connection
- Check Firebase emulators are running (if using emulators)
- Check Firebase Console project settings

### "Invalid email format"
```
E/LoginActivity: Signup failed: The email address is badly formatted
```
**Solution:** Use valid email format (e.g., `user@example.com`)

## 📊 What Success Looks Like

### In Logcat:
```
D/FirebaseConfig: Firebase initialized
D/FirebaseConfig: Auth instance: [DEFAULT]
D/LoginActivity: Attempting signup for: test@example.com
D/LoginActivity: Signup successful! User ID: abc123xyz...
D/LoginActivity: User email: test@example.com
```

### In Firebase Console:
- **Authentication → Users:** Shows new user
- **Firestore:** Can see user data (if app creates user document)

### In App:
- Toast: "Account created successfully!"
- Navigates to Home screen
- Can use all app features

## 🎯 Quick Verification Checklist

- [ ] Logcat shows "Attempting signup/login"
- [ ] Logcat shows "successful" message
- [ ] Toast message appears in app
- [ ] App navigates to Home screen
- [ ] Firebase Console shows user (if using production)
- [ ] Emulator UI shows user (if using emulators)

## 💡 Pro Tips

1. **Use Logcat filter:** `tag:LoginActivity` to see only auth logs
2. **Clear logs:** Click trash icon in Logcat before testing
3. **Watch in real-time:** Logcat updates as you use the app
4. **Check both:** Logcat (app logs) + Firebase Console (backend data)

## 🔧 Enable Emulator Mode (Optional)

To use local Firebase emulators instead of production:

1. **Edit `FirebaseConfig.kt`:**
   - Uncomment the emulator connection code
   - Change IP to `10.0.2.2` for Android emulator
   - Or use your computer's IP for physical device

2. **Start emulators:**
   ```powershell
   cd backend
   firebase emulators:start
   ```

3. **Restart app** - Now connects to emulators

---

**Now you can verify everything is working!** 🎉
