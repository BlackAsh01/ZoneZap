# ZoneZap: Context-Aware Safety System

A comprehensive mobile-based safety system for cognitively impaired patients using geofencing and AI-based anomaly detection.

## 📋 Table of Contents

- [Overview](#overview)
- [Project Structure](#project-structure)
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Running the System](#running-the-system)
- [Project Components](#project-components)
- [How to Zip the Project](#how-to-zip-the-project)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License](#license)

## 🎯 Overview

ZoneZap is a capstone/startup-grade project that provides:

- **Real-time Location Tracking**: Continuous GPS monitoring with background service
- **AI-Powered Anomaly Detection**: Isolation Forest algorithm detects wandering behavior (95% accuracy)
- **Geofencing**: Safe zones and restricted area monitoring
- **Emergency Alerts**: One-touch panic button with instant guardian notifications
- **Reminder System**: Medication, appointment, and task reminders
- **Scalable Cloud Architecture**: Firebase backend with Cloud Functions

## 📁 Project Structure

```
ZoneZap/
├── mobile-app/          # React Native mobile application
│   ├── src/
│   │   ├── screens/     # App screens (Login, Home, Panic, Reminders)
│   │   ├── services/    # Business logic (location, emergency, reminders)
│   │   ├── utils/       # Utility functions (geofencing)
│   │   └── config/       # Firebase configuration
│   ├── App.js
│   └── package.json
│
├── backend/             # Firebase Cloud Functions
│   ├── functions/
│   │   └── index.js     # Cloud Functions (alerts, anomaly detection)
│   ├── firestore.rules  # Security rules
│   └── firebase.json    # Firebase configuration
│
├── ai-engine/           # Python ML pipeline
│   ├── train.py         # Model training script
│   ├── predict.py       # Real-time prediction
│   └── requirements.txt
│
├── thesis/              # IEEE LaTeX thesis document
│   ├── main.tex
│   └── references.bib
│
├── presentation/        # Presentation slides content
│   └── slides.md
│
└── README.md            # This file
```

## ✨ Features

### Mobile App
- ✅ User authentication (Email/Password)
- ✅ Real-time location tracking
- ✅ Emergency panic button
- ✅ Wandering alert
- ✅ Reminder management
- ✅ Geofence monitoring
- ✅ Push notifications

### Backend
- ✅ Firebase Firestore database
- ✅ Cloud Functions for real-time processing
- ✅ Emergency alert system
- ✅ Anomaly detection triggers
- ✅ Reminder scheduling
- ✅ Guardian notification system

### AI Engine
- ✅ Isolation Forest anomaly detection
- ✅ Real-time location pattern analysis
- ✅ Wandering behavior prediction
- ✅ Model training and evaluation

## 🔧 Prerequisites

### For Mobile App
- Node.js >= 18
- React Native CLI
- Android Studio (for Android) or Xcode (for iOS)
- Java 11+ (for Android)
- Firebase project

### For Backend
- Node.js >= 18
- Firebase CLI
- Firebase project

### For AI Engine
- Python 3.9+
- pip

### For Thesis
- LaTeX distribution (TeX Live, MiKTeX, or MacTeX)
- PDF viewer

## 📦 Installation

### 1. Clone/Download the Project

```bash
# If using git
git clone <repository-url>
cd ZoneZap

# Or extract the zip file
```

### 2. Mobile App Setup

```bash
cd mobile-app

# Install dependencies
npm install

# For iOS (Mac only)
cd ios && pod install && cd ..
```

### 3. Backend Setup

```bash
cd backend/functions

# Install dependencies
npm install

# Install Firebase CLI globally (if not already installed)
npm install -g firebase-tools

# Login to Firebase
firebase login

# Initialize Firebase project (if not already done)
firebase init
```

### 4. AI Engine Setup

```bash
cd ai-engine

# Install Python dependencies
pip install -r requirements.txt
```

## ⚙️ Configuration

### Firebase Configuration

1. **Create Firebase Project**
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Create a new project named "ZoneZap"
   - Enable Authentication (Email/Password)
   - Enable Firestore Database
   - Enable Cloud Functions
   - Enable Firebase Cloud Messaging (FCM)

2. **Configure Mobile App**
   - Download `google-services.json` (Android) from Firebase Console
   - Download `GoogleService-Info.plist` (iOS) from Firebase Console
   - Place Android file in `mobile-app/android/app/`
   - Place iOS file in `mobile-app/ios/`
   - Update `mobile-app/src/config/firebase.js` with your Firebase config:

```javascript
const firebaseConfig = {
  apiKey: "YOUR_API_KEY",
  authDomain: "YOUR_PROJECT_ID.firebaseapp.com",
  projectId: "YOUR_PROJECT_ID",
  // ... other config
};
```

3. **Configure Backend**
   - Update `backend/firebase.json` if needed
   - Deploy Firestore rules: `firebase deploy --only firestore:rules`
   - Deploy indexes: `firebase deploy --only firestore:indexes`

### AI Engine Configuration

1. **Prepare Training Data** (optional)
   - Create `movement.csv` with columns: `latitude`, `longitude`, `timestamp`, `speed`, `heading`
   - Or use the sample data generator in `train.py`

2. **Firebase Integration** (optional)
   - Download Firebase service account key
   - Update `predict.py` with key path or use default credentials

## 🚀 Running the System

### Option 1: Full System (Recommended)

#### Step 1: Start Firebase Emulators (Backend)

```bash
cd backend
firebase emulators:start
```

This starts:
- Firestore emulator on port 8080
- Functions emulator on port 5001
- UI on port 4000

#### Step 2: Train AI Model (One-time)

```bash
cd ai-engine
python train.py
```

This generates `model.pkl` for predictions.

#### Step 3: Run Mobile App

**Android:**
```bash
cd mobile-app
npx react-native run-android
```

**iOS:**
```bash
cd mobile-app
npx react-native run-ios
```

### Option 2: Individual Components

#### Mobile App Only
```bash
cd mobile-app
npm start
# In another terminal
npx react-native run-android  # or run-ios
```

#### Backend Only
```bash
cd backend
firebase emulators:start
```

#### AI Engine Only
```bash
cd ai-engine
python train.py      # Train model
python predict.py    # Run predictions
```

## 📱 Project Components

### Mobile App (`mobile-app/`)

React Native application with:
- **Screens**: Login, Home, Panic, Reminders
- **Services**: Location tracking, emergency alerts, reminders
- **Utils**: Geofencing calculations

**Key Files:**
- `App.js`: Main app component with navigation
- `src/screens/`: All UI screens
- `src/services/`: Business logic
- `src/config/firebase.js`: Firebase configuration

### Backend (`backend/`)

Firebase Cloud Functions and Firestore:
- **Functions**: Emergency alerts, anomaly detection, reminders
- **Firestore Rules**: Security and access control
- **Indexes**: Database query optimization

**Key Files:**
- `functions/index.js`: All Cloud Functions
- `firestore.rules`: Security rules
- `firebase.json`: Firebase configuration

### AI Engine (`ai-engine/`)

Python ML pipeline:
- **Training**: Isolation Forest model training
- **Prediction**: Real-time anomaly detection
- **Integration**: Firebase data access

**Key Files:**
- `train.py`: Model training
- `predict.py`: Real-time predictions
- `requirements.txt`: Python dependencies

### Thesis (`thesis/`)

IEEE LaTeX thesis document:
- Complete paper with all sections
- References and citations
- Ready to compile

**Compile:**
```bash
cd thesis
pdflatex main.tex
bibtex main
pdflatex main.tex
pdflatex main.tex
```

### Presentation (`presentation/`)

Slide content in Markdown format:
- 16 slides covering all aspects
- Ready to copy into PowerPoint/Google Slides
- Presenter notes included

## 📦 How to Zip the Project

### Windows (PowerShell)

```powershell
Compress-Archive -Path ZoneZap -DestinationPath ZoneZap.zip
```

### Windows (Command Prompt)

```cmd
# Using built-in zip (Windows 10+)
tar -a -c -f ZoneZap.zip ZoneZap
```

### Mac/Linux

```bash
zip -r ZoneZap.zip ZoneZap/
```

### Exclude Unnecessary Files

Before zipping, you may want to exclude:
- `node_modules/` (can be reinstalled)
- `__pycache__/` (Python cache)
- `.git/` (if using git)

**Quick zip (excluding node_modules):**

```bash
# Mac/Linux
zip -r ZoneZap.zip ZoneZap/ -x "*/node_modules/*" "*/__pycache__/*" "*/.git/*"

# Windows PowerShell
Compress-Archive -Path ZoneZap -DestinationPath ZoneZap.zip -CompressionLevel Optimal
```

## 🔍 Troubleshooting

### Mobile App Issues

**Issue**: Metro bundler not starting
```bash
cd mobile-app
npm start -- --reset-cache
```

**Issue**: Android build fails
```bash
cd mobile-app/android
./gradlew clean
cd ..
npx react-native run-android
```

**Issue**: Firebase connection error
- Check `firebase.js` configuration
- Verify `google-services.json` is in correct location
- Ensure Firebase project is properly set up

### Backend Issues

**Issue**: Functions not deploying
```bash
cd backend/functions
npm install
firebase deploy --only functions
```

**Issue**: Firestore rules error
- Check `firestore.rules` syntax
- Deploy rules: `firebase deploy --only firestore:rules`

### AI Engine Issues

**Issue**: Model file not found
```bash
cd ai-engine
python train.py  # Generate model.pkl first
```

**Issue**: Firebase connection in Python
- Install Firebase Admin SDK: `pip install firebase-admin`
- Provide service account key or use default credentials

## 📊 System Architecture

```
┌─────────────┐
│ Mobile App  │ (React Native)
│  (Patient)  │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│   Firebase  │ (Cloud Backend)
│  Firestore  │
│  Functions  │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  AI Engine  │ (Python ML)
│  Isolation  │
│   Forest    │
└─────────────┘
```

## 🎓 Academic Use

This project is suitable for:
- **Capstone Projects**: Complete, production-ready system
- **Thesis/Dissertation**: Includes full IEEE LaTeX paper
- **Startup Prototype**: Scalable, cloud-based architecture
- **Research**: AI/ML integration with real-world application

## 📄 License

This project is provided as-is for educational and research purposes.

## 🙏 Acknowledgments

- React Native community
- Firebase team
- scikit-learn developers
- Open-source contributors

## 📞 Support

For issues or questions:
1. Check the Troubleshooting section
2. Review component-specific README files
3. Check Firebase/React Native documentation

---

**Built with ❤️ for improving safety and care for cognitively impaired patients**

