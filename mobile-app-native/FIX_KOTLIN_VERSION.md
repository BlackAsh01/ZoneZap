# Fixing Kotlin Version Conflict (kotlin-stdlib-jdk7-1.7.20)

## Problem
The build is failing with `java.lang.NoClassDefFoundError: kotlin/enums/EnumEntriesKt` because an old Kotlin stdlib version (1.7.20) is being pulled in, which doesn't have the `EnumEntriesKt` class that was introduced in Kotlin 1.9+.

## Changes Made

### 1. Version Alignment
- ✅ Updated `build.gradle` Kotlin version to `2.0.21`
- ✅ Aligned all forced Kotlin stdlib versions to `2.0.21` across all files
- ✅ Added explicit rejection rules using `componentSelection` to prevent old versions

### 2. Files Modified
- `build.gradle` - Added resolution strategy to buildscript classpath
- `app/build.gradle` - Enhanced resolution strategy with component rejection
- `settings.gradle` - Already has plugin resolution strategy
- `gradle/init.gradle` - Updated with consistent versions and rejection rules

## Steps to Fix

### Step 1: Clean All Gradle Caches

**Option A: Use the provided PowerShell script**
```powershell
cd "C:\Users\ashwi\OneDrive - School of Management Studies, Vels University(VISTAS), (Estd. u s 3 of the UGC Act 1956)\Desktop\Project - Shiv\mobile-app-native"
.\clean-gradle-caches.ps1
```

**Option B: Manual cleanup**
1. Close Android Studio completely
2. Delete these directories:
   - `mobile-app-native\.gradle`
   - `mobile-app-native\build`
   - `mobile-app-native\app\build`
   - `%USERPROFILE%\.gradle\caches\modules-2\files-2.1\org.jetbrains.kotlin\kotlin-stdlib-jdk7\1.7.20`
   - `%USERPROFILE%\.gradle\caches\transforms-3` (entire directory)
   - `%USERPROFILE%\.gradle\caches\build-cache-1` (entire directory)

### Step 2: Configure Init Script in Android Studio

The `gradle/init.gradle` script needs to be configured in Android Studio:

1. Open Android Studio
2. Go to **File → Settings** (or **Android Studio → Preferences** on Mac)
3. Navigate to **Build, Execution, Deployment → Build Tools → Gradle**
4. Under **Gradle projects**, find **Init script** section
5. Click the **+** button and add:
   ```
   C:\Users\ashwi\OneDrive - School of Management Studies, Vels University(VISTAS), (Estd. u s 3 of the UGC Act 1956)\Desktop\Project - Shiv\mobile-app-native\gradle\init.gradle
   ```
6. Click **Apply** and **OK**

**Alternative:** Copy `gradle/init.gradle` to `%USERPROFILE%\.gradle\init.d\init.gradle` (this applies to all projects)

### Step 3: Sync and Rebuild

1. In Android Studio, click **File → Sync Project with Gradle Files**
2. Wait for sync to complete
3. Click **Build → Clean Project**
4. Click **Build → Rebuild Project**

### Step 4: Verify Dependencies (Optional)

If the error persists, check what's pulling in the old version:

```powershell
cd "C:\Users\ashwi\OneDrive - School of Management Studies, Vels University(VISTAS), (Estd. u s 3 of the UGC Act 1956)\Desktop\Project - Shiv\mobile-app-native"
# If gradlew.bat exists:
.\gradlew.bat dependencies --configuration classpath | Select-String -Pattern "kotlin-stdlib.*1\.7"
.\gradlew.bat dependencies --configuration compileClasspath | Select-String -Pattern "kotlin-stdlib.*1\.7"
```

## What the Fixes Do

1. **Force Versions**: All Kotlin stdlib dependencies are forced to version `2.0.21`
2. **Reject Old Versions**: `componentSelection` explicitly rejects any version that's not `2.0.21`
3. **Transform Old Versions**: `eachDependency` replaces any old versions with `2.0.21`
4. **Buildscript Protection**: Resolution strategy applied to buildscript classpath to prevent old versions in plugin dependencies

## If Error Persists

If you still see the error after following these steps:

1. **Check Android Studio Gradle Version**: Ensure Android Studio is using the correct Gradle version (8.13 as specified in `gradle-wrapper.properties`)

2. **Verify Init Script**: Check Android Studio's Gradle console output for messages about the init script being loaded

3. **Try Different Kotlin Version**: If `2.0.21` causes issues, try `2.0.20` or `2.0.0` (update all files consistently)

4. **Check AGP Compatibility**: Android Gradle Plugin 8.13.2 should work with Kotlin 2.0.x, but verify compatibility

5. **Report Dependency Tree**: Run the dependency check commands above and share the output

## Technical Details

The error occurs because:
- Kotlin build tools (expecting Kotlin 1.9+ features) encounter `kotlin-stdlib-jdk7-1.7.20.jar`
- This old version doesn't have `kotlin/enums/EnumEntriesKt` class
- The transform operation fails when trying to process the old JAR

Our solution ensures that:
- All Kotlin stdlib dependencies resolve to version `2.0.21`
- Old versions are explicitly rejected before resolution
- Buildscript classpath also uses the correct versions
