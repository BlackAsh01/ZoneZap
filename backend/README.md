# ZoneZap Backend

Firebase Cloud Functions and Firestore configuration for ZoneZap.

## Setup

1. Install Firebase CLI:
```bash
npm install -g firebase-tools
```

2. Login to Firebase:
```bash
firebase login
```

3. Initialize Firebase project:
```bash
firebase init
```

4. Install dependencies:
```bash
cd functions
npm install
```

## Deployment

Deploy all functions:
```bash
firebase deploy --only functions
```

Deploy Firestore rules:
```bash
firebase deploy --only firestore:rules
```

Deploy Firestore indexes:
```bash
firebase deploy --only firestore:indexes
```

## Local Development

Start Firebase emulators:
```bash
firebase emulators:start
```

## Functions

- **onEmergencyAlert**: Triggered when a new emergency alert is created. Sends notifications to guardians.
- **analyzeLocationPatterns**: Monitors location logs and detects anomalies (wandering behavior).
- **checkOverdueReminders**: Scheduled function that checks for overdue reminders and sends notifications.

## Firestore Collections

- `users`: User profiles and guardian relationships
- `alerts`: Emergency alerts and notifications
- `reminders`: Scheduled reminders for users
- `movement_logs`: Location tracking data for AI analysis
- `alert_logs`: System logs for alerts

