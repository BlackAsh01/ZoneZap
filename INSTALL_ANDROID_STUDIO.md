# Installing Android Studio for ZoneZap

Step-by-step guide to install Android Studio and run your app.

## 📥 Step 1: Download Android Studio

1. **Visit:** https://developer.android.com/studio
2. **Click:** "Download Android Studio"
3. **Accept terms** and download
4. **File size:** ~1GB (takes 5-10 minutes to download)

## 🔧 Step 2: Install Android Studio

1. **Run the installer** (`android-studio-*.exe`)
2. **Follow the wizard:**
   - Click "Next" through setup
   - Choose installation location (default is fine)
   - Select components (all selected by default)
   - Click "Install"
   - Wait for installation (~5-10 minutes)

3. **Launch Android Studio** when installation completes

## ⚙️ Step 3: First-Time Setup

1. **Welcome Screen:**
   - Click "Next"
   - Choose "Standard" installation
   - Click "Next"

2. **SDK Components:**
   - Android Studio will download:
     - Android SDK
     - Android SDK Platform
     - Android Virtual Device
   - Click "Next" and wait (~10-15 minutes)

3. **License Agreement:**
   - Accept all licenses
   - Click "Finish"

4. **Wait for Setup:**
   - Android Studio downloads components
   - This may take 10-20 minutes
   - Grab a coffee ☕

## ✅ Step 4: Verify Installation

1. **Open Android Studio**
2. **File → Settings** (or Preferences)
3. **Appearance & Behavior → System Settings → Android SDK**
4. **Check:**
   - Android SDK Location is set
   - Android SDK Platform 33 is installed
   - Android SDK Build-Tools is installed

## 🚀 Step 5: Run Your App

1. **File → Open**
2. **Navigate to:** `Project - Shiv\mobile-app\android`
3. **Click OK**
4. **Wait for Gradle sync** (first time: 5-10 minutes)
5. **Connect your Android device** (USB debugging enabled)
6. **Click Run button** (green ▶️)
7. **Select your device**
8. **Click OK**

**Done!** App will build and install on your device! 🎉

## 🔍 Verify Device Connection

After Android Studio is installed:

1. **Connect your Android device**
2. **In Android Studio:** Tools → Device Manager
3. **Your device should appear**
4. **If not:** Enable USB debugging on device

## 📱 Enable USB Debugging on Device

1. **Settings → About Phone**
2. **Tap "Build Number" 7 times**
3. **Go back → Developer Options**
4. **Enable "USB Debugging"**
5. **Connect via USB**
6. **Click "Allow" when prompted**

## ⏱️ Total Time

- **Download:** 5-10 minutes
- **Install:** 5-10 minutes
- **First Setup:** 10-20 minutes
- **Total:** ~30-40 minutes

## ✅ After Installation

You'll have:
- ✅ Android Studio IDE
- ✅ Android SDK (automatically configured)
- ✅ ADB in PATH (via Android Studio)
- ✅ Build tools
- ✅ Everything needed!

## 🎯 Next Steps

Once installed:
1. Open `mobile-app/android` in Android Studio
2. Click Run
3. Test your app!

---

**Android Studio is the industry standard for React Native Android development. It's worth the installation time!** 💪
