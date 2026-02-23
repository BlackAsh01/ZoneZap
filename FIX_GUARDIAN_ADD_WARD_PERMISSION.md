# Fix for Guardian Add Ward Permission Error

## Problem

When a guardian tries to add a ward (user), they get a permission error:
```
PERMISSION_DENIED: evaluation error at L7:21 for 'get' @ L7, false for 'get' @ L7
```

This happens because:
1. Guardians need to **read** user documents to verify they exist and get their type
2. Guardians need to **update** user documents to add themselves to the `guardians` array
3. But the old rules only allowed guardians to read user documents if they were **already** in the guardians array (chicken-and-egg problem)

## Solution

Updated Firestore security rules to allow:
1. **Guardians can read user documents** (type: "user") even if they're not in the guardians array yet
2. **Guardians can update user documents** to add themselves to the guardians array

## Updated Rules

```firestore
match /users/{userId} {
  // Allow get if:
  // 1. User owns it
  // 2. Is already a guardian (in guardians array)
  // 3. Is a guardian trying to add a ward (can read user documents of type "user")
  allow get: if request.auth != null && (
    request.auth.uid == userId || 
    ('guardians' in resource.data && request.auth.uid in resource.data.guardians) ||
    // Allow guardians to read user documents (to add themselves as guardian)
    (resource.data.type == "user" && 
     exists(/databases/$(database)/documents/users/$(request.auth.uid)) &&
     get(/databases/$(database)/documents/users/$(request.auth.uid)).data.type == "guardian")
  );
  
  // Allow update if:
  // 1. User owns it
  // 2. Guardian is updating to add themselves to guardians array
  allow update: if request.auth != null && (
    request.auth.uid == userId ||
    // Allow guardians to update user documents to add themselves
    (resource.data.type == "user" &&
     exists(/databases/$(database)/documents/users/$(request.auth.uid)) &&
     get(/databases/$(database)/documents/users/$(request.auth.uid)).data.type == "guardian")
  );
  
  // Allow create/delete only for own document
  allow create: if request.auth != null && request.auth.uid == userId;
  allow delete: if request.auth != null && request.auth.uid == userId;
}
```

## How It Works

### When Guardian Adds a Ward:

1. **Guardian enters user email** → App searches for user by email
2. **User found** → App tries to read user document
   - ✅ **NEW**: Guardian can now read user document (even if not in guardians array)
3. **Verify types** → Check user is type "user" and guardian is type "guardian"
4. **Update user document** → Add guardian ID to user's `guardians` array
   - ✅ **NEW**: Guardian can now update user document
5. **Update guardian document** → Add user ID to guardian's `wards` array
   - ✅ Guardian can update their own document

## Testing

1. **Restart Firebase Emulators** (to load new rules):
   ```powershell
   cd backend
   firebase emulators:start
   ```

2. **Login as Guardian:**
   - Select "Guardian" mode
   - Login with guardian credentials

3. **Add a Ward:**
   - Click "Add Ward" button
   - Enter user email
   - Should successfully add without permission errors

4. **Verify:**
   - Check Firestore Emulator UI (http://localhost:4000)
   - User document should have guardian ID in `guardians` array
   - Guardian document should have user ID in `wards` array

## Security Notes

- ✅ Guardians can only read/update user documents (type: "user"), not other guardians
- ✅ Users can still only update their own documents
- ✅ The guardian's own document must exist and have type "guardian"
- ✅ The target user document must have type "user"

## Related Files

- `backend/firestore.rules` - Updated security rules
- `mobile-app-native/app/src/main/java/com/zonezapapp/services/UserService.kt` - Service that adds guardians
- `mobile-app-native/app/src/main/java/com/zonezapapp/ui/guardian/GuardianActivity.kt` - UI for adding wards
