"""
ZoneZap AI Engine - Anomaly Detection Training
Trains an Isolation Forest model to detect wandering behavior and anomalies
Now supports training on REAL data from Firebase Firestore!
"""

import numpy as np
import pandas as pd
from sklearn.ensemble import IsolationForest
from sklearn.preprocessing import StandardScaler
import pickle
import json
from datetime import datetime
import firebase_admin
from firebase_admin import credentials, firestore
import os

def initialize_firebase(cred_path=None):
    """
    Initialize Firebase Admin SDK
    """
    try:
        if not firebase_admin._apps:
            if cred_path and os.path.exists(cred_path):
                cred = credentials.Certificate(cred_path)
                firebase_admin.initialize_app(cred)
                print(f"[OK] Firebase initialized with credentials: {cred_path}")
            else:
                # Try default credentials (for Cloud Functions or local emulator)
                try:
                    firebase_admin.initialize_app()
                    print("[OK] Firebase initialized with default credentials")
                except:
                    # Try with emulator
                    os.environ['FIRESTORE_EMULATOR_HOST'] = 'localhost:8080'
                    firebase_admin.initialize_app()
                    print("[OK] Firebase initialized with emulator (localhost:8080)")
        return firestore.client()
    except Exception as e:
        print(f"[WARNING] Firebase initialization failed: {e}")
        print("  Will use CSV file or sample data instead")
        return None

def fetch_movement_data_from_firestore(db, user_id=None, limit=5000, min_samples=100):
    """
    Fetch real movement data from Firestore
    Returns DataFrame with columns: latitude, longitude, timestamp, speed, heading
    """
    if not db:
        return None
    
    try:
        print(f"\nFetching movement data from Firestore...")
        print(f"  User ID: {user_id if user_id else 'ALL USERS'}")
        print(f"  Limit: {limit} records")
        
        logs_ref = db.collection('movement_logs')
        
        # Filter by user if specified
        if user_id:
            query = logs_ref.where('userId', '==', user_id)
        else:
            query = logs_ref
        
        # Order by timestamp (most recent first)
        query = query.order_by('timestamp', direction=firestore.Query.DESCENDING).limit(limit)
        
        logs = query.stream()
        
        records = []
        for log in logs:
            data = log.to_dict()
            
            # Extract data with proper type conversion
            record = {
                'latitude': float(data.get('latitude', 0)),
                'longitude': float(data.get('longitude', 0)),
                'speed': float(data.get('speed', 0)),  # m/s
                'heading': float(data.get('heading', 0)),  # degrees (bearing)
                'timestamp': data.get('timestamp')
            }
            
            # Convert Firestore Timestamp to datetime
            if record['timestamp']:
                if hasattr(record['timestamp'], 'to_datetime'):
                    record['timestamp'] = record['timestamp'].to_datetime()
                elif hasattr(record['timestamp'], 'seconds'):
                    # Firestore Timestamp object
                    record['timestamp'] = datetime.fromtimestamp(record['timestamp'].seconds)
                else:
                    record['timestamp'] = datetime.now()
            else:
                record['timestamp'] = datetime.now()
            
            records.append(record)
        
        if len(records) < min_samples:
            print(f"[WARNING] Only {len(records)} records found (minimum recommended: {min_samples})")
            print(f"  Consider collecting more location data or reducing min_samples")
        
        df = pd.DataFrame(records)
        
        # Sort by timestamp (oldest first for proper feature extraction)
        df = df.sort_values('timestamp').reset_index(drop=True)
        
        print(f"[OK] Successfully fetched {len(df)} movement records from Firestore")
        print(f"  Date range: {df['timestamp'].min()} to {df['timestamp'].max()}")
        print(f"  Location range: Lat [{df['latitude'].min():.4f}, {df['latitude'].max():.4f}]")
        print(f"                  Lng [{df['longitude'].min():.4f}, {df['longitude'].max():.4f}]")
        
        return df
        
    except Exception as e:
        print(f"✗ Error fetching from Firestore: {e}")
        return None

def load_movement_data(csv_path='movement.csv', db=None, user_id=None, use_firestore=True, limit=5000):
    """
    Load movement data from Firestore (preferred) or CSV file
    Expected columns: latitude, longitude, timestamp, speed, heading
    """
    # Try Firestore first if enabled
    if use_firestore and db:
        df = fetch_movement_data_from_firestore(db, user_id=user_id, limit=limit)
        if df is not None and len(df) > 0:
            # Save to CSV as backup
            df.to_csv('movement_firestore_backup.csv', index=False)
            print(f"✓ Backup saved to movement_firestore_backup.csv")
            return df
    
    # Fallback to CSV file
    try:
        df = pd.read_csv(csv_path)
        print(f"[OK] Loaded {len(df)} movement records from CSV: {csv_path}")
        return df
    except FileNotFoundError:
        print(f"[WARNING] {csv_path} not found and Firestore unavailable.")
        print(f"Using sample data for demonstration.")
        return generate_sample_data()

