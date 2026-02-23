# ZoneZap – Complete Project Documentation

This document teaches the **ZoneZap** project to someone else: what it does, how it is built, and how the pieces fit together.

---

## Table of contents

1. [What is ZoneZap?](#1-what-is-zonezap)
2. [Architecture overview](#2-architecture-overview)
3. [Tech stack](#3-tech-stack)
4. [Repository structure](#4-repository-structure)
5. [Data model (Firestore)](#5-data-model-firestore)
6. [User roles: User vs Guardian](#6-user-roles-user-vs-guardian)
7. [Main user flows](#7-main-user-flows)
8. [Backend (Cloud Functions)](#8-backend-cloud-functions)
9. [Mobile app (Android)](#9-mobile-app-android)
10. [AI engine](#10-ai-engine)
11. [Security (Firestore rules)](#11-security-firestore-rules)
12. [How to run the project](#12-how-to-run-the-project)
13. [Glossary](#13-glossary)

---

## 1. What is ZoneZap?

**ZoneZap** is a **safety and care management** application for:

- **Users (wards):** People who need oversight (e.g. elderly, people with cognitive conditions). They use the app to log location, get reminders, and send emergency alerts.
- **Guardians:** Family or carers who monitor one or more users. They receive panic and wandering alerts, can add reminders for wards, and (in the guardian app) view wards’ status.

**Core features:**

| Feature | Description |
|--------|-------------|
| **Authentication** | Email/password sign-in. Two modes at login: **User** or **Guardian**. |
| **Location tracking** | The app records the user’s location periodically and writes to Firestore (`movement_logs`). |
| **Panic / emergency alert** | User taps a panic button → an alert is created in Firestore → Cloud Function sends FCM notifications to all guardians. |
| **Wandering detection** | Cloud Function analyzes recent `movement_logs` (speed, variance). If anomalies are found, it creates a **WANDERING** alert and notifies guardians. |
| **Reminders** | Users or guardians can create reminders (title, description, scheduled time). A scheduled Cloud Function checks every 5 minutes for overdue reminders and sends FCM to the user. |
| **Guardian–ward link** | Guardians can “add” users (wards) by email. The link is stored in Firestore (`users.guardians` / `users.wards`). |
| **AI engine (optional)** | Python service that trains an Isolation Forest model on Firestore `movement_logs` and can predict anomalies; can be used to enhance or replace the in-Cloud anomaly logic. |

---

## 2. Architecture overview

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         ZoneZap Architecture                             │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│   ┌──────────────────┐         ┌─────────────────────────────────────┐ │
│   │  Android App     │         │  Firebase                           │ │
│   │  (Kotlin)        │────────▶│  • Authentication (Email/Password)  │ │
│   │                  │  Auth   │  • Firestore (users, alerts,         │ │
│   │  • Login         │         │    reminders, movement_logs)         │ │
│   │  • Home / Panic  │         │  • Cloud Functions                   │ │
│   │  • Reminders     │         │    (alerts, location analysis,      │ │
│   │  • Guardian      │         │     overdue reminders)               │ │
│   └────────┬─────────┘         └──────────────┬──────────────────────┘ │
│            │                                 │                         │
│            │ Writes: alerts,                 │ FCM notifications       │
│            │ movement_logs,                  │ to guardians / users   │
│            │ reminders, users                │                         │
│            ▼                                 ▼                         │
│   ┌──────────────────┐         ┌─────────────────────────────────────┐ │
│   │  AI Engine       │         │  Guardians / Users (devices)          │ │
│   │  (Python)        │◀───────▶│  Receive push notifications           │ │
│   │  • Train on      │ Firestore│  (panic, wandering, reminders)        │ │
│   │    movement_logs │         └─────────────────────────────────────┘ │
│   │  • Predict       │                                                  │
│   │    anomalies     │                                                  │
│   └──────────────────┘                                                  │
└─────────────────────────────────────────────────────────────────────────┘
```

**Flow in short:**

1. User signs in on the Android app (as User or Guardian).
2. App reads/writes Firestore (users, alerts, reminders, movement_logs) using Firestore security rules.
3. Cloud Functions react to Firestore (e.g. new alert → notify guardians; new movement_log → optional anomaly check; schedule → overdue reminders).
4. Optional: AI engine trains on `movement_logs` (and optionally feeds predictions back into the system).

---

## 3. Tech stack

| Layer | Technology |
|-------|------------|
| **Backend** | Firebase: Authentication, Firestore, Cloud Functions (Node.js 18) |
| **Mobile** | Android: Kotlin, Material Design, Fused Location Provider, Coroutines |
| **AI** | Python 3.9+: scikit-learn (Isolation Forest), pandas, Firebase Admin SDK |

---

## 4. Repository structure

```
ZoneZap/
├── backend/                    # Firebase backend
│   ├── firebase.json           # Firebase config (functions, firestore, emulators)
│   ├── firestore.rules        # Firestore security rules
│   ├── firestore.indexes.json # Composite indexes for queries
│   └── functions/             # Cloud Functions (Node.js)
│       ├── package.json
│       └── index.js           # All function definitions
│
├── mobile-app-native/         # Android app (Kotlin)
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── java/com/zonezapapp/
│   │   │   │   ├── config/    # FirebaseConfig.kt
│   │   │   │   ├── data/      # Reminder, EmergencyAlert, LocationData
│   │   │   │   ├── services/  # UserService, LocationService, EmergencyService, ReminderService
│   │   │   │   └── ui/        # Activities: login, home, panic, reminders, guardian
│   │   │   ├── res/
│   │   │   └── AndroidManifest.xml
│   │   └── build.gradle
│   └── build.gradle
│
├── ai-engine/                 # Python ML for anomaly detection
│   ├── train.py               # Train on CSV or Firestore
│   ├── train_with_firebase.py # Convenience script using Firestore
│   ├── predict.py             # Load model, predict on location data
│   └── requirements.txt
│
├── SETUP-OTHER-SYSTEM.md      # Setup on another machine + Firebase
└── PROJECT-DOCUMENTATION.md   # This file
```

---

## 5. Data model (Firestore)

All data lives in **Firestore**. Main collections:

### `users`

One document per authenticated user (document ID = Firebase Auth UID).

| Field | Type | Description |
|-------|------|-------------|
| `userId` | string | Same as document ID |
| `email` | string | User email |
| `name` | string | Display name |
| `type` | string | `"user"` or `"guardian"` |
| `guardians` | array of string | (Only for `type: "user"`) UIDs of guardians |
| `wards` | array of string | (Only for `type: "guardian"`) UIDs of wards |
| `fcmToken` | string | Optional; for FCM push notifications |
| `createdAt`, `updatedAt` | timestamp | Audit |

### `alerts`

One document per emergency or system-generated alert.

| Field | Type | Description |
|-------|------|-------------|
| `userId` | string | User (ward) who triggered or is concerned |
| `alertType` | string | e.g. `"PANIC"`, `"WANDERING"` |
| `level` | string | e.g. `"CRITICAL"`, `"MEDIUM"` |
| `location` | map | `latitude`, `longitude`, optional `accuracy` |
| `timestamp` | timestamp | When the alert was created |
| `status` | string | e.g. `"ACTIVE"` |
| `anomalies` | array | (Optional) From Cloud Function anomaly detection |

### `reminders`

One document per reminder.

| Field | Type | Description |
|-------|------|-------------|
| `userId` | string | Ward (user) the reminder is for |
| `title` | string | Reminder title |
| `description` | string | Optional description |
| `scheduledTime` | timestamp | When the reminder is due |
| `type` | string | e.g. `"GENERAL"` |
| `isCompleted` | boolean | Whether it is done |
| `createdBy` / `guardianId` | string | Optional; guardian who created it |
| `createdAt`, `updatedAt`, `completedAt`, `deletedAt` | timestamp | Audit |

### `movement_logs`

One document per location sample from the app.

| Field | Type | Description |
|-------|------|-------------|
| `userId` | string | User who generated the log |
| `latitude`, `longitude` | number | Position |
| `timestamp` | timestamp | When the location was captured |
| `speed`, `heading` | number | Optional; from device |
| `accuracy` | number | Optional |

### `alert_logs`

Written only by Cloud Functions (e.g. for analytics). No client read/write (rules deny all).

---

## 6. User roles: User vs Guardian

- **User (ward):**  
  - Has `type: "user"` and a `guardians` array.  
  - Uses Home (location, reminders, panic), Reminders, and Panic screens.  
  - Can have reminders created for them by guardians.

- **Guardian:**  
  - Has `type: "guardian"` and a `wards` array.  
  - Uses Guardian screen: add wards (by email), see wards, create reminders for wards, receive panic/wandering alerts via FCM.

Linking: a guardian adds a user (ward) by email → `UserService.addGuardianToUser(userId, guardianId)` (or `addWardToGuardian`) updates both `users/{userId}.guardians` and `users/{guardianId}.wards`.

---

## 7. Main user flows

### 7.1 Sign up / Login

1. User opens app → **LoginActivity**.
2. Chooses **User** or **Guardian** mode (toggles).
3. Signs up (email + password) or logs in.
4. **UserService.createOrUpdateUser()** creates/updates `users/{uid}` with `type` and empty `guardians` or `wards`.
5. If already logged in and not from logout, app fetches `users/{uid}` and redirects to **HomeActivity** (user) or **GuardianActivity** (guardian).

### 7.2 Panic alert

1. User taps panic on Home or opens **PanicActivity** and triggers alert.
2. **EmergencyService.sendEmergencyAlert()** adds a document to `alerts` with `alertType: "PANIC"`, `userId`, `location`, `timestamp`, `status: "ACTIVE"`.
3. Cloud Function **onEmergencyAlert** (Firestore trigger on `alerts/{alertId}` create):
   - Reads `users/{userId}.guardians`.
   - For each guardian, gets `fcmToken` from `users/{guardianId}` and sends FCM notification.
   - Optionally writes to `alert_logs`.

### 7.3 Location logging and wandering detection

1. **HomeActivity** (or background flow) uses **LocationService** to get location (Fused Location Provider).
2. App writes each sample to `movement_logs` (userId, latitude, longitude, timestamp, etc.).
3. Cloud Function **analyzeLocationPatterns** (Firestore trigger on `movement_logs/{logId}` create):
   - Loads last 30 movement_logs for that user.
   - If at least 10, runs **detectAnomalies()** (speed > 5 m/s or high variance in distances).
   - If anomalies found, creates an alert in `alerts` with `alertType: "WANDERING"` and notifies guardians (same as panic flow if you add that in the function or via a shared helper).

### 7.4 Reminders

1. **RemindersActivity** (and Guardian for wards) uses **ReminderService** to create/update/delete documents in `reminders` (with `userId` = ward, optional `createdBy` = guardian).
2. Cloud Function **checkOverdueReminders** (scheduled every 5 minutes):
   - Queries `reminders` where `isCompleted == false` and `scheduledTime <= now`.
   - For each, gets `users/{userId}.fcmToken` and sends FCM notification to the user.

### 7.5 Guardian adds a ward

1. Guardian opens **GuardianActivity**, enters ward’s email.
2. **UserService.findUserByEmail()** finds a user document with `type: "user"` and that email.
3. **UserService.addGuardianToUser(wardId, guardianId)** (or **addWardToGuardian**) updates:
   - `users/{wardId}.guardians` to include guardianId
   - `users/{guardianId}.wards` to include wardId

---

## 8. Backend (Cloud Functions)

All functions live in **`backend/functions/index.js`**. Firebase Admin is initialized once; no service account path in code (uses default in deployed environment).

### 8.1 `onEmergencyAlert`

- **Trigger:** Firestore `onCreate` on `alerts/{alertId}`.
- **Logic:**  
  - Load `users/{alert.userId}` and get `guardians`.  
  - For each guardian, get `users/{guardianId}.fcmToken` and send FCM with title/body and data (alertId, userId, alertType, location, timestamp).  
  - Write to `alert_logs` (alertId, userId, alertType, timestamp, guardiansNotified).

### 8.2 `analyzeLocationPatterns`

- **Trigger:** Firestore `onCreate` on `movement_logs/{logId}`.
- **Logic:**  
  - Query last 30 `movement_logs` for `userId`, ordered by `timestamp` desc.  
  - If size &lt; 10, return.  
  - Otherwise run **detectAnomalies(locations)** (in-memory):  
    - HIGH_SPEED if average speed &gt; 5 m/s.  
    - ERRATIC_MOVEMENT if variance of step distances &gt; 10000.  
  - If any anomalies, create document in `alerts` with `alertType: "WANDERING"`, `level: "MEDIUM"`, location, timestamp, status, anomalies.

### 8.3 `checkOverdueReminders`

- **Trigger:** Pub/Sub schedule `every 5 minutes`.
- **Logic:**  
  - Query `reminders` where `isCompleted == false` and `scheduledTime <= now`.  
  - For each reminder, get `users/{reminder.userId}.fcmToken` and send FCM (title: reminder title, body: description or default).

Helper **calculateDistance** uses Haversine formula (meters). **toRadians** converts degrees to radians.

---

## 9. Mobile app (Android)

### 9.1 Screens (Activities)

| Activity | Purpose |
|----------|---------|
| **LoginActivity** | Entry; sign in/up; User vs Guardian mode; redirect to Home or Guardian. |
| **HomeActivity** | User dashboard: current location, recent reminders, panic button, FAB to reminders. |
| **PanicActivity** | Dedicated screen to trigger emergency alert (e.g. panic button). |
| **RemindersActivity** | List/create/edit/complete reminders (for current user or ward). |
| **GuardianActivity** | Guardian dashboard: list wards, add ward by email, create reminders for wards. |

### 9.2 Key services

| Service | Responsibility |
|---------|----------------|
| **FirebaseConfig** | Holds Firebase Auth and Firestore instances (initialized in app). |
| **UserService** | createOrUpdateUser, getUser, addGuardianToUser, addWardToGuardian, getGuardians, getWards, findUserByEmail, etc. |
| **LocationService** | Permissions, getCurrentLocation(), trackLocation() Flow, writes to Firestore (movement_logs) from app logic that uses it. |
| **EmergencyService** | sendEmergencyAlert(userId, alertType, location), updateAlertStatus, getActiveAlerts. |
| **ReminderService** | CRUD for `reminders` (create for userId/wardId, list, update isCompleted, etc.). |
| **WardLocationService** | (If present) Used by guardian to fetch/listen to ward location or movement_logs. |

### 9.3 Data models

- **Reminder** – id, userId, title, description, scheduledTime, type, isCompleted, createdBy, guardianId, timestamps; `fromDocument()` for Firestore.
- **EmergencyAlert** – id, userId, alertType, level, location (LocationData), timestamp, status, message; `fromDocument()`.
- **LocationData** – latitude, longitude, accuracy, speed, heading.

### 9.4 Permissions (AndroidManifest)

- INTERNET, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, ACCESS_BACKGROUND_LOCATION, VIBRATE, POST_NOTIFICATIONS.

---

## 10. AI engine

- **Purpose:** Train an anomaly detection model on location data and optionally use it for predictions (e.g. wandering risk).
- **Algorithm:** Isolation Forest (scikit-learn).
- **Data source:** Firestore `movement_logs` (or CSV). Scripts use Firebase Admin with a service account key (e.g. in `ai-engine/`).
- **train.py:** Can train from CSV (`--csv`), from Firestore (`--firebase-cred`, `--user-id`, `--limit`), or with sample data (`--no-firestore`). Outputs `model.pkl`, `training_report.json`, optional backup CSV.
- **train_with_firebase.py:** Wrapper that uses Firestore and default/service-account path.
- **predict.py:** Loads `model.pkl`, can connect to Firestore, extracts features (distance from home, velocity, heading change, hour, day of week), runs model, returns anomaly score (-1 = anomaly, 1 = normal).
- **Integration:** Cloud Functions currently use in-JS **detectAnomalies()**. The AI engine can be used to retrain periodically or to run batch predictions; replacing or complementing the Cloud Function logic would require calling the Python service from the function or reimplementing the model in Node.

---

## 11. Security (Firestore rules)

- **users:** Read own doc; list allowed for authenticated. Get allowed if owner, or in resource’s `guardians`, or (for adding guardian) requester is guardian and target is user. Create/update/delete only for own document; update also allowed when guardian is adding self to user’s guardians (and types match).
- **alerts:** Create only if `request.resource.data.userId == request.auth.uid`. Read/update if owner or guardian of the user (via `users/{userId}.guardians`).
- **reminders:** Create if owner or guardian of `request.resource.data.userId`. Read if owner or guardian of `userId`. Update/delete only if owner (`resource.data.userId == request.auth.uid`).
- **movement_logs:** Create only if `request.resource.data.userId == request.auth.uid`. Read if owner or guardian of `resource.data.userId`.
- **alert_logs:** No client access; only Cloud Functions (rules deny read, write).

Indexes (in **firestore.indexes.json**):  
- movement_logs: (userId ASC, timestamp DESC)  
- alerts: (userId ASC, status ASC, timestamp DESC)  
- reminders: (userId ASC, isCompleted ASC, scheduledTime ASC)

---

## 12. How to run the project

- **First-time setup (other user):** Do **Firebase setup** (existing project or new one), then follow **After git clone** in **SETUP-OTHER-SYSTEM.md**.
- **Clone, then:** Install Node 18+, Firebase CLI, Python 3.9+, Android Studio (JDK 17). Add `google-services.json` to `mobile-app-native/app/` and service account key to `ai-engine/`. From repo root: backend (`firebase login`, `firebase use`, `npm install` in functions, `firebase deploy --only firestore` then `--only functions`), open `mobile-app-native` in Android Studio and run, optionally set up `ai-engine` venv and run training.

See **SETUP-OTHER-SYSTEM.md** for step-by-step commands and Firebase setup for the other user.

---

## 13. Glossary

| Term | Meaning |
|------|--------|
| **Ward** | A user (type `"user"`) who is linked to one or more guardians. |
| **Guardian** | A user (type `"guardian"`) who can receive alerts and create reminders for their wards. |
| **FCM** | Firebase Cloud Messaging; used for push notifications to devices. |
| **movement_log** | A single location record in `movement_logs` (userId, lat, lng, timestamp, etc.). |
| **Anomaly** | In this project: high speed or erratic movement pattern that may indicate wandering or risk. |

---

**End of documentation.** For setup details and commands, use **SETUP-OTHER-SYSTEM.md**.
