# ZoneZap - Native Android App

A native Android application built with Kotlin for safety and care management. This app provides emergency alerts, location tracking, and reminder functionality.

## 🎯 Why Native Android?

This native Android app was created to avoid the Gradle complexity issues with React Native. It uses:
- **Kotlin** - Modern, concise language
- **Standard Android Gradle** - No complex configurations
- **Material Design** - Beautiful, native UI
- **Firebase** - Backend services
- **Android Studio** - Full IDE support

## ✨ Features

- ✅ Firebase Authentication (Email/Password)
- ✅ Real-time Location Tracking
- ✅ Emergency Panic Button
- ✅ Wandering Alerts
- ✅ Reminders Management
- ✅ Location Logging to Firestore
- ✅ Material Design UI

## 📋 Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17 or later
- Android SDK (API 24+)
- Firebase project with Authentication and Firestore enabled

## 🚀 Setup Instructions

### 1. Open Project in Android Studio

1. Open **Android Studio**
2. Click **File > Open**
3. Navigate to `mobile-app-native` folder
4. Click **OK**
5. Wait for Gradle sync to complete (should be fast and error-free!)

### 2. Configure Firebase

1. Copy `google-services.json` from your Firebase project to `app/` directory
   - The file should already be there if copied from the React Native app
   - If not, download it from Firebase Console > Project Settings > Your Apps

2. Verify `google-services.json` is in the correct location:
   ```
   mobile-app-native/app/google-services.json
   ```

### 3. Build and Run

1. Connect an Android device or start an emulator
2. Click **Run** button (green play icon) or press `Shift+F10`
3. The app will build and install automatically

## 📁 Project Structure

```
mobile-app-native/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/zonezapapp/
│   │       │   ├── config/          # Firebase configuration
│   │       │   ├── data/            # Data models
│   │       │   ├── services/        # Business logic services
│   │       │   └── ui/              # Activities and UI
│   │       │       ├── login/       # Login screen
│   │       │       ├── home/        # Home screen
│   │       │       ├── panic/       # Panic/emergency screen
│   │       │       └── reminders/  # Reminders screen
│   │       ├── res/                 # Resources (layouts, strings, etc.)
│   │       └── AndroidManifest.xml
│   └── build.gradle
├── build.gradle                     # Project-level Gradle
├── settings.gradle
└── gradle.properties
```

## 🔧 Key Files

- `app/build.gradle` - App dependencies and configuration
- `app/src/main/AndroidManifest.xml` - App permissions and activities
- `app/src/main/java/com/zonezapapp/config/FirebaseConfig.kt` - Firebase setup
- `app/src/main/java/com/zonezapapp/services/` - Service classes

## 📱 Screens

1. **LoginActivity** - Email/password authentication
2. **HomeActivity** - Dashboard with location and reminders
3. **PanicActivity** - Emergency alert buttons
4. **RemindersActivity** - Manage reminders

## 🛠️ Dependencies

- **Firebase** - Authentication, Firestore, Messaging
- **Material Components** - UI components
- **Location Services** - GPS tracking
- **Coroutines** - Async operations
- **Navigation** - Screen navigation

## ✅ Advantages Over React Native

1. **No Gradle Issues** - Uses standard Android Gradle configuration
2. **Faster Builds** - No Metro bundler, direct compilation
3. **Better Performance** - Native code execution
4. **Full Android Studio Support** - Debugging, profiling, etc.
5. **Native UI** - Material Design components
6. **Easier Debugging** - Standard Android debugging tools

## 🔐 Permissions

The app requires:
- **Internet** - Firebase connectivity
- **Location** - GPS tracking for safety
- **Vibrate** - Emergency alerts
- **Notifications** - Push notifications (future)

## 🐛 Troubleshooting

### Gradle Sync Issues
- **Clean Project**: Build > Clean Project
- **Invalidate Caches**: File > Invalidate Caches / Restart
- **Check JDK**: File > Project Structure > SDK Location

### Firebase Issues
- Verify `google-services.json` is in `app/` directory
- Check Firebase project settings match the JSON file
- Ensure Firebase Authentication and Firestore are enabled

### Location Issues
- Grant location permissions when prompted
- Enable location services on device
- Check device location settings

## 📝 Notes

- This is a native Android app - no Node.js or npm required
- All dependencies are managed by Gradle
- Firebase configuration is automatic via `google-services.json`
- The app follows Android best practices and Material Design guidelines

## 🎉 Success!

If you can open the project in Android Studio and sync Gradle without errors, you're all set! The native Android approach eliminates all the React Native Gradle complexity.

---

**Built with ❤️ using Kotlin and Android Studio**
