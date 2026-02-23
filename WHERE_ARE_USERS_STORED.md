# Where Are Users Stored? - Manual Check Guide

Users are stored in **two places** in Firebase:

## 📍 Location 1: Firebase Authentication

**Purpose:** Stores email/password credentials for login

**How to Check:**

### Using Firebase Emulator UI (Local Development):

1. **Start Firebase Emulators** (if not running):
   ```powershell
   cd backend
   firebase emulators:start
   ```

2. **Open Firebase Emulator UI:**
   - Open browser: **http://localhost:4000**
   - Click on **"Authentication"** tab (left sidebar)
   - You'll see all registered users with:
     - Email address
     - User ID (UID)
     - Creation timestamp
     - Provider (email/password)

### Using Firebase Console (Production):

1. Go to: **https://console.firebase.google.com/**
2. Select your project
3. Click **"Authentication"** in left sidebar
4. Click **"Users"** tab
5. See all registered users

## 📍 Location 2: Firestore Database - `users` Collection

**Purpose:** Stores user profile data (type, name, guardians, wards)

**How to Check:**

### Using Firebase Emulator UI (Local Development):

1. **Open Firebase Emulator UI:**
   - Browser: **http://localhost:4000**
   - Click on **"Firestore"** tab (left sidebar)

2. **Navigate to Users Collection:**
   - In the left panel, expand collections
   - Click on **`users`** collection
   - You'll see documents with:
     - **Document ID:** Firebase Auth UID (same as Authentication User ID)
     - **Fields:**
       - `userId`: Firebase Auth UID
       - `email`: User's email
       - `name`: User's display name
       - `type`: "user" or "guardian"
       - `createdAt`: Timestamp
       - `updatedAt`: Timestamp
       - `guardians`: Array of guardian IDs (for users)
       - `wards`: Array of user IDs (for guardians)

### Using Firebase Console (Production):

1. Go to: **https://console.firebase.google.com/**
2. Select your project
3. Click **"Firestore Database"** in left sidebar
4. Click **"Data"** tab
5. Navigate to **`users`** collection
6. Click on a user document to see all fields

## 🔍 Quick Check: Does a User Exist?

### Method 1: Check Authentication (Email/Password)

**In Emulator UI (http://localhost:4000):**
1. Go to **Authentication** tab
2. Look for the email address
3. If found → User exists for login
4. If not found → User needs to sign up

**Example:**
```
Authentication Tab:
├── ashwin@gmail.com
│   ├── UID: aRoTWcUmryinWFnCxSSbXsk545W5
│   ├── Created: 2026-02-04 00:27:39
│   └── Provider: password
```

### Method 2: Check Firestore (User Profile)

**In Emulator UI (http://localhost:4000):**
1. Go to **Firestore** tab
2. Click **`users`** collection
3. Look for document with matching email
4. Check the `type` field to see if user or guardian

**Example:**
```
Firestore → users Collection:
├── Document ID: aRoTWcUmryinWFnCxSSbXsk545W5
│   ├── email: "ashwin@gmail.com"
│   ├── name: "ashwin"
│   ├── type: "user"
│   ├── guardians: []
│   ├── createdAt: Timestamp
│   └── updatedAt: Timestamp
```

## 📊 Complete User Data Structure

### Authentication (Firebase Auth):
```json
{
  "uid": "aRoTWcUmryinWFnCxSSbXsk545W5",
  "email": "ashwin@gmail.com",
  "emailVerified": false,
  "creationTime": "2026-02-04T00:27:39.000Z",
  "lastSignInTime": "2026-02-04T00:54:07.000Z"
}
```

### Firestore (users Collection):
```json
{
  "userId": "aRoTWcUmryinWFnCxSSbXsk545W5",
  "email": "ashwin@gmail.com",
  "name": "ashwin",
  "type": "user",
  "createdAt": "2026-02-04T00:27:39.000Z",
  "updatedAt": "2026-02-04T00:27:39.000Z",
  "guardians": []
}
```

## 🧪 Testing: Create a Test User

### Step 1: Sign Up in App
1. Open app
2. Enter email: `test@example.com`
3. Enter password: `password123`
4. Select mode (User/Guardian)
5. Click "Sign Up"

### Step 2: Verify in Emulator UI

**Check Authentication:**
- Go to http://localhost:4000 → Authentication
- Should see: `test@example.com`

**Check Firestore:**
- Go to http://localhost:4000 → Firestore → users
- Should see document with:
  - Email: `test@example.com`
  - Type: `user` or `guardian` (based on selection)

## 🔧 Troubleshooting

### User Not Showing in Authentication

**Possible Causes:**
1. **Emulators not running** → Start with `firebase emulators:start`
2. **App connected to production** → Check `FirebaseConfig.kt` for emulator connection
3. **Signup failed** → Check Logcat for errors

**Check:**
```powershell
# Verify emulators are running
netstat -an | findstr "9099 8080 4000"
```

### User in Authentication but Not in Firestore

**Possible Causes:**
1. **Signup succeeded but Firestore document creation failed**
2. **Check Logcat for errors:**
   ```
   E/UserService: Error creating/updating user document
   ```

**Solution:**
- User can still login (Auth exists)
- But profile data is missing
- Can manually create document in Firestore UI

### Can't Access Emulator UI

**Check:**
1. **Emulators running?**
   ```powershell
   cd backend
   firebase emulators:start
   ```

2. **Port 4000 available?**
   ```powershell
   netstat -ano | findstr :4000
   ```

3. **Browser URL correct?**
   - Should be: **http://localhost:4000**
   - NOT: https://localhost:4000

## 📝 Summary

| Location | What's Stored | How to Check |
|----------|---------------|--------------|
| **Firebase Auth** | Email/Password credentials | http://localhost:4000 → Authentication |
| **Firestore `users`** | Profile data (type, name, etc.) | http://localhost:4000 → Firestore → users |

**Both must exist for full functionality:**
- ✅ Auth exists → User can login
- ✅ Firestore exists → User has profile data
- ❌ Only Auth → Can login but missing profile
- ❌ Only Firestore → Cannot login

## 🎯 Quick Reference

**Emulator UI:** http://localhost:4000
- **Authentication Tab:** See all registered users (email/password)
- **Firestore Tab:** See user profiles (type, guardians, wards)

**Production Console:** https://console.firebase.google.com/
- Same structure, but shows production data
