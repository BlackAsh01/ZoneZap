# Fix for Reminders Permission Denied Error

## Error Message
```
PERMISSION_DENIED: false for 'list' @ L41
Query: reminders where userId==... and isCompleted==false
```

## Root Cause

The Firebase emulators need to be restarted to load the updated Firestore security rules. The rules file has been updated, but the emulators are still using the old cached rules.

## Solution

### Step 1: Restart Firebase Emulators

1. **Stop the current emulators:**
   - Find the terminal/PowerShell window where emulators are running
   - Press `Ctrl+C` to stop them

2. **Restart the emulators:**
   ```powershell
   cd backend
   firebase emulators:start
   ```

3. **Wait for startup:**
   ```
   ✔  auth: Emulator started at http://localhost:9099
   ✔  firestore: Emulator started at http://localhost:8080
   ✔  ui: Emulator UI started at http://localhost:4000
   ```

### Step 2: Rebuild and Run the App

1. **In Android Studio:**
   - Build → Clean Project
   - Build → Rebuild Project
   - Run the app again

### Step 3: Verify User Document Exists

The app now automatically creates the user document on login if it doesn't exist. This ensures the Firestore rules can properly evaluate permissions.

## Current Firestore Rules

The rules in `backend/firestore.rules` are correct:

```firestore
// Reminders collection
match /reminders/{reminderId} {
  // Allow get if user owns the reminder OR guardian created it
  allow get: if request.auth != null && (
    resource.data.userId == request.auth.uid ||
    (resource.data.userId != request.auth.uid &&
     exists(/databases/$(database)/documents/users/$(resource.data.userId)) && 
     'guardians' in get(/databases/$(database)/documents/users/$(resource.data.userId)).data &&
     request.auth.uid in get(/databases/$(database)/documents/users/$(resource.data.userId)).data.guardians)
  );
  // Allow list for authenticated users
  allow list: if request.auth != null;
  // ... rest of rules
}
```

## Why This Happens

1. **Firestore emulators cache rules in memory** - When you update `firestore.rules`, the emulators need to be restarted to load the new rules
2. **Rules evaluation** - When Firestore evaluates a list query, it checks the `allow get` permission for each document that would be returned
3. **User document requirement** - The guardian check requires the user document to exist, which is now created automatically on login

## Testing

After restarting emulators:

1. **Login as a user**
2. **Navigate to HomeActivity**
3. **Reminders should load without permission errors**
4. **Check Logcat** - Should not see `PERMISSION_DENIED` errors

## Additional Notes

- The app now includes error handling for reminder loading failures
- User documents are automatically created on login if they don't exist
- Coroutines are properly cancelled on logout to prevent crashes

## If Error Persists

If the error still occurs after restarting emulators:

1. **Check Firestore Emulator UI:**
   - Open http://localhost:4000
   - Go to Firestore section
   - Verify rules are loaded correctly

2. **Check User Document:**
   - In Firestore Emulator UI, check if user document exists in `users` collection
   - Verify it has `type`, `email`, and other required fields

3. **Clear Emulator Data:**
   ```powershell
   # Stop emulators
   # Delete emulator data directory (usually in backend/.firebase/)
   # Restart emulators
   ```

4. **Verify Rules Syntax:**
   - Check `backend/firestore.rules` for syntax errors
   - Use Firebase CLI to validate: `firebase deploy --only firestore:rules --dry-run`
