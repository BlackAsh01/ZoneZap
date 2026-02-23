# Guardian-User Mapping Guide

This guide explains how to map multiple users (wards) to a guardian in the ZoneZap application.

## 🎯 Overview

A **guardian** can monitor **multiple users** (wards). The relationship is bidirectional:
- User's `guardians` array contains guardian IDs
- Guardian's `wards` array contains user IDs

## 📱 Method 1: Using the Mobile App (Recommended)

### For Guardians:

1. **Login as Guardian**
   - Select "Guardian" mode on login screen
   - Enter guardian credentials
   - Navigate to Guardian Dashboard

2. **Add a Ward**
   - Click **"Add Ward"** button (top-right of "Your Wards" section)
   - Enter the **email address** of the user you want to monitor
   - Click "Add"
   - The user will be added to your wards list

3. **View Your Wards**
   - All your wards appear in the "Your Wards" section
   - You can see alerts from all your wards

### Example:
```
Guardian Email: guardian@example.com
Ward 1 Email: user1@example.com
Ward 2 Email: user2@example.com
Ward 3 Email: user3@example.com
```

## 🔧 Method 2: Programmatically (Using UserService)

### Kotlin Code Example:

```kotlin
import com.zonezapapp.services.UserService

val userService = UserService()

// Add a ward to a guardian
lifecycleScope.launch {
    val guardianId = FirebaseConfig.auth.currentUser?.uid ?: return@launch
    val userEmail = "user@example.com"
    
    // Find user by email
    val user = userService.findUserByEmail(userEmail)
    if (user != null) {
        val userId = user["userId"] as? String
        val success = userService.addWardToGuardian(guardianId, userId)
        if (success) {
            // Ward added successfully
        }
    }
}
```

### Available Functions:

1. **`addWardToGuardian(guardianId, userId)`**
   - Adds a user to guardian's wards list
   - Also adds guardian to user's guardians list (bidirectional)

2. **`removeWardFromGuardian(guardianId, userId)`**
   - Removes a user from guardian's wards list
   - Also removes guardian from user's guardians list

3. **`findUserByEmail(email)`**
   - Finds a user by email address
   - Returns user data including userId, name, email

4. **`getAllUsers()`**
   - Gets all users (type: "user")
   - Useful for displaying a list to select from

## 🗄️ Method 3: Direct Firestore Update

### Using Firebase Console or Emulator UI:

1. **For User Document:**
   ```json
   {
     "userId": "user123",
     "email": "user@example.com",
     "type": "user",
     "guardians": ["guardian1_id", "guardian2_id"]
   }
   ```

2. **For Guardian Document:**
   ```json
   {
     "userId": "guardian1_id",
     "email": "guardian@example.com",
     "type": "guardian",
     "wards": ["user1_id", "user2_id", "user3_id"]
   }
   ```

### Steps:
1. Open Firebase Emulator UI (http://localhost:4000) or Firebase Console
2. Go to Firestore Database
3. Navigate to `users/{guardianId}` document
4. Edit the `wards` array field
5. Add user IDs to the array
6. Also update the user's `guardians` array with the guardian ID

## 📊 Data Structure

### Guardian Document:
```json
{
  "userId": "guardian_firebase_auth_uid",
  "email": "guardian@example.com",
  "name": "Guardian Name",
  "type": "guardian",
  "wards": [
    "user1_firebase_auth_uid",
    "user2_firebase_auth_uid",
    "user3_firebase_auth_uid"
  ],
  "createdAt": "Timestamp",
  "updatedAt": "Timestamp"
}
```

### User Document:
```json
{
  "userId": "user_firebase_auth_uid",
  "email": "user@example.com",
  "name": "User Name",
  "type": "user",
  "guardians": [
    "guardian1_firebase_auth_uid",
    "guardian2_firebase_auth_uid"
  ],
  "createdAt": "Timestamp",
  "updatedAt": "Timestamp"
}
```

## 🔄 How It Works

### When a Guardian Adds a Ward:

1. **Guardian enters user email** → App searches for user
2. **User found** → App gets user's Firebase Auth UID
3. **Bidirectional update:**
   - Adds user ID to guardian's `wards` array
   - Adds guardian ID to user's `guardians` array
4. **Guardian Dashboard updates** → Shows new ward in list
5. **Guardian can now see** → All alerts from this ward

### When a User Sends an Alert:

1. **User triggers emergency** → Alert created in Firestore
2. **System checks user's guardians** → Gets all guardian IDs
3. **Guardians see alert** → Appears in Guardian Dashboard
4. **Guardian can resolve** → Mark alert as resolved

## ✅ Verification

### Check if Mapping Worked:

1. **In Guardian Dashboard:**
   - Login as guardian
   - Check "Your Wards" section
   - Should see all added users

2. **In Firestore:**
   - Check `users/{guardianId}` → `wards` array should contain user IDs
   - Check `users/{userId}` → `guardians` array should contain guardian ID

3. **Test Alert Flow:**
   - Login as user
   - Send emergency alert
   - Login as guardian
   - Alert should appear in "Active Alerts"

## 🚨 Important Notes

1. **Bidirectional Relationship:**
   - Always update both sides (guardian's wards AND user's guardians)
   - UserService functions handle this automatically

2. **User Must Exist:**
   - User must be registered and have type "user"
   - Guardian must have type "guardian"

3. **No Duplicates:**
   - Functions check for duplicates before adding
   - Same user won't be added twice

4. **Security:**
   - Only authenticated users can add/remove wards
   - Firestore rules enforce permissions

## 📝 Example Scenarios

### Scenario 1: Family Guardian
```
Guardian: parent@example.com
Wards:
  - child1@example.com
  - child2@example.com
  - child3@example.com
```

### Scenario 2: Care Facility
```
Guardian: nurse@carefacility.com
Wards:
  - resident1@example.com
  - resident2@example.com
  - resident3@example.com
  - resident4@example.com
  - resident5@example.com
```

### Scenario 3: Multiple Guardians for One User
```
User: elderly@example.com
Guardians:
  - son@example.com
  - daughter@example.com
  - neighbor@example.com
```

## 🔍 Troubleshooting

### "User not found"
- Verify user email is correct
- Check user exists in Firestore
- Ensure user type is "user"

### "Ward not appearing"
- Refresh Guardian Dashboard
- Check Firestore `wards` array
- Verify bidirectional relationship exists

### "Can't see alerts"
- Ensure user is in guardian's `wards` array
- Check alert's `userId` matches ward's ID
- Verify Firestore security rules allow access
