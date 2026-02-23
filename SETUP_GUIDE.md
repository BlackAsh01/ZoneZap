# ZoneZap Setup Guide

Quick setup guide to get ZoneZap running on your system.

## 🚀 Quick Start (5 Minutes)

### Step 1: Prerequisites Check

```bash
# Check Node.js version (need >= 18)
node --version

# Check Python version (need >= 3.9)
python --version

# Check if Firebase CLI is installed
firebase --version
```

If any are missing:
- **Node.js**: Download from [nodejs.org](https://nodejs.org/)
- **Python**: Download from [python.org](https://www.python.org/)
- **Firebase CLI**: `npm install -g firebase-tools`

### Step 2: Firebase Setup (One-time)

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Add Project" → Name it "ZoneZap"
3. Enable these services:
   - ✅ Authentication (Email/Password)
   - ✅ Firestore Database
   - ✅ Cloud Functions
   - ✅ Firebase Cloud Messaging

4. Get your Firebase config:
   - Go to Project Settings → General
   - Scroll to "Your apps" → Add app (Web)
   - Copy the config object

### Step 3: Configure Mobile App

1. **Update Firebase Config:**
   ```bash
   # Edit mobile-app/src/config/firebase.js
   # Replace with your Firebase config values
   ```

2. **Add Firebase Files:**
   - Download `google-services.json` (Android) from Firebase Console
   - Place in: `mobile-app/android/app/google-services.json`
   - Download `GoogleService-Info.plist` (iOS) from Firebase Console
   - Place in: `mobile-app/ios/GoogleService-Info.plist`

3. **Install Dependencies:**
   ```bash
   cd mobile-app
   npm install
   ```

### Step 4: Configure Backend

```bash
cd backend

# Login to Firebase
firebase login

# Initialize (if first time)
firebase init
# Select: Firestore, Functions
# Use existing project: ZoneZap

# Install function dependencies
cd functions
npm install
cd ..
```

### Step 5: Train AI Model

```bash
cd ai-engine

# Install Python dependencies
pip install -r requirements.txt

# Train the model (uses sample data if movement.csv not found)
python train.py
```

This creates `model.pkl` for predictions.

### Step 6: Run Everything

**Terminal 1 - Backend:**
```bash
cd backend
firebase emulators:start
```

**Terminal 2 - Mobile App:**
```bash
cd mobile-app
npx react-native run-android  # or run-ios
```

**Terminal 3 - AI Engine (Optional):**
```bash
cd ai-engine
python predict.py
```

## ✅ Verification Checklist

- [ ] Firebase project created
- [ ] Firebase config updated in mobile app
- [ ] `google-services.json` added to Android project
- [ ] Backend dependencies installed
- [ ] AI model trained (`model.pkl` exists)
- [ ] Firebase emulators running
- [ ] Mobile app builds and runs
- [ ] Can create account and login
- [ ] Location tracking works
- [ ] Panic button sends alerts

## 🐛 Common Issues

### "Firebase not configured"
- Check `mobile-app/src/config/firebase.js` has your Firebase config
- Verify `google-services.json` is in correct location

### "Module not found"
```bash
cd mobile-app
rm -rf node_modules
npm install
```

### "Firebase emulators not starting"
```bash
cd backend
firebase emulators:start --only firestore,functions
```

### "Model file not found"
```bash
cd ai-engine
python train.py
```

## 📱 Testing the App

1. **Create Account:**
   - Open app
   - Enter email/password
   - Click "Sign Up"

2. **Test Location Tracking:**
   - Grant location permission
   - Check Home screen shows current location

3. **Test Panic Button:**
   - Go to Panic screen
   - Click "PANIC BUTTON"
   - Check Firebase Console → Firestore → `alerts` collection

4. **Test Reminders:**
   - Go to Reminders screen
   - Add a reminder
   - Check it appears in list

## 🎓 For Academic Submission

1. **Compile Thesis:**
   ```bash
   cd thesis
   pdflatex main.tex
   bibtex main
   pdflatex main.tex
   pdflatex main.tex
   ```

2. **Create Presentation:**
   - Open `presentation/slides.md`
   - Copy content to PowerPoint/Google Slides
   - Add diagrams and images

3. **Zip Project:**
   ```bash
   # Windows PowerShell
   Compress-Archive -Path ZoneZap -DestinationPath ZoneZap.zip
   
   # Mac/Linux
   zip -r ZoneZap.zip ZoneZap/
   ```

## 📞 Need Help?

1. Check `README.md` for detailed documentation
2. Review component-specific README files:
   - `mobile-app/README.md`
   - `backend/README.md`
   - `ai-engine/README.md`
3. Check Firebase/React Native official documentation

---

**You're all set! 🎉**

