# Fix: Firestore Permission Denied Error

## 🔍 The Problem

Error: `PERMISSION_DENIED - Property data is undefined on object. for 'create' @ L17`

The Firestore security rules were using `request.data` for create operations, but the correct syntax is `request.resource.data`.

## ✅ Solution Applied

Updated Firestore rules to use correct syntax:
- **Before:** `request.data.userId` (incorrect for create)
- **After:** `request.resource.data.userId` (correct for create)

## 📝 Changes Made

### 1. Alerts Collection
- Fixed `allow create` to use `request.resource.data.userId`

### 2. Reminders Collection  
- Split `allow read, write` into separate rules
- Fixed `allow create` to use `request.resource.data.userId`
- Kept `allow update, delete` using `resource.data.userId` (for existing documents)

### 3. Movement Logs Collection
- Fixed `allow create` to use `request.resource.data.userId`

## 🚀 Deploy Updated Rules

### If Using Firebase Emulators:

The rules are automatically loaded from `backend/firestore.rules`. Just restart emulators:

```powershell
cd backend
firebase emulators:start
```

### If Using Production Firebase:

Deploy the updated rules:

```powershell
cd backend
firebase deploy --only firestore:rules
```

## ✅ Verify It's Working

After updating rules and restarting emulators:

1. **Try creating an alert** (panic button)
2. **Check Logcat** - should see no permission errors
3. **Check Emulator UI** - http://localhost:4000
   - Go to Firestore tab
   - Should see alerts being created

## 📚 Firestore Rules Syntax Reference

- **`request.resource.data`** - Data being written (for create/update)
- **`resource.data`** - Existing document data (for read/update/delete)
- **`request.auth.uid`** - Current user's ID

## 🐛 If Still Getting Errors

### Check Rules Syntax:
```powershell
cd backend
firebase deploy --only firestore:rules --dry-run
```

### Check Emulator Logs:
- Look for rule evaluation errors in emulator output
- Check Emulator UI → Firestore → Rules tab

### Common Issues:
- **"Property undefined"** - Check field names match exactly
- **"Permission denied"** - Verify user is authenticated (`request.auth != null`)
- **"Evaluation error"** - Check rule syntax and field access

## 📝 Summary

- ✅ Fixed `request.data` → `request.resource.data` for create operations
- ✅ Split combined rules into separate read/create/update/delete rules
- ✅ Rules now correctly validate user permissions
- ✅ Restart emulators to apply changes

**The permission errors should be resolved!** 🎉
