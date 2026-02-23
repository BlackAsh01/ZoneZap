# Guardian Interface - Setup Guide

## 🎯 What Was Created

I've created a **Guardian Dashboard** interface for guardians to monitor their wards (users they're responsible for).

## 📱 Guardian Features

### GuardianActivity Features:
1. **View Active Alerts** - See all emergency alerts from wards
2. **View Wards List** - See all users assigned to this guardian
3. **Resolve Alerts** - Mark alerts as resolved
4. **Alert Details** - View alert information including location

## 🚀 How to Use

### Option 1: Automatic Detection (Recommended)

When a user logs in, the app automatically checks if they're a guardian:
- If they have wards → Goes to Guardian Dashboard
- If they're a regular user → Goes to Home Screen

### Option 2: Guardian Login Button

1. **On Login Screen:**
   - Enter email/password
   - Click **"Login as Guardian"** button (at bottom)
   - Goes directly to Guardian Dashboard

## 🔧 Setting Up Guardians

### To Make Someone a Guardian:

1. **Create a user account** (regular signup)

2. **Add them as guardian to a user:**
   - In Firestore, go to `users/{userId}` document
   - Add a `guardians` field (array of guardian user IDs)
   - Example:
     ```json
     {
       "userId": "user123",
       "name": "John Doe",
       "email": "john@example.com",
       "guardians": ["guardian1_id", "guardian2_id"]
     }
     ```

3. **Guardian can now login** and see alerts from their wards

## 📋 Guardian Dashboard Shows

### Active Alerts Section:
- All emergency alerts from all wards
- Alert type (PANIC, WANDERING, etc.)
- Timestamp
- Status
- Location (if available)

### Your Wards Section:
- List of all users assigned to this guardian
- Ward name and email

### Actions:
- **Click alert** → View details and resolve
- **Click ward** → View ward details (can be expanded)
- **Logout** → Return to login screen

## 🗄️ Firestore Structure

### Users Collection:
```json
{
  "users/{userId}": {
    "name": "User Name",
    "email": "user@example.com",
    "guardians": ["guardian1_id", "guardian2_id"]
  }
}
```

### Alerts Collection:
```json
{
  "alerts/{alertId}": {
    "userId": "ward_user_id",
    "alertType": "PANIC",
    "status": "ACTIVE",
    "timestamp": "...",
    "location": {
      "latitude": 0.0,
      "longitude": 0.0
    }
  }
}
```

## 🔐 Security

The Firestore rules already support guardians:
- Guardians can read alerts from their wards
- Guardians can update alerts (resolve them)
- Guardians can read user data for their wards

## 📝 Testing Guardian Interface

### Step 1: Create Test Users

1. **Create User Account:**
   - Email: `user@test.com`
   - Password: `password123`

2. **Create Guardian Account:**
   - Email: `guardian@test.com`
   - Password: `password123`

### Step 2: Link Guardian to User

In Firebase Emulator UI (http://localhost:4000):
1. Go to Firestore
2. Create/Edit `users/{user_id}` document
3. Add field: `guardians` (type: array)
4. Add guardian's user ID to the array

### Step 3: Test Guardian Login

1. **Login as guardian:**
   - Email: `guardian@test.com`
   - Password: `password123`
   - Click "Login as Guardian"

2. **Should see:**
   - Guardian Dashboard
   - Wards list (if linked)
   - Active alerts (if any)

### Step 4: Create Alert from User App

1. **Login as user** (`user@test.com`)
2. **Click Panic Button**
3. **Alert should appear** in Guardian Dashboard

## 🎨 UI Layout

The Guardian Dashboard includes:
- **Toolbar** - "Guardian Dashboard" title
- **Your Wards** - Horizontal scrolling list
- **Active Alerts** - Vertical list of alerts
- **Logout Button** - Return to login

## 🔄 Real-time Updates

The Guardian Dashboard:
- ✅ Loads alerts on startup
- ✅ Shows active alerts from all wards
- ✅ Can resolve alerts
- ⚠️ **Note:** Real-time updates not yet implemented (can be added with Firestore listeners)

## 📱 Files Created

1. **`GuardianActivity.kt`** - Guardian dashboard screen
2. **`activity_guardian.xml`** - Guardian dashboard layout
3. **Updated `LoginActivity.kt`** - Added guardian login option
4. **Updated `AndroidManifest.xml`** - Registered GuardianActivity

## 🚀 Next Steps

To use the guardian interface:

1. **Rebuild the app:**
   - Build → Clean Project
   - Build → Rebuild Project

2. **Set up guardian relationships** in Firestore

3. **Login as guardian** to see the dashboard

## 💡 Future Enhancements

Possible additions:
- Real-time alert updates (Firestore listeners)
- Ward location tracking on map
- Alert history
- Push notifications (already in Cloud Functions)
- Ward management (add/remove wards)

---

**The Guardian Interface is now ready to use!** 🎉
