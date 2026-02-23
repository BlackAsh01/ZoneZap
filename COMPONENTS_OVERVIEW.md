# ZoneZap Components Overview - How Everything Works Together

## 🏗️ System Architecture

```
┌─────────────────┐
│  Mobile App     │  (Native Android/Kotlin)
│  - Login        │
│  - Location     │
│  - Panic Button │
│  - Reminders    │
└────────┬────────┘
         │
         │ Firebase SDK
         │
┌────────▼─────────────────────────────────────┐
│         Firebase Backend                      │
│  ┌──────────────────────────────────────┐   │
│  │  Firestore Database                  │   │
│  │  - users, alerts, reminders, logs    │   │
│  └──────────────────────────────────────┘   │
│  ┌──────────────────────────────────────┐   │
│  │  Cloud Functions (Backend)          │   │
│  │  - Emergency alerts                  │   │
│  │  - Anomaly detection                │   │
│  │  - Reminder notifications           │   │
│  └──────────────────────────────────────┘   │
│  ┌──────────────────────────────────────┐   │
│  │  Firebase Cloud Messaging (FCM)     │   │
│  │  - Push notifications               │   │
│  └──────────────────────────────────────┘   │
└────────┬─────────────────────────────────────┘
         │
         │ (Optional Integration)
         │
┌────────▼────────┐
│   AI Engine     │  (Python ML)
│   - Isolation   │
│     Forest      │
│   - Anomaly     │
│     Detection   │
└─────────────────┘
```

---

## 📦 Component Breakdown

### 1. **Mobile App** (`mobile-app-native/`)

**Technology:** Native Android (Kotlin)

**What It Does:**
- ✅ User authentication (email/password)
- ✅ Real-time location tracking
- ✅ Emergency panic button
- ✅ Wandering alerts
- ✅ Reminder management
- ✅ UI/UX with Material Design

**How to Run:**
```bash
# Open in Android Studio
# File → Open → mobile-app-native
# Click Run button (▶️)
```

**Key Files:**
- `app/src/main/java/com/zonezapapp/ui/` - Activities (screens)
- `app/src/main/java/com/zonezapapp/services/` - Business logic
- `app/src/main/java/com/zonezapapp/config/FirebaseConfig.kt` - Firebase setup

**Status:** ✅ Ready to run (Native Android app)

---

### 2. **Backend** (`backend/`)

**Technology:** Firebase Cloud Functions (Node.js)

**What It Does:**

#### a) **Emergency Alert Handler** (`onEmergencyAlert`)
- **Trigger:** When a new alert is created in Firestore
- **Action:**
  1. Gets user's guardian list
  2. Sends push notifications to all guardians
  3. Logs the alert for analytics
- **Result:** Guardians receive instant notifications

#### b) **Anomaly Detection** (`analyzeLocationPatterns`)
- **Trigger:** When location log is added to Firestore
- **Action:**
  1. Analyzes last 30 location points
  2. Detects unusual patterns (high speed, erratic movement)
  3. Creates "WANDERING" alert if anomalies found
- **Result:** Automatic wandering detection

#### c) **Reminder Checker** (`checkOverdueReminders`)
- **Trigger:** Runs every 5 minutes (scheduled)
- **Action:**
  1. Finds overdue reminders
  2. Sends push notifications to users
- **Result:** Users get reminder notifications

**How to Run:**
```bash
cd backend

# Install dependencies
cd functions
npm install
cd ..

# Start Firebase emulators (for local testing)
firebase emulators:start

# Deploy to production
firebase deploy --only functions
```

**Key Files:**
- `functions/index.js` - All Cloud Functions
- `firestore.rules` - Security rules
- `firestore.indexes.json` - Database indexes

**Status:** ✅ Ready (Functions implemented, can run locally or deploy)

---

### 3. **AI Engine** (`ai-engine/`)

**Technology:** Python (scikit-learn, Isolation Forest)

**What It Does:**
- Trains ML model on location patterns
- Detects anomalies (wandering behavior)
- Can integrate with Firebase for real-time predictions

**How It Works:**
1. **Training** (`train.py`):
   - Loads movement data (CSV or generates sample)
   - Extracts features: distance from home, velocity, heading changes, time
   - Trains Isolation Forest model
   - Saves `model.pkl`

2. **Prediction** (`predict.py`):
   - Loads trained model
   - Analyzes new location data
   - Returns anomaly score (-1 = anomaly, 1 = normal)

**How to Run:**
```bash
cd ai-engine

# Install dependencies
pip install -r requirements.txt

# Train the model
python train.py

# Run predictions (optional)
python predict.py
```

**Key Files:**
- `train.py` - Model training script
- `predict.py` - Prediction script
- `model.pkl` - Trained model (generated)
- `requirements.txt` - Python dependencies

**Status:** ✅ Ready (Model can be trained, optional component)

---

## 🔄 How Components Work Together

### **Scenario 1: User Presses Panic Button**