def generate_sample_data(n_samples=1000):
    """
    Generate sample movement data for demonstration
    """
    np.random.seed(42)
    
    # Normal movement patterns (around a home location)
    home_lat, home_lng = 12.9716, 77.5946  # Example: Bangalore
    
    # Normal walking patterns
    normal_lat = home_lat + np.random.normal(0, 0.01, n_samples // 2)
    normal_lng = home_lng + np.random.normal(0, 0.01, n_samples // 2)
    normal_speed = np.random.normal(1.4, 0.3, n_samples // 2)  # ~5 km/h walking speed
    
    # Anomalous patterns (wandering, rapid movement)
    anomaly_lat = home_lat + np.random.normal(0, 0.1, n_samples // 2)
    anomaly_lng = home_lng + np.random.normal(0, 0.1, n_samples // 2)
    anomaly_speed = np.random.normal(5.0, 1.5, n_samples // 2)  # ~18 km/h (unusual)
    
    df = pd.DataFrame({
        'latitude': np.concatenate([normal_lat, anomaly_lat]),
        'longitude': np.concatenate([normal_lng, anomaly_lng]),
        'speed': np.concatenate([normal_speed, anomaly_speed]),
        'heading': np.random.uniform(0, 360, n_samples),
        'timestamp': pd.date_range(start='2024-01-01', periods=n_samples, freq='5min')
    })
    
    return df

def extract_features(df):
    """
    Extract features for anomaly detection
    """
    features = []
    
    # Calculate distance from home (median location as proxy for home)
    home_lat = df['latitude'].median()
    home_lng = df['longitude'].median()
    
    df['distance_from_home'] = np.sqrt(
        (df['latitude'] - home_lat) ** 2 + 
        (df['longitude'] - home_lng) ** 2
    ) * 111000  # Convert to meters (approximate)
    
    # Calculate movement velocity
    df['velocity'] = df['speed']
    
    # Calculate rate of change in direction (erratic movement indicator)
    df['heading_change'] = df['heading'].diff().abs()
    df['heading_change'] = df['heading_change'].fillna(0)
    df['heading_change'] = df['heading_change'].apply(lambda x: min(x, 360 - x))
    
    # Time-based features
    # Ensure timestamp is datetime type
    if not pd.api.types.is_datetime64_any_dtype(df['timestamp']):
        df['timestamp'] = pd.to_datetime(df['timestamp'])
    
    df['hour'] = df['timestamp'].dt.hour
    df['day_of_week'] = df['timestamp'].dt.dayofweek
    
    # Feature matrix
    feature_cols = [
        'distance_from_home',
        'velocity',
        'heading_change',
        'hour',
        'day_of_week'
    ]
    
    X = df[feature_cols].values
    
    return X, feature_cols

def train_model(X, contamination=0.05):
    """
    Train Isolation Forest model for anomaly detection
    """
    print(f"Training Isolation Forest with contamination={contamination}")
    
    # Standardize features
    scaler = StandardScaler()
    X_scaled = scaler.fit_transform(X)
    
    # Train Isolation Forest
    model = IsolationForest(
        contamination=contamination,
        random_state=42,
        n_estimators=100,
        max_samples='auto'
    )
    
    model.fit(X_scaled)
    
    return model, scaler

def evaluate_model(model, scaler, X):
    """
    Evaluate the model and return predictions
    """
    X_scaled = scaler.transform(X)
    predictions = model.predict(X_scaled)
    scores = model.score_samples(X_scaled)
    
    # Convert to binary: -1 (anomaly) -> 1, 1 (normal) -> 0
    anomalies = (predictions == -1).astype(int)
    
    return anomalies, scores

def save_model(model, scaler, feature_cols, output_path='model.pkl'):
    """
    Save trained model and scaler
    """
    model_data = {
        'model': model,
        'scaler': scaler,
        'feature_cols': feature_cols,
        'trained_at': datetime.now().isoformat()
    }
    
    with open(output_path, 'wb') as f:
        pickle.dump(model_data, f)
    
    print(f"Model saved to {output_path}")

def main():
    """
    Main training pipeline
    """
    import argparse
    
    parser = argparse.ArgumentParser(description='Train ZoneZap Anomaly Detection Model')
    parser.add_argument('--firebase-cred', type=str, help='Path to Firebase service account JSON')
    parser.add_argument('--user-id', type=str, help='Train on specific user data (optional)')
    parser.add_argument('--limit', type=int, default=5000, help='Max records to fetch from Firestore')
    parser.add_argument('--csv', type=str, help='Use CSV file instead of Firestore')
    parser.add_argument('--contamination', type=float, default=0.05, help='Expected anomaly rate (0.01-0.1)')
    parser.add_argument('--no-firestore', action='store_true', help='Disable Firestore, use CSV/sample data')
    
    args = parser.parse_args()
    
    print("=" * 50)
    print("ZoneZap AI Engine - Training Anomaly Detection Model")
    print("=" * 50)
    
    # Initialize Firebase
    db = None
    use_firestore = not args.no_firestore
    
    if use_firestore:
        db = initialize_firebase(args.firebase_cred)
        if db:
            print("✓ Using REAL data from Firebase Firestore")
        else:
            print("[WARNING] Firebase unavailable, will try CSV or sample data")
            use_firestore = False
    
    # Load data
    if args.csv:
        print(f"Using CSV file: {args.csv}")
        df = load_movement_data(csv_path=args.csv, db=None, use_firestore=False)
    else:
        df = load_movement_data(
            csv_path='movement.csv',
            db=db,
            user_id=args.user_id,
            use_firestore=use_firestore,
            limit=args.limit
        )
    
    if df is None or len(df) == 0:
        print("[ERROR] No data available for training")
        return
    
    print(f"\nDataset Statistics:")
    print(f"  Total records: {len(df)}")
    print(f"  Speed range: {df['speed'].min():.2f} - {df['speed'].max():.2f} m/s")
    print(f"  Average speed: {df['speed'].mean():.2f} m/s ({df['speed'].mean() * 3.6:.2f} km/h)")
    
    # Extract features
    X, feature_cols = extract_features(df)
    print(f"\n[OK] Extracted {X.shape[1]} features: {', '.join(feature_cols)}")
    
    # Train model
    contamination = max(0.01, min(0.1, args.contamination))  # Clamp between 1% and 10%
    print(f"\nTraining model with contamination rate: {contamination:.1%}")
    model, scaler = train_model(X, contamination=contamination)
    
    # Evaluate on training data
    anomalies, scores = evaluate_model(model, scaler, X)
    
    n_anomalies = anomalies.sum()
    anomaly_rate = n_anomalies / len(anomalies) * 100
    
    print(f"\n" + "=" * 50)
    print(f"Training Results:")
    print(f"  Total samples: {len(anomalies)}")
    print(f"  Anomalies detected: {n_anomalies} ({anomaly_rate:.2f}%)")
    print(f"  Normal samples: {len(anomalies) - n_anomalies}")
    print(f"  Average anomaly score: {scores[anomalies == 1].mean():.3f}" if n_anomalies > 0 else "")
    print(f"  Average normal score: {scores[anomalies == 0].mean():.3f}")
    
    # Save model
    save_model(model, scaler, feature_cols, 'model.pkl')
    
    # Save training report
    data_source = "Firestore" if use_firestore and db else ("CSV" if args.csv else "Sample")
    report = {
        'training_date': datetime.now().isoformat(),
        'data_source': data_source,
        'user_id': args.user_id if args.user_id else 'all_users',
        'n_samples': len(df),
        'n_features': len(feature_cols),
        'features': feature_cols,
        'contamination': contamination,
        'n_anomalies_detected': int(n_anomalies),
        'anomaly_rate': float(anomaly_rate),
        'model_type': 'IsolationForest',
        'speed_stats': {
            'min': float(df['speed'].min()),
            'max': float(df['speed'].max()),
            'mean': float(df['speed'].mean()),
            'std': float(df['speed'].std())
        },
        'location_range': {
            'lat_min': float(df['latitude'].min()),
            'lat_max': float(df['latitude'].max()),
            'lng_min': float(df['longitude'].min()),
            'lng_max': float(df['longitude'].max())
        }
    }
    
    with open('training_report.json', 'w') as f:
        json.dump(report, f, indent=2)
    
    print(f"\n[OK] Training report saved to training_report.json")
    print("=" * 50)
    print("[OK] Training completed successfully!")
    print("=" * 50)
    print(f"\nNext steps:")
    print(f"  1. Review training_report.json for detailed statistics")
    print(f"  2. Test predictions: python predict.py")
    print(f"  3. Deploy model for real-time anomaly detection")

if __name__ == '__main__':
    main()

