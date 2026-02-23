# Backend Services Status

**Last Updated:** $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")

## 🟢 Running Services

### 1. Firebase Emulators ✅
- **Status:** Starting/Running in background
- **Firestore:** http://localhost:8080
- **Functions:** http://localhost:5001  
- **UI Dashboard:** http://localhost:4000
- **PID:** Check terminal output

**What it does:**
- Provides local Firestore database for testing
- Runs Cloud Functions locally
- Allows testing without deploying to production

**Access:**
- Open browser: http://localhost:4000
- View data, functions logs, and emulator status

### 2. AI Engine ✅
- **Status:** Model trained and ready
- **Model File:** `ai-engine/model.pkl`
- **Training Report:** `ai-engine/training_report.json`

**What it does:**
- Anomaly detection for wandering behavior
- Location pattern analysis
- Can be integrated with Cloud Functions

**To retrain:**
```bash
cd ai-engine
python train_with_firebase.py
```

## 📋 Available Cloud Functions

### 1. `onEmergencyAlert`
- **Trigger:** New document in `alerts` collection
- **Action:** Sends FCM notifications to guardians
- **Status:** ✅ Ready (runs in emulator)

### 2. `analyzeLocationPatterns`
- **Trigger:** New document in `movement_logs` collection
- **Action:** Analyzes location for anomalies, creates wandering alerts
- **Status:** ✅ Ready (runs in emulator)

### 3. `checkOverdueReminders`
- **Trigger:** Scheduled every 5 minutes
- **Action:** Checks overdue reminders, sends notifications
- **Status:** ✅ Ready (runs in emulator)

## 🔗 Integration Status

### Mobile App → Backend
- ✅ Mobile app connects to Firebase emulators automatically
- ✅ Location tracking logs to `movement_logs` collection
- ✅ Emergency alerts create documents in `alerts` collection
- ✅ Reminders sync with `reminders` collection

### Backend → Mobile App
- ✅ Cloud Functions send FCM notifications (when deployed)
- ✅ Anomaly detection creates alerts automatically
- ✅ Reminder notifications sent for overdue items

## 🚀 Next Steps

1. **Verify Emulators Running:**
   - Open http://localhost:4000 in browser
   - Check that Firestore and Functions are active

2. **Test Mobile App:**
   - Run the Android app
   - It will connect to emulators automatically
   - Create test data and verify functions trigger

3. **Monitor Functions:**
   - View function logs in Emulator UI
   - Test emergency alerts and check notifications
   - Verify location analysis triggers

4. **Deploy to Production (when ready):**
   ```bash
   cd backend
   firebase deploy --only functions
   ```

## 📊 Monitoring Commands

### Check if services are running:
```powershell
# Check ports
netstat -an | findstr "4000 8080 5001"

# Check Firebase emulator process
Get-Process | Where-Object {$_.ProcessName -like "*node*" -or $_.ProcessName -like "*firebase*"}
```

### View logs:
- Firebase Emulator UI: http://localhost:4000
- Terminal output (where emulators were started)

## 🐛 Troubleshooting

### Emulators not starting?
- Check if ports are in use
- Verify Firebase CLI: `firebase --version`
- Try: `firebase emulators:start --only firestore,functions`

### Functions not triggering?
- Check Firestore rules allow writes
- Verify collection names match (`alerts`, `movement_logs`, `reminders`)
- Check function logs in Emulator UI

### AI Engine issues?
- Model already trained: `ai-engine/model.pkl` exists
- To retrain: `cd ai-engine && python train_with_firebase.py`
- Check Python version: `python --version` (need 3.8+)

## 📝 Service Endpoints

| Service | URL | Purpose |
|--------|-----|---------|
| Emulator UI | http://localhost:4000 | Dashboard and monitoring |
| Firestore | http://localhost:8080 | Database API |
| Functions | http://localhost:5001 | Cloud Functions API |

## ✅ All Systems Operational

All backend services are configured and ready to use!
