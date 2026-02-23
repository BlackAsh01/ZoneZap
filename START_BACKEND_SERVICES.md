# Starting Backend Services

This guide explains how to start the backend components for ZoneZap.

## 🚀 Quick Start

### Option 1: Use PowerShell Scripts (Windows)

**Start Firebase Backend:**
```powershell
cd backend
.\start-backend.ps1
```

**Start AI Engine:**
```powershell
cd ai-engine
.\start-ai-engine.ps1
```

### Option 2: Manual Commands

## 📦 Firebase Cloud Functions

### Prerequisites
- Node.js 18+ installed
- Firebase CLI installed: `npm install -g firebase-tools`
- Logged in to Firebase: `firebase login`

### Start Locally (Emulators)

1. **Install dependencies:**
```bash
cd backend/functions
npm install
cd ..
```

2. **Start Firebase emulators:**
```bash
firebase emulators:start
```

This will start:
- **Firestore Emulator**: http://localhost:8080
- **Functions Emulator**: http://localhost:5001
- **Emulator UI**: http://localhost:4000

### Deploy to Production

```bash
cd backend
firebase deploy --only functions
```

### Available Cloud Functions

1. **`onEmergencyAlert`** - Triggered when emergency alert is created
   - Sends FCM notifications to guardians
   - Logs alert to `alert_logs` collection

2. **`analyzeLocationPatterns`** - Triggered on new movement log
   - Analyzes location patterns for anomalies
   - Creates wandering alerts if anomalies detected

3. **`checkOverdueReminders`** - Scheduled (every 5 minutes)
   - Checks for overdue reminders
   - Sends notification to users

## 🤖 AI Engine

### Prerequisites
- Python 3.8+ installed
- Virtual environment (recommended)

### Setup

1. **Navigate to AI engine directory:**
```bash
cd ai-engine
```

2. **Create virtual environment:**
```bash
python -m venv .venv
```

3. **Activate virtual environment:**
   - Windows: `.venv\Scripts\activate`
   - Linux/Mac: `source .venv/bin/activate`

4. **Install dependencies:**
```bash
pip install -r requirements.txt
```

### Training Options

#### Option 1: Train on Firebase Data (Recommended)

**Prerequisites:**
- Firebase service account key (`firebase-service-account.json`)
- Get it from: Firebase Console → Project Settings → Service Accounts

**Train:**
```bash
python train_with_firebase.py
```

Or with options:
```bash
python train.py --firebase-cred firebase-service-account.json --limit 10000 --contamination 0.05
```

#### Option 2: Train on CSV File

```bash
python train.py --csv movement.csv
```

#### Option 3: Use Firebase Emulator (Local Testing)

```bash
# Set emulator host
export FIRESTORE_EMULATOR_HOST=localhost:8080  # Linux/Mac
$env:FIRESTORE_EMULATOR_HOST="localhost:8080"  # Windows PowerShell

# Train
python train.py
```

### Run Predictions

After training (model.pkl will be created):

```bash
python predict.py
```

### Training Output

- `model.pkl` - Trained model file
- `training_report.json` - Training statistics
- `movement_firebase_backup.csv` - Backup of training data

## 🔄 Running Both Services Together

### Terminal 1: Firebase Emulators
```bash
cd backend
firebase emulators:start
```

### Terminal 2: AI Engine Training
```bash
cd ai-engine
python -m venv .venv
.venv\Scripts\activate  # Windows
pip install -r requirements.txt
python train_with_firebase.py
```

## 📊 Monitoring

### Firebase Emulator UI
- Open browser: http://localhost:4000
- View Firestore data, functions logs, and emulator status

### Check Running Services
```bash
# Windows PowerShell
netstat -an | findstr "4000 8080 5001"

# Linux/Mac
lsof -i :4000 -i :8080 -i :5001
```

## 🐛 Troubleshooting

### Firebase Emulators Won't Start
- Check if ports 4000, 8080, 5001 are available
- Make sure Firebase CLI is installed: `firebase --version`
- Login to Firebase: `firebase login`

### AI Engine Training Fails
- Check Python version: `python --version` (need 3.8+)
- Verify dependencies: `pip list | grep sklearn`
- Check Firebase credentials file exists
- For emulator: Make sure Firebase emulators are running first

### Functions Not Triggering
- Check Firestore rules allow writes
- Verify emulators are running
- Check function logs in Emulator UI

## 📝 Next Steps

1. **Start Firebase emulators** - Provides backend services
2. **Train AI model** - Uses real data from Firebase
3. **Test mobile app** - App connects to emulators automatically
4. **Deploy to production** - When ready: `firebase deploy`

## 🔗 Related Files

- `backend/functions/index.js` - Cloud Functions code
- `backend/firestore.rules` - Firestore security rules
- `ai-engine/train.py` - AI training script
- `ai-engine/predict.py` - Prediction script
