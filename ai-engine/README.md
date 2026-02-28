# ZoneZap AI Engine

Machine Learning pipeline for anomaly detection and wandering behavior prediction.

## Features

- **Isolation Forest** algorithm for anomaly detection
- Real-time location pattern analysis
- Wandering behavior detection
- Train on **Firebase Firestore** or **Supabase** (app) location data

## How the model fits the app

- **Live app (Vercel + Supabase):** When the mobile app sends location via `POST /api/movement-logs`, the API uses **rule-based** anomaly detection in `vercel-api/lib/anomaly.js` (speed and movement variance on the last 30 points). No Python model is used in production.
- **This AI engine:** Trains an Isolation Forest on location data. You can train on the **same data** the app sends by exporting from Supabase (see below) or from Firebase. The trained `model.pkl` can be used for offline analysis or future server-side integration.

## Setup

1. Install dependencies:
```bash
pip install -r requirements.txt
```

2. Prepare training data:
   - Create a CSV with columns: `latitude`, `longitude`, `timestamp`, `speed`, `heading`
   - Or export from Supabase (recommended if you use the Vercel backend), or use Firebase/sample data

## Training

### Option 1: Train on app location data (Supabase / Vercel backend) 🎯

If your app uses the **Vercel + Supabase** backend, export movement logs from Supabase then train:

```bash
# 1. Export from Supabase (from project root; set SUPABASE_URL and SUPABASE_SERVICE_ROLE_KEY)
node vercel-api/scripts/export-movement-logs-to-csv.js

# 2. Train on the exported CSV
cd ai-engine
python train.py --csv ../vercel-api/scripts/movement_export.csv
```

This uses the **same location data** the app sends to the API. Collect some movement data with the app first, then run the export and train.

### Option 2: Train on Firebase Firestore data

Train the model using location data from Firebase Firestore:

```bash
python train.py --firebase-cred path/to/serviceAccountKey.json
python train.py --firebase-cred serviceAccountKey.json --user-id USER_ID
```

**Getting Firebase Service Account Key:** Firebase Console → Project Settings → Service Accounts → Generate New Private Key.

### Option 3: Train on any CSV file

```bash
python train.py --csv movement.csv
```

CSV format: `latitude,longitude,timestamp,speed,heading`

### Option 4: Use sample data (for testing)

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

- **Production (Vercel):** Anomaly detection is done in `vercel-api/lib/anomaly.js` using the last 30 movement logs (speed and variance). No Python model is called there.
- **Optional:** The AI engine can be used for batch analysis, or the trained `model.pkl` can be integrated later (e.g. reimplement scoring in Node or call a Python service).

## Files

- `train.py`: Model training script
- `predict.py`: Real-time prediction script
- `requirements.txt`: Python dependencies
- `model.pkl`: Trained model (generated after training)
- `training_report.json`: Training statistics (generated after training)

