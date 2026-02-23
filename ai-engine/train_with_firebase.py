#!/usr/bin/env python3
"""
Helper script to train model with Firebase credentials
Usage: python train_with_firebase.py [options]
"""

import os
import sys
import subprocess

def main():
    # Check for Firebase credentials
    cred_paths = [
        'firebase-service-account.json',
        'serviceAccountKey.json',
        '../backend/firebase-service-account.json',
        os.path.expanduser('~/firebase-service-account.json')
    ]
    
    cred_path = None
    for path in cred_paths:
        if os.path.exists(path):
            cred_path = path
            break
    
    # Build command
    cmd = [sys.executable, 'train.py']
    
    if cred_path:
        cmd.extend(['--firebase-cred', cred_path])
        print(f"✓ Found Firebase credentials: {cred_path}")
    else:
        print("⚠ No Firebase credentials found. Will try default credentials or emulator.")
        print("  To use Firestore, either:")
        print("  1. Download service account key from Firebase Console")
        print("  2. Save as 'firebase-service-account.json' in this directory")
        print("  3. Or use Firebase emulator (set FIRESTORE_EMULATOR_HOST)")
    
    # Add other arguments from command line
    if len(sys.argv) > 1:
        cmd.extend(sys.argv[1:])
    
    print(f"\nRunning: {' '.join(cmd)}\n")
    
    # Run training
    subprocess.run(cmd)

if __name__ == '__main__':
    main()
