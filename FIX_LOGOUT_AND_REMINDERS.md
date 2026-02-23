# Fix for Logout and Reminders Permission Issues

## Issues Fixed

### 1. Logout Closes App Instead of Showing Login Screen

**Problem:** When clicking logout, the app closes instead of navigating to the login screen.

**Root Cause:** 
- `signOut()` is called but LoginActivity auto-navigates if it detects a logged-in user
- Activity stack wasn't properly cleared
- `finish()` only closes current activity, not the entire stack

**Solution:**
- Added `FLAG_ACTIVITY_NEW_TASK` and `FLAG_ACTIVITY_CLEAR_TASK` flags to Intent
- Changed `finish()` to `finishAffinity()` to close all activities in the task
- This ensures LoginActivity is shown as a fresh start

**Files Changed:**
- `mobile-app-native/app/src/main/java/com/zonezapapp/ui/home/HomeActivity.kt`
- `mobile-app-native/app/src/main/java/com/zonezapapp/ui/guardian/GuardianActivity.kt`

### 2. Reminders Permission Denied Error

**Problem:** 
```
PERMISSION_DENIED: false for 'list' @ L32
Query: reminders where userId==... and isCompleted==false
```

**Root Cause:**
- Firebase emulators may have cached old rules
- Rules file needs to be reloaded

**Solution:**
The rules are correct in `backend/firestore.rules`:
```firestore
// Reminders collection
match /reminders/{reminderId} {
  allow get: if request.auth != null && resource.data.userId == request.auth.uid;
  allow list: if request.auth != null;  // Line 41 - allows list queries
  allow create: if request.auth != null && request.resource.data.userId == request.auth.uid;
  allow update, delete: if request.auth != null && resource.data.userId == request.auth.uid;
}
```

**Action Required:**
Restart Firebase emulators to reload the rules:

```bash
# Stop emulators (Ctrl+C)
cd backend
firebase emulators:start
```

Or if using production Firebase:
```bash
cd backend
firebase deploy --only firestore:rules
```

## Testing

### Test Logout:
1. Login to the app
2. Click logout (menu or button)
3. Should navigate to LoginActivity (not close app)
4. Login screen should be visible

### Test Reminders:
1. After restarting emulators
2. Login as user
3. Navigate to reminders or home screen
4. Reminders should load without permission errors

## Code Changes

### HomeActivity.kt - Logout Fix:
```kotlin
private fun logout() {
    AlertDialog.Builder(this)
        .setTitle("Logout")
        .setMessage("Are you sure you want to logout?")
        .setPositiveButton("Logout") { _, _ ->
            FirebaseConfig.auth.signOut()
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finishAffinity()  // Closes all activities in the task
        }
        .setNegativeButton("Cancel", null)
        .show()
}
```

### GuardianActivity.kt - Logout Fix:
```kotlin
private fun logout() {
    FirebaseConfig.auth.signOut()
    val intent = Intent(this, LoginActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    startActivity(intent)
    finishAffinity()
}
```

## Notes

- `FLAG_ACTIVITY_CLEAR_TASK`: Clears all activities in the task before starting the new activity
- `FLAG_ACTIVITY_NEW_TASK`: Starts the activity in a new task
- `finishAffinity()`: Finishes the current activity and all parent activities in the same task
- These flags ensure a clean navigation to LoginActivity after logout
