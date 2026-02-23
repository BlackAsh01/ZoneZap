# ZoneZap — Architecture Diagrams (HLD, LLD, ML)

This document contains **High Level Design (HLD)**, **Low Level Design (LLD)**, and **ML pipeline** diagrams for the ZoneZap project. Diagrams are in Mermaid format and render on GitHub, GitLab, and most Markdown viewers.

---

## Table of contents

1. [HLD — System context](#1-hld--system-context)
2. [HLD — Container diagram](#2-hld--container-diagram)
3. [HLD — Data flow](#3-hld--data-flow)
4. [LLD — Android app components](#4-lld--android-app-components)
5. [LLD — Backend (Cloud Functions)](#5-lld--backend-cloud-functions)
6. [LLD — Sequence: Panic alert](#6-lld--sequence-panic-alert)
7. [LLD — Sequence: Wandering detection](#7-lld--sequence-wandering-detection)
8. [LLD — Sequence: Overdue reminders](#8-lld--sequence-overdue-reminders)
9. [LLD — Firestore data model](#9-lld--firestore-data-model)
10. [ML — Training pipeline](#10-ml--training-pipeline)
11. [ML — Prediction pipeline](#11-ml--prediction-pipeline)
12. [ML — Feature flow](#12-ml--feature-flow)

---

## 1. HLD — System context

Shows ZoneZap and its external users/systems.

```mermaid
flowchart TB
    subgraph External["External actors"]
        U[User / Ward\nUses app: location, reminders, panic]
        G[Guardian\nReceives alerts, creates reminders for wards]
    end

    subgraph ZoneZap["ZoneZap System"]
        APP[Android App\nKotlin]
        FIRE[Firebase\nAuth, Firestore, Functions, FCM]
        AI[AI Engine\nPython - optional]
    end

    U -->|Use| APP
    G -->|Use| APP
    APP <-->|Auth, Read/Write, FCM| FIRE
    FIRE <-->|Read movement_logs| AI
    FIRE -->|Push: reminders| U
    FIRE -->|Push: panic, wandering| G
```

---

## 2. HLD — Container diagram

Main deployable units and how they communicate.

```mermaid
flowchart TB
    subgraph Client["Client Tier"]
        ANDROID["Android App (Kotlin)\n• Login, Home, Panic\n• Reminders, Guardian\n• LocationService, EmergencyService"]
    end

    subgraph Firebase["Firebase (Cloud)"]
        AUTH[Firebase Auth\nEmail/Password]
        FS[(Firestore\nusers, alerts\nreminders\nmovement_logs\nalert_logs)]
        CF[Cloud Functions\nNode.js 18\n• onEmergencyAlert\n• analyzeLocationPatterns\n• checkOverdueReminders]
        FCM[Firebase Cloud\nMessaging]
    end

    subgraph AI["AI Tier (Optional)"]
        PY[Python AI Engine\n• train.py\n• train_with_firebase.py\n• predict.py\nIsolation Forest]
    end

    ANDROID -->|Sign in| AUTH
    ANDROID -->|Read/Write| FS
    ANDROID -->|Receive push| FCM
    FS -->|Triggers| CF
    CF -->|Read users, send push| FS
    CF -->|Send| FCM
    PY -->|Read movement_logs\n(service account)| FS
    PY -.->|Optional: write predictions| FS
```

---

## 3. HLD — Data flow

End-to-end data flow for alerts and location.

```mermaid
flowchart LR
    subgraph Input
        A1[User taps Panic]
        A2[App gets GPS]
        A3[App writes movement_log]
    end

    subgraph Firestore
        F1[(alerts)]
        F2[(movement_logs)]
        F3[(users)]
    end

    subgraph Functions
        FN1[onEmergencyAlert]
        FN2[analyzeLocationPatterns]
    end

    subgraph Output
        O1[FCM to Guardians]
        O2[WANDERING alert → FCM]
    end

    A1 --> A2
    A2 --> F1
    F1 --> FN1
    FN1 --> F3
    FN1 --> O1

    A3 --> F2
    F2 --> FN2
    FN2 --> F1
    FN2 --> O2
```

---

## 4. LLD — Android app components

Packages and main classes inside the mobile app.

```mermaid
flowchart TB
    subgraph UI["com.zonezapapp.ui"]
        Login[LoginActivity\nUser/Guardian mode]
        Home[HomeActivity\nLocation, reminders, panic]
        Panic[PanicActivity]
        Reminders[RemindersActivity]
        Guardian[GuardianActivity\nWards, add by email]
    end

    subgraph Services["com.zonezapapp.services"]
        UserSvc[UserService\ncreateOrUpdateUser\naddGuardianToUser\ngetUserGuardians\nfindUserByEmail]
        LocSvc[LocationService\ngetCurrentLocation\ntrackLocation]
        EmergSvc[EmergencyService\nsendEmergencyAlert\ngetActiveAlerts]
        RemSvc[ReminderService\nCRUD reminders]
    end

    subgraph Data["com.zonezapapp.data"]
        Rem[Reminder]
        Alert[EmergencyAlert]
        LocData[LocationData]
    end

    subgraph Config["com.zonezapapp.config"]
        FBConfig[FirebaseConfig\nAuth, Firestore]
    end

    Login --> UserSvc
    Home --> LocSvc
    Home --> EmergSvc
    Home --> RemSvc
    Panic --> EmergSvc
    Reminders --> RemSvc
    Guardian --> UserSvc
    Guardian --> RemSvc

    UserSvc --> FBConfig
    EmergSvc --> FBConfig
    RemSvc --> FBConfig
    LocSvc --> LocData
    EmergSvc --> Alert
    RemSvc --> Rem
```

---

## 5. LLD — Backend (Cloud Functions)

Functions, triggers, and internal helpers.

```mermaid
flowchart TB
    subgraph Triggers
        T1["Firestore: alerts/{id}\n onCreate"]
        T2["Firestore: movement_logs/{id}\n onCreate"]
        T3["Pub/Sub: every 5 min"]
    end

    subgraph Functions["index.js"]
        F1[onEmergencyAlert]
        F2[analyzeLocationPatterns]
        F3[checkOverdueReminders]
    end

    subgraph Helpers
        H1[detectAnomalies\nspeed > 5 m/s\nvariance threshold]
        H2[calculateDistance\nHaversine]
        H3[toRadians]
    end

    subgraph External
        FS[(Firestore)]
        FCM_SVC[FCM API]
    end

    T1 --> F1
    T2 --> F2
    T3 --> F3

    F1 --> FS
    F1 --> FCM_SVC
    F2 --> FS
    F2 --> H1
    H1 --> H2
    H2 --> H3
    F2 --> FS
    F3 --> FS
    F3 --> FCM_SVC
```

---

## 6. LLD — Sequence: Panic alert

Flow from user tap to guardian notification.

```mermaid
sequenceDiagram
    participant User
    participant App as Android App
    participant Firestore
    participant CF as onEmergencyAlert
    participant FCM
    participant Guardian

    User->>App: Tap Panic
    App->>App: Get current location
    App->>Firestore: alerts.add({ userId, alertType: PANIC, location, timestamp })
    Firestore->>CF: onCreate(alertId)
    CF->>Firestore: users.doc(userId).get()
    Firestore-->>CF: user doc (guardians[])
    loop For each guardian
        CF->>Firestore: users.doc(guardianId).get()
        Firestore-->>CF: fcmToken
        CF->>FCM: send({ token, notification, data })
        FCM->>Guardian: Push notification
    end
    CF->>Firestore: alert_logs.add(...)
```

---

## 7. LLD — Sequence: Wandering detection

Flow from new movement_log to guardian notification.

```mermaid
sequenceDiagram
    participant App as Android App
    participant Firestore
    participant CF as analyzeLocationPatterns
    participant FCM
    participant Guardian

    App->>Firestore: movement_logs.add({ userId, lat, lng, timestamp, speed })
    Firestore->>CF: onCreate(logId)
    CF->>Firestore: movement_logs.where(userId).orderBy(timestamp).limit(30)
    Firestore-->>CF: recent logs
    alt At least 10 logs
        CF->>CF: detectAnomalies(locations)
        alt Anomaly (HIGH_SPEED or ERRATIC_MOVEMENT)
            CF->>Firestore: alerts.add({ userId, alertType: WANDERING, location, anomalies })
            Firestore->>CF: onCreate(alertId) → onEmergencyAlert
            CF->>Firestore: users.doc(userId).get()
            loop For each guardian
                CF->>FCM: send(...)
                FCM->>Guardian: Push (WANDERING alert)
            end
        end
    end
```

---

## 8. LLD — Sequence: Overdue reminders

Scheduled job and FCM to user.

```mermaid
sequenceDiagram
    participant Scheduler as Pub/Sub Scheduler
    participant CF as checkOverdueReminders
    participant Firestore
    participant FCM
    participant User

    Scheduler->>CF: Every 5 minutes
    CF->>Firestore: reminders.where(isCompleted==false, scheduledTime<=now)
    Firestore-->>CF: overdue reminders
    loop For each reminder
        CF->>Firestore: users.doc(reminder.userId).get()
        Firestore-->>CF: fcmToken
        CF->>FCM: send({ token, title: reminder.title, body })
        FCM->>User: Push (Reminder)
    end
```

---

## 9. LLD — Firestore data model

Collections and main fields (entity relationship style).

```mermaid
erDiagram
    users ||--o{ users : "guardians"
    users ||--o{ users : "wards"
    users ||--o{ alerts : "userId"
    users ||--o{ reminders : "userId"
    users ||--o{ movement_logs : "userId"

    users {
        string userId PK
        string email
        string name
        string type "user|guardian"
        array guardians "user only"
        array wards "guardian only"
        string fcmToken
        timestamp createdAt
        timestamp updatedAt
    }

    alerts {
        string alertId PK
        string userId FK
        string alertType "PANIC|WANDERING"
        string level
        map location
        timestamp timestamp
        string status
        array anomalies
    }

    reminders {
        string reminderId PK
        string userId FK
        string title
        string description
        timestamp scheduledTime
        string type
        boolean isCompleted
        string createdBy
        timestamp createdAt
        timestamp updatedAt
    }

    movement_logs {
        string logId PK
        string userId FK
        float latitude
        float longitude
        timestamp timestamp
        float speed
        float heading
        float accuracy
    }

    alert_logs {
        string logId PK
        string alertId
        string userId
        string alertType
        timestamp timestamp
        int guardiansNotified
    }
```

---

## 10. ML — Training pipeline

From raw data to saved model (Python AI engine).

```mermaid
flowchart LR
    subgraph Input
        FS[(Firestore\nmovement_logs)]
        CSV[CSV file\noptional]
    end

    subgraph Scripts
        T1[train_with_firebase.py\nor train.py]
    end

    subgraph Process
        LOAD[Load movement data\nuserId, limit]
        FEAT[Feature extraction\ndistance_from_home\nvelocity, heading_change\nhour, day_of_week]
        SCALE[StandardScaler]
        IF[Isolation Forest\ncontamination e.g. 0.05]
        SAVE[Save model.pkl\nscaler, feature_cols]
        REPORT[training_report.json\nbackup CSV]
    end

    FS --> T1
    CSV --> T1
    T1 --> LOAD
    LOAD --> FEAT
    FEAT --> SCALE
    SCALE --> IF
    IF --> SAVE
    IF --> REPORT
```

---

## 11. ML — Prediction pipeline

Loading model and scoring new location data.

```mermaid
flowchart LR
    subgraph Input
        MODEL[model.pkl\nmodel, scaler, feature_cols]
        LOC[Location data\nlat, lng, speed\nheading, timestamp]
        FS[(Firestore\noptional)]
    end

    subgraph predict_py["predict.py"]
        LOAD_M[Load model.pkl]
        EXT[extract_features_from_location\ndistance_from_home\nvelocity, heading_change\nhour, day_of_week]
        SCALE_P[Apply scaler]
        PRED[model.predict\nor decision_function]
        OUT[Anomaly score\n-1 anomaly\n+1 normal]
    end

    MODEL --> LOAD_M
    LOC --> EXT
    FS -.-> LOC
    LOAD_M --> SCALE_P
    EXT --> SCALE_P
    SCALE_P --> PRED
    PRED --> OUT
```

---

## 12. ML — Feature flow

How raw location becomes model input (detailed).

```mermaid
flowchart TB
    subgraph Raw["Raw location (per point)"]
        R1[latitude]
        R2[longitude]
        R3[speed]
        R4[heading]
        R5[timestamp]
    end

    subgraph Features["Engineered features"]
        F1["distance_from_home\n(Euclidean * 111000 m)"]
        F2["velocity\n(speed)"]
        F3["heading_change\n(rate of change)"]
        F4["hour\n(0-23)"]
        F5["day_of_week\n(0-6)"]
    end

    subgraph Model["Isolation Forest"]
        IN[Feature vector]
        IF[Isolation Forest]
        SCORE[Anomaly score]
    end

    R1 --> F1
    R2 --> F1
    R3 --> F2
    R4 --> F3
    R5 --> F4
    R5 --> F5

    F1 --> IN
    F2 --> IN
    F3 --> IN
    F4 --> IN
    F5 --> IN
    IN --> IF
    IF --> SCORE
```

---

## Summary

| Diagram | Purpose |
|--------|----------|
| **HLD Context** | Who uses the system and what the system is |
| **HLD Container** | Main building blocks: Android, Firebase, AI Engine |
| **HLD Data flow** | How data moves for alerts and location |
| **LLD Android** | Activities, services, data classes, config |
| **LLD Backend** | Cloud Functions, triggers, helpers |
| **LLD Sequences** | Panic, wandering, overdue reminders step-by-step |
| **LLD Firestore** | Collections and relationships |
| **ML Training** | Firestore/CSV → features → Isolation Forest → model.pkl |
| **ML Prediction** | model.pkl + location → features → score |
| **ML Features** | Raw fields → engineered features → model input |

To view Mermaid diagrams: open this file in GitHub, GitLab, VS Code (with Mermaid extension), or any Markdown viewer that supports Mermaid.
