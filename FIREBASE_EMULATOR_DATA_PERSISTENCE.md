# Firebase Emulator Data Persistence

## Problem

By default, Firebase emulators lose all data when you restart them. This makes development difficult as you need to recreate test data every time.

## Solution: Export/Import Emulator Data

Firebase emulators support exporting and importing data to persist it between restarts.

## Configuration

The `firebase.json` file has been updated to include:
- `emulatorExportPath`: Directory where emulator data will be exported
- `singleProjectMode`: Ensures consistent project ID across emulators

## Usage

### Method 1: Automatic Export on Exit (Recommended)

When you stop the emulators with `Ctrl+C`, they will automatically export data to `.firebase/emulator-export/`.

**To use exported data:**

1. **Start emulators with import:**
   ```powershell
   cd backend
   firebase emulators:start --import=.firebase/emulator-export
   ```

2. **Start emulators with import and export on exit:**
   ```powershell
   cd backend
   firebase emulators:start --import=.firebase/emulator-export --export-on-exit=.firebase/emulator-export
   ```

### Method 2: Manual Export/Import

**Export data manually:**
```powershell
# While emulators are running, in another terminal:
firebase emulators:export .firebase/emulator-export
```

**Import data when starting:**
```powershell
cd backend
firebase emulators:start --import=.firebase/emulator-export
```

### Method 3: Create a Script (Easiest)

Create a file `backend/start-emulators.ps1`:

```powershell
# Start emulators with import and auto-export
firebase emulators:start --import=.firebase/emulator-export --export-on-exit=.firebase/emulator-export
```

Then run:
```powershell
cd backend
.\start-emulators.ps1
```

Or create `backend/start-emulators.sh` for Linux/Mac:

```bash
#!/bin/bash
firebase emulators:start --import=.firebase/emulator-export --export-on-exit=.firebase/emulator-export
```

Make it executable:
```bash
chmod +x start-emulators.sh
```

## What Gets Exported

- **Auth Emulator**: All user accounts, tokens, etc.
- **Firestore Emulator**: All collections and documents
- **Functions Emulator**: Function logs (if applicable)

## Directory Structure

After first export, you'll have:
```
backend/
  .firebase/
    emulator-export/
      firestore_export/
        firestore_export.overall_export_metadata
        firestore_export/
          (collections and documents)
      auth_export/
        accounts.json
        (other auth data)
```

## Best Practices

1. **Commit export directory to .gitignore:**
   Add to `.gitignore`:
   ```
   backend/.firebase/emulator-export/
   ```

2. **Use separate exports for different scenarios:**
   ```powershell
   # Test data
   firebase emulators:export .firebase/test-data
   
   # Development data
   firebase emulators:export .firebase/dev-data
   ```

3. **Clear data when needed:**
   ```powershell
   # Delete export directory to start fresh
   Remove-Item -Recurse -Force .firebase/emulator-export
   ```

## Troubleshooting

### Export directory doesn't exist
- First run will create it automatically
- Or create it manually: `mkdir -p .firebase/emulator-export`

### Import fails
- Make sure the export directory exists
- Check that it contains valid export files
- Try deleting and re-exporting

### Data not persisting
- Make sure you're using `--import` flag when starting
- Check that `--export-on-exit` is set if you want auto-export
- Verify the export directory path is correct

## Example Workflow

1. **First time setup:**
   ```powershell
   cd backend
   firebase emulators:start --export-on-exit=.firebase/emulator-export
   ```
   - Create test users, data, etc.
   - Stop with `Ctrl+C` (data auto-exports)

2. **Subsequent runs:**
   ```powershell
   cd backend
   firebase emulators:start --import=.firebase/emulator-export --export-on-exit=.firebase/emulator-export
   ```
   - All your data is restored
   - Changes are auto-saved on exit

3. **Reset data:**
   ```powershell
   Remove-Item -Recurse -Force .firebase/emulator-export
   firebase emulators:start --export-on-exit=.firebase/emulator-export
   ```

## Notes

- Export/import only works with emulators, not production Firebase
- Large datasets may take time to export/import
- The export format is Firebase-specific and not human-readable JSON
- You can have multiple export directories for different test scenarios
