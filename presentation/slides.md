# ZoneZap Presentation Slides

## Slide 1: Title Slide

**ZoneZap: A Context-Aware Safety System for Cognitively Impaired Patients**

*Using Real-Time Location Tracking and AI-Based Anomaly Detection*

- Student Name
- Student Name
- Supervisor Name
- School of Management Studies, Vels University (VISTAS)
- Date

---

## Slide 2: Problem Statement

**The Challenge**

- **60 million** people worldwide suffer from dementia
- **Wandering behavior** is a critical safety concern
- **60%** of dementia patients will wander at some point
- Traditional monitoring methods are:
  - Expensive (24/7 supervision)
  - Impractical (physical restraints)
  - Invasive (compromises dignity)

**Our Solution**: ZoneZap — a mobile-based safety system with real-time alerts and AI-powered wandering detection for users and their guardians.

---

## Slide 3: Existing Systems

**Current Solutions & Limitations**

| Solution | Pros | Cons |
|----------|------|------|
| GPS Trackers | Simple, portable | No intelligence, battery issues |
| Wearable Devices | Fall detection | Limited to falls, not wandering |
| Smart Homes | Indoor monitoring | Fixed location, expensive |
| Manual Supervision | Reliable | Costly, not scalable |

**Gap**: Few systems combine real-time AI analysis with a dedicated mobile app and guardian notification.

---

## Slide 4: Proposed Solution

**ZoneZap — Key Features**

✅ **Real-Time Location Tracking**
- Continuous GPS via Android Fused Location Provider
- Location logs stored in the cloud (Firestore)
- Configurable update intervals and distance filter

✅ **AI-Powered Anomaly Detection**
- Isolation Forest algorithm (Python + Cloud Functions)
- Detects high speed and erratic movement patterns
- Automatic wandering alerts to guardians

✅ **Dual Roles: User & Guardian**
- **Users (wards):** Use location, reminders, and panic button
- **Guardians:** Add wards by email, receive panic and wandering alerts, create reminders for wards

✅ **Emergency Panic Button**
- One-touch emergency alert
- Instant guardian notification via FCM with location
- Cloud Function processes alerts in real time

✅ **Reminders**
- Users and guardians can create reminders (title, time)
- Scheduled Cloud Function checks overdue reminders every 5 minutes and sends push notifications

---

## Slide 5: System Architecture

**Three-Tier Architecture**

```
┌─────────────────────────────────────┐
│   Mobile Application (Android)      │
│   Kotlin • Material Design          │
│   - Login (User / Guardian mode)    │
│   - Home: location, reminders, panic │
│   - Reminders, Panic, Guardian UI   │
│   - LocationService, EmergencyService│
└──────────────┬──────────────────────┘
               │ Firebase Auth & Firestore
               ▼
┌─────────────────────────────────────┐
│   Cloud Backend (Firebase)           │
│   - Firestore (users, alerts,       │
│     reminders, movement_logs)      │
│   - Cloud Functions (Node.js 18)    │
│     • onEmergencyAlert               │
│     • analyzeLocationPatterns       │
│     • checkOverdueReminders          │
│   - Firebase Cloud Messaging (FCM)  │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│   AI Engine (Python)                │
│   - Isolation Forest (scikit-learn)  │
│   - Train on Firestore or CSV        │
│   - Anomaly prediction (optional)   │
└─────────────────────────────────────┘
```

---

## Slide 6: Data Model (Firestore)

**Collections & Key Fields**

```
users
├── userId (doc id = Auth UID)
├── email, name
├── type: "user" | "guardian"
├── guardians[]     (for type "user")
├── wards[]         (for type "guardian")
├── fcmToken
└── createdAt, updatedAt

alerts
├── alertId (doc id)
├── userId, alertType (PANIC | WANDERING)
├── level, location { lat, lng }
├── timestamp, status
└── anomalies[] (from Cloud Function)

reminders
├── reminderId (doc id)
├── userId (ward)
├── title, description, scheduledTime
├── isCompleted
└── createdBy (guardian, optional)

movement_logs
├── logId (doc id)
├── userId
├── latitude, longitude, timestamp
└── speed, heading, accuracy
```

---

## Slide 7: Mobile App — Screens & Roles

**User (Ward) Flow**

1. **Login** — Email/password; choose **User** or **Guardian** mode
2. **Home** — Current location, upcoming reminders, panic button, FAB to reminders
3. **Panic** — Dedicated screen to trigger emergency alert (notifies all guardians)
4. **Reminders** — List, create, edit, and mark reminders complete

**Guardian Flow**

1. **Login** — Same auth; choose **Guardian** mode
2. **Guardian** — Add wards by email, view wards, create reminders for wards; receives FCM for panic and wandering alerts

**Tech**: Native Android (Kotlin), Material Design, Coroutines, Firebase SDK, Fused Location Provider.

---

## Slide 8: Backend — Cloud Functions

**Three Functions (Node.js)**

| Function | Trigger | Action |
|----------|---------|--------|
| **onEmergencyAlert** | Firestore: `alerts/{id}` onCreate | Read user’s guardians → send FCM to each with alert details and location |
| **analyzeLocationPatterns** | Firestore: `movement_logs/{id}` onCreate | Fetch last 30 logs for user → run anomaly logic (speed > 5 m/s or high variance) → if anomaly, create WANDERING alert (guardians notified via onEmergencyAlert) |
| **checkOverdueReminders** | Schedule: every 5 min | Query reminders where not completed and scheduledTime ≤ now → send FCM to user |