```
1. Mobile App (PanicActivity.kt)
   └─> Creates alert document in Firestore
       └─> Collection: "alerts"
           └─> Fields: userId, alertType="PANIC", location, timestamp

2. Backend (Cloud Function: onEmergencyAlert)
   └─> Triggered automatically when alert created
       └─> Reads user's guardian list
           └─> Sends FCM push notifications to all guardians
               └─> Guardians receive notification on their devices

3. Result: Guardians immediately notified of emergency
```

### **Scenario 2: Location Tracking & Wandering Detection**

```
1. Mobile App (LocationService.kt)
   └─> Continuously tracks location
       └─> Logs to Firestore every few seconds
           └─> Collection: "movement_logs"
               └─> Fields: userId, latitude, longitude, timestamp

2. Backend (Cloud Function: analyzeLocationPatterns)
   └─> Triggered when new location log added
       └─> Analyzes last 30 location points
           └─> Detects anomalies (high speed, erratic movement)
               └─> Creates "WANDERING" alert if detected
                   └─> Alert triggers guardian notifications

3. Result: Automatic wandering detection and alert
```

### **Scenario 3: Reminder System**

```
1. Mobile App (RemindersActivity.kt)
   └─> User creates reminder
       └─> Saves to Firestore
           └─> Collection: "reminders"
               └─> Fields: userId, title, description, scheduledTime

2. Backend (Cloud Function: checkOverdueReminders)
   └─> Runs every 5 minutes (scheduled)
       └─> Finds reminders where scheduledTime <= now
           └─> Sends FCM push notification to user
               └─> User receives reminder notification

3. Result: Automatic reminder notifications
```

---

## 🚀 How to Run the Complete System

### **Option 1: Full System (Recommended for Testing)**

**Terminal 1 - Backend:**
```bash
cd backend
firebase emulators:start
```
- Firestore: http://localhost:8080
- UI Dashboard: http://localhost:4000

**Terminal 2 - Mobile App:**
```bash
# Open Android Studio
# File → Open → mobile-app-native
# Click Run button
```

**Terminal 3 - AI Engine (Optional):**
```bash
cd ai-engine
python train.py  # Train model once
python predict.py  # Run predictions (optional)
```

### **Option 2: Mobile App Only (Works Standalone)**

The mobile app can work with **production Firebase** (not emulators):
- Just run the app in Android Studio
- It connects to your Firebase project automatically
- All backend functions run in Firebase cloud

### **Option 3: Local Development**

For local testing with emulators:
1. Start Firebase emulators
2. Update Firebase config in app to point to emulators
3. Run mobile app

---

## ✅ Component Status

| Component | Status | How to Run |
|-----------|--------|------------|
| **Mobile App** | ✅ Ready | Android Studio → Run |
| **Backend Functions** | ✅ Ready | `firebase emulators:start` |
| **AI Engine** | ✅ Ready | `python train.py` |
| **Firebase Config** | ✅ Configured | Already set up |
| **Database Rules** | ✅ Ready | Deployed/configured |

---

## 🧪 Testing Each Component

### **Test Mobile App:**
1. Run app in Android Studio
2. Create account → Login
3. Grant location permission
4. Test panic button → Check Firebase Console
5. Add reminder → Check it appears

### **Test Backend:**
1. Start emulators: `firebase emulators:start`
2. Open dashboard: http://localhost:4000
3. Create alert in Firestore → Watch function trigger
4. Check logs for function execution

### **Test AI Engine:**
1. Train model: `python train.py`
2. Check `model.pkl` created
3. Run predictions: `python predict.py`
4. Review training report

---

## 📊 Data Flow Summary

```
Mobile App
    │
    ├─> Login → Firebase Auth → Creates user session
    │
    ├─> Location → Firestore (movement_logs) → Triggers anomaly detection
    │
    ├─> Panic Button → Firestore (alerts) → Triggers emergency notifications
    │
    └─> Reminders → Firestore (reminders) → Scheduled function sends notifications

Backend Functions
    │
    ├─> onEmergencyAlert → Sends FCM notifications
    │
    ├─> analyzeLocationPatterns → Creates wandering alerts
    │
    └─> checkOverdueReminders → Sends reminder notifications

AI Engine (Optional)
    │
    └─> Can analyze Firestore data → More advanced anomaly detection
```

---

## 🎯 Quick Start Commands

```bash
# 1. Start Backend (Terminal 1)
cd backend
firebase emulators:start

# 2. Run Mobile App (Android Studio)
# File → Open → mobile-app-native → Run

# 3. Train AI Model (Optional, Terminal 2)
cd ai-engine
pip install -r requirements.txt
python train.py
```

---

## 💡 Key Points

1. **Mobile App** is the main interface - users interact here
2. **Backend Functions** run automatically - no manual intervention needed
3. **AI Engine** is optional - enhances anomaly detection
4. **Firebase** connects everything - database, auth, notifications
5. **All components work independently** - can test separately

---

**Everything is ready to run!** 🎉

The mobile app works standalone, and backend functions run automatically when data is created in Firestore.
