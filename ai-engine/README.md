# ZoneZap AI Engine

Machine Learning pipeline for anomaly detection and wandering behavior prediction.

## Features

- **Isolation Forest** algorithm for anomaly detection
- Real-time location pattern analysis
- Wandering behavior detection
- Integration with Firebase for real-time predictions

## Setup

1. Install dependencies:
```bash
pip install -r requirements.txt
```

2. Prepare training data:
   - Create a CSV file `movement.csv` with columns: `latitude`, `longitude`, `timestamp`, `speed`, `heading`
   - Or use the sample data generator in `train.py`

## Training

### Option 1: Train on REAL Firebase Data (Recommended) 🎯

Train the model using actual location data from your Firebase Firestore:

```bash
# Method 1: Using helper script (auto-detects credentials)
python train_with_firebase.py

# Method 2: Direct with Firebase credentials
python train.py --firebase-cred path/to/serviceAccountKey.json

# Method 3: Train on specific user's data
python train.py --firebase-cred serviceAccountKey.json --user-id USER_ID

# Method 4: Use Firebase emulator (for local testing)
export FIRESTORE_EMULATOR_HOST=localhost:8080
python train.py
```

**Getting Firebase Service Account Key:**
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project (zonezap-a6953)
3. Go to **Project Settings** → **Service Accounts**
4. Click **Generate New Private Key**
5. Save as `firebase-service-account.json` in `ai-engine/` directory

**What it does:**
- ✅ Fetches real movement data from Firestore `movement_logs` collection
- ✅ Uses actual location patterns from your mobile app users
- ✅ Trains on real-world wandering behavior patterns
- ✅ Saves backup CSV for future reference
- ✅ Generates comprehensive training report

### Option 2: Train on CSV File

If you have location data in CSV format:

```bash
python train.py --csv movement.csv
```

CSV format: `latitude,longitude,timestamp,speed,heading`

### Option 3: Use Sample Data (For Testing)

```bash
python train.py --no-firestore
```

**Training Options:**
- `--limit N`: Maximum records to fetch (default: 5000)
- `--contamination RATE`: Expected anomaly rate 0.01-0.1 (default: 0.05)
- `--user-id ID`: Train on specific user's data only
- `--csv FILE`: Use CSV file instead of Firestore

**Example:**
```bash
# Train on last 10,000 records with 3% expected anomaly rate
python train.py --firebase-cred serviceAccountKey.json --limit 10000 --contamination 0.03
```

**Training Output:**
- `model.pkl` - Trained model (use for predictions)
- `training_report.json` - Detailed statistics and metrics
- `movement_firestore_backup.csv` - Backup of training data

## Prediction

Run real-time predictions:
```bash
python predict.py
```

For Firebase integration:
1. Download Firebase service account key
2. Set path in `predict.py` or use default credentials in Cloud Functions

## Model Details

**Algorithm**: Isolation Forest
- Contamination rate: 5% (configurable)
- Features: Distance from home, velocity, heading change, hour, day of week
- Output: Anomaly score (-1 = anomaly, 1 = normal)

## Integration

The AI engine can be integrated with:
- Firebase Cloud Functions (for real-time processing)
- Standalone Python service
- Batch processing scripts

## Files

- `train.py`: Model training script
- `predict.py`: Real-time prediction script
- `requirements.txt`: Python dependencies
- `model.pkl`: Trained model (generated after training)
- `training_report.json`: Training statistics (generated after training)

