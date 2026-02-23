"""
ZoneZap AI Engine - Real-time Anomaly Prediction
Uses trained model to predict anomalies in real-time location data
"""

import numpy as np
import pandas as pd
import pickle
import json
from datetime import datetime
import firebase_admin
from firebase_admin import credentials, firestore

def load_model(model_path='model.pkl'):
    """
    Load trained model and scaler
    """
    with open(model_path, 'rb') as f:
        model_data = pickle.load(f)
    
    return model_data['model'], model_data['scaler'], model_data['feature_cols']

def initialize_firebase(cred_path=None):
    """
    Initialize Firebase Admin SDK
    """
    try:
        if not firebase_admin._apps:
            if cred_path:
                cred = credentials.Certificate(cred_path)
                firebase_admin.initialize_app(cred)
            else:
                # Use default credentials (for Cloud Functions)
                firebase_admin.initialize_app()
        return firestore.client()
    except Exception as e:
        print(f"Warning: Firebase initialization failed: {e}")
        print("Running in offline mode (will not connect to Firestore)")
        return None

def extract_features_from_location(location_data, home_location=None):
    """
    Extract features from a single location data point
    """
    lat = location_data.get('latitude', 0)
    lng = location_data.get('longitude', 0)
    speed = location_data.get('speed', 0)
    heading = location_data.get('heading', 0)
    timestamp = location_data.get('timestamp')
    
    # Use provided home location or default
    if home_location is None:
        home_location = {'latitude': lat, 'longitude': lng}
    
    # Calculate distance from home
    distance_from_home = np.sqrt(
        (lat - home_location['latitude']) ** 2 + 
        (lng - home_location['longitude']) ** 2
    ) * 111000  # Convert to meters
    
    # Velocity
    velocity = speed
    
    # Heading change (would need previous heading for accurate calculation)
    heading_change = 0  # Simplified for single point
    
    # Time features
    if timestamp:
        dt = pd.to_datetime(timestamp)
        hour = dt.hour
        day_of_week = dt.dayofweek
    else:
        dt = datetime.now()
        hour = dt.hour
        day_of_week = dt.weekday()
    
    features = np.array([[
        distance_from_home,
        velocity,
        heading_change,
        hour,
        day_of_week
    ]])
    
    return features

def predict_anomaly(model, scaler, features):
    """
    Predict if location data represents an anomaly
    """
    # Scale features
    features_scaled = scaler.transform(features)
    
    # Predict
    prediction = model.predict(features_scaled)[0]
    score = model.score_samples(features_scaled)[0]
    
    # -1 = anomaly, 1 = normal
    is_anomaly = prediction == -1
    
    return is_anomaly, score

def process_realtime_location(db, model, scaler, feature_cols, location_data, user_id):
    """
    Process a real-time location update and predict anomalies
    """
    try:
        # Get user's home location from Firestore
        home_location = None
        if db:
            user_doc = db.collection('users').document(user_id).get()
            if user_doc.exists:
                user_data = user_doc.to_dict()
                home_location = user_data.get('home_location')
        
        # Extract features
        features = extract_features_from_location(location_data, home_location)
        
        # Predict
        is_anomaly, score = predict_anomaly(model, scaler, features)
        
        result = {
            'is_anomaly': bool(is_anomaly),
            'anomaly_score': float(score),
            'confidence': float(abs(score)),
            'timestamp': datetime.now().isoformat(),
            'location': {
                'latitude': location_data.get('latitude'),
                'longitude': location_data.get('longitude'),
            }
        }
        
        # If anomaly detected, create alert
        if is_anomaly and db:
            alert_data = {
                'userId': user_id,
                'alertType': 'WANDERING',
                'level': 'MEDIUM',
                'location': {
                    'latitude': location_data.get('latitude'),
                    'longitude': location_data.get('longitude'),
                },
                'timestamp': firestore.SERVER_TIMESTAMP,
                'status': 'ACTIVE',
                'ai_confidence': float(abs(score)),
                'detection_method': 'isolation_forest'
            }
            
            db.collection('alerts').add(alert_data)
            print(f"⚠️  Anomaly detected! Alert created for user {user_id}")
        
        return result
        
    except Exception as e:
        print(f"Error processing location: {e}")
        return None

def batch_predict_from_firestore(db, model, scaler, feature_cols, user_id, limit=100):
    """
    Batch predict anomalies from recent Firestore movement logs
    """
    if not db:
        print("Firebase not initialized. Cannot fetch from Firestore.")
        return []
    
    try:
        # Get recent movement logs
        logs_ref = db.collection('movement_logs')
        logs = logs_ref.where('userId', '==', user_id)\
                      .order_by('timestamp', direction=firestore.Query.DESCENDING)\
                      .limit(limit)\
                      .stream()
        
        results = []
        for log in logs:
            log_data = log.to_dict()
            log_data['timestamp'] = log_data.get('timestamp')
            
            result = process_realtime_location(
                db, model, scaler, feature_cols, log_data, user_id
            )
            
            if result:
                results.append(result)
        
        return results
        
    except Exception as e:
        print(f"Error in batch prediction: {e}")
        return []

def main():
    """
    Main prediction pipeline
    """
    print("=" * 50)
    print("ZoneZap AI Engine - Real-time Anomaly Prediction")
    print("=" * 50)
    
    # Load model
    try:
        model, scaler, feature_cols = load_model('model.pkl')
        print("✓ Model loaded successfully")
    except FileNotFoundError:
        print("✗ Error: model.pkl not found. Please train the model first.")
        print("  Run: python train.py")
        return
    
    # Initialize Firebase (optional)
    db = initialize_firebase()
    if db:
        print("✓ Firebase initialized")
    else:
        print("⚠ Firebase not initialized (offline mode)")
    
    # Example: Process a sample location
    sample_location = {
        'latitude': 12.9716,
        'longitude': 77.5946,
        'speed': 6.0,  # High speed (anomaly)
        'heading': 45,
        'timestamp': datetime.now()
    }
    
    print("\nProcessing sample location...")
    result = process_realtime_location(
        db, model, scaler, feature_cols, 
        sample_location, 'sample_user_id'
    )
    
    if result:
        print(f"\nPrediction Result:")
        print(f"  Anomaly: {'YES ⚠️' if result['is_anomaly'] else 'NO ✓'}")
        print(f"  Confidence: {result['confidence']:.3f}")
        print(f"  Score: {result['anomaly_score']:.3f}")
    
    print("\n" + "=" * 50)
    print("Prediction completed!")
    print("=" * 50)

if __name__ == '__main__':
    main()