**Anomaly logic (in Cloud Function):** HIGH_SPEED if average speed > 5 m/s; ERRATIC_MOVEMENT if distance variance > threshold. Uses Haversine distance.

---

## Slide 9: AI Pipeline (Python)

**Anomaly Detection — Training & Prediction**

```
Location Data (Firestore movement_logs or CSV)
    │
    ▼
Feature Extraction
├── Distance from home
├── Velocity
├── Heading change
└── Hour, day of week
    │
    ▼
Isolation Forest (scikit-learn)
├── Training: train.py / train_with_firebase.py
├── Contamination: configurable (e.g. 5%)
└── Output: model.pkl, training_report.json
    │
    ▼
Prediction (predict.py)
├── Normal: score ≈ 1
└── Anomaly: score ≈ -1
```

**Usage**: Train on real Firestore data or CSV; optional integration with Cloud logic or batch analysis.

---

## Slide 10: Security & Privacy

**Firestore Security Rules**

- **users**: Read own doc; guardians can read wards; create/update only as owner or guardian (for linking).
- **alerts**: Create only for own userId; read/update if owner or guardian of that user.
- **reminders**: Create if owner or guardian of ward; read if owner or guardian; update/delete only owner.
- **movement_logs**: Create only for own userId; read if owner or guardian.
- **alert_logs**: Server-only (Cloud Functions); no client access.

**Other**
- Firebase Auth (email/password); TLS for all traffic; no storage of credentials in app.

---

## Slide 11: Technology Stack

**Mobile**
- Android (Kotlin), Material Design, AndroidX
- Fused Location Provider, Coroutines
- Firebase: Auth, Firestore, FCM

**Backend**
- Firebase Firestore, Cloud Functions (Node.js 18)
- Firebase Cloud Messaging

**AI**
- Python 3.9+, scikit-learn (Isolation Forest), pandas, numpy
- Firebase Admin SDK (for Firestore training data)

**Deployment**
- Firebase project (Blaze for Functions); Android APK (debug/release)

---

## Slide 12: Main User Flows (Summary)

**Panic**
- User taps panic → app writes to `alerts` → **onEmergencyAlert** → FCM to all guardians with location.

**Wandering**
- App writes location to `movement_logs` → **analyzeLocationPatterns** → if anomaly, create WANDERING alert → **onEmergencyAlert** → FCM to guardians.

**Reminders**
- User/Guardian creates reminder in `reminders` → every 5 min **checkOverdueReminders** → if overdue, FCM to user.

**Guardian–Ward link**
- Guardian adds ward by email → UserService updates `users/{ward}.guardians` and `users/{guardian}.wards`.

---

## Slide 13: Results & Impact

**System Capabilities**

| Aspect | Description |
|--------|-------------|
| Alert delivery | FCM to guardians on panic and wandering alerts |
| Reminder delivery | Scheduled check every 5 minutes; FCM to user |
| Anomaly logic | In-cloud (speed/variance) + optional Python Isolation Forest |
| Roles | Clear separation: User (ward) vs Guardian |

**Benefits**
- Proactive safety through real-time location and anomaly detection
- Immediate guardian notification on panic and wandering
- Reminders support daily care
- Scalable cloud backend (Firebase)

---

## Slide 14: Future Enhancements

**Possible Extensions**

- **Geofencing** — Safe/restricted zones; alerts on boundary crossing
- **Guardian web dashboard** — View wards and alerts in a browser
- **Wearable integration** — Heart rate, fall detection
- **Indoor positioning** — Bluetooth beacons
- **Stronger AI** — Use Python model output in Cloud Functions or real-time pipeline
- **Multi-language** — Localization for wider deployment

---

## Slide 15: Conclusion

**Summary**

✅ **Problem**: Safety monitoring for cognitively impaired users (wards) and peace of mind for guardians.

✅ **Solution**: ZoneZap — Native Android app (User + Guardian), Firebase backend, real-time alerts (panic + wandering), reminders, and optional AI engine.

✅ **Architecture**: Mobile (Kotlin) → Firestore + Cloud Functions → FCM; Python AI for training and optional prediction.

✅ **Security**: Firestore rules enforce user/guardian access; Auth and TLS in place.

**Thank you. Questions?**

---

## Slide 16: Demo (Optional)

**Live Demonstration**

1. Open ZoneZap Android app; show Login (User vs Guardian).
2. As User: show Home (location), reminders list, panic button.
3. Trigger panic; show guardian device receiving FCM.
4. As Guardian: add ward by email; create a reminder for ward.
5. (Optional) Show Firestore data or Cloud Function logs.

**Points to highlight:** Clear roles, real-time alerts, simple UI, cloud-scalable design.

---

## Presenter Notes

**Talking points**
- Slide 2: Emphasize human impact — families and caregivers.
- Slide 5: Walk through data flow: app → Firestore → Functions → FCM.
- Slide 8: Explain how panic and wandering both use the same alert pipeline (onEmergencyAlert).
- Slide 11: Clarify Native Android (Kotlin) vs React Native for accuracy.

**Time (approx.)**
- Intro & problem: 2 min  
- Solution & architecture: 4 min  
- Data model & flows: 3 min  
- Backend & AI: 3 min  
- Security, stack, results: 3 min  
- Future & conclusion: 2 min  
- Q&A: 3 min  
**Total: ~20 min**
