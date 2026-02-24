# ZoneZap – Functionalities to Update for Vercel + Mobile App

This checklist lists **every functionality** you need to add or change so the **Android app** works with a **Vercel backend** instead of Firebase.

---

## Overview

| Layer | What to do |
|-------|------------|
| **Vercel (new)** | Add API routes, database, auth, cron, and FCM calls. |
| **Android app** | Replace Firebase SDK usage with HTTP calls to your Vercel API and keep FCM for receiving push. |

---

# Part 1: Vercel backend (new)

## 1.1 Project setup

- [ ] Create a Vercel project (e.g. Next.js or plain Node API).
- [ ] Add environment variables: `DATABASE_URL`, `JWT_SECRET`, `FCM_SERVICE_ACCOUNT_KEY` (or path), `API_BASE_URL` for the app.

## 1.2 Database

- [ ] Choose a database (Vercel Postgres, Supabase, Neon, or any SQL/NoSQL with an API).
- [ ] Create tables/collections equivalent to Firestore:
  - **users** – id, email, name, type (user | guardian), guardians[] (for users), wards[] (for guardians), fcm_token, created_at, updated_at
  - **alerts** – id, user_id, alert_type, level, location (lat, lng), timestamp, status, anomalies (optional)
  - **reminders** – id, user_id, title, description, scheduled_time, type, is_completed, created_by, created_at, updated_at, completed_at, deleted_at
  - **movement_logs** – id, user_id, latitude, longitude, timestamp, speed, heading, accuracy
  - **alert_logs** – id, alert_id, user_id, alert_type, timestamp, guardians_notified (optional, for analytics)

## 1.3 Authentication API

- [ ] **POST /api/auth/register** – body: `{ email, password, name, type: "user" | "guardian" }`. Create user in DB, return JWT (and optionally create Firebase user for FCM token later, or use another flow for FCM).
- [ ] **POST /api/auth/login** – body: `{ email, password }`. Validate, return JWT and user payload (id, email, name, type).
- [ ] **POST /api/auth/refresh** (optional) – refresh JWT.
- [ ] Use a secret to sign JWTs; mobile app sends `Authorization: Bearer <token>` on every request.

## 1.4 Users API

- [ ] **GET /api/users/me** – return current user (from JWT). Used after login to get profile/type.
- [ ] **PATCH /api/users/me** – update name, fcm_token, etc.
- [ ] **GET /api/users/by-email?email=** – find user by email (for guardian “add ward”). Restrict: only guardians can call; return minimal info (id, email, name, type) if found and type is "user".
- [ ] **POST /api/users/me/guardians** or **POST /api/guardians/link** – body: `{ wardEmail }` or `{ wardId }`. Link current user (guardian) to ward: update ward’s guardians[] and guardian’s wards[] in DB.

## 1.5 Alerts API

- [ ] **POST /api/alerts** – body: `{ userId, alertType: "PANIC", location: { latitude, longitude, accuracy? } }`.  
  - Create alert in DB.  
  - Load user’s guardians, get their FCM tokens, call FCM HTTP API to send push.  
  - Optionally write to alert_logs.  
  - Return alert id.
- [ ] **GET /api/alerts** – query params: e.g. userId, status. Return alerts for current user (or wards if guardian). Auth: JWT.
- [ ] **PATCH /api/alerts/:id** – body: `{ status }`. Resolve/dismiss alert. Auth: owner or guardian of the user.

## 1.6 Movement logs API (and wandering logic)

- [ ] **POST /api/movement-logs** – body: `{ userId, latitude, longitude, timestamp, speed?, heading?, accuracy? }`.  
  - Insert into movement_logs.  
  - In the same request (or a separate serverless call): load last 30 movement_logs for this user; run anomaly detection (e.g. avg speed > 5 m/s or variance > 10000); if anomaly, create WANDERING alert and notify guardians via FCM (same as panic flow).
- [ ] **GET /api/movement-logs** – query params: userId, limit, before (timestamp). For guardian viewing ward’s location history. Auth: only for own userId or ward’s userId if caller is guardian.

## 1.7 Reminders API

- [ ] **GET /api/reminders** – query params: userId (for ward’s reminders when guardian). Return reminders for current user or for a ward if caller is that ward’s guardian.
- [ ] **POST /api/reminders** – body: `{ userId, title, description, scheduledTime, type? }`. Create reminder. Auth: userId must be self or a linked ward.
- [ ] **PATCH /api/reminders/:id** – body: `{ isCompleted?, title?, description?, scheduledTime? }`. Update reminder. Auth: owner only.
- [ ] **DELETE /api/reminders/:id** (optional). Auth: owner only.

## 1.8 Overdue reminders (cron)

- [ ] **GET or POST /api/cron/overdue-reminders** – no auth from client; secure with CRON_SECRET or Vercel Cron header.  
  - Query reminders where is_completed = false and scheduled_time <= now.  
  - For each, get user’s fcm_token and send FCM notification.  
- [ ] Configure Vercel Cron (Pro) to hit this route every 5 minutes, or use an external cron service.

## 1.9 FCM (push notifications)

- [ ] Keep a Firebase project (or minimal setup) for FCM.
- [ ] In Vercel: use FCM HTTP v1 API with a service account key (env var). Implement a small helper: given token + title + body + optional data, send the request.
- [ ] Use this helper in: POST /api/alerts (notify guardians), movement-log anomaly flow (notify guardians), and /api/cron/overdue-reminders (notify user).

---

# Part 2: Android app – functionalities to update

## 2.1 Config / base URL

- [ ] **Add API base URL** – e.g. `https://your-app.vercel.app` in BuildConfig or a config file. Use this for all API calls.
- [ ] **Add HTTP client** – use Retrofit + OkHttp (or Ktor) with:
  - Base URL from config
  - Interceptor that adds `Authorization: Bearer <token>` from secure storage (e.g. EncryptedSharedPreferences or DataStore).
  - JSON converter (e.g. Gson or Moshi).

## 2.2 Auth (replace Firebase Auth)

| Current (Firebase) | Update to |
|-------------------|-----------|
| `FirebaseConfig.auth.currentUser` | Store JWT and user payload after login; “current user” = decoded from token or from a local user model. |
| `FirebaseConfig.auth.signInWithEmailAndPassword` | Call **POST /api/auth/login** with email/password; save JWT and user; navigate by `user.type`. |
| `FirebaseConfig.auth.createUserWithEmailAndPassword` | Call **POST /api/auth/register** with email, password, name, type; save JWT and user. |
| `FirebaseConfig.auth.signOut()` | Clear stored JWT and user; navigate to login. |
| Check “user exists in Firestore” after login | Replace with **GET /api/users/me** after login to get profile/type. |

**Files to change:**

- [ ] **LoginActivity.kt** – replace Firebase sign-in/sign-up with API calls; on success save token and user; read user type and go to Home or Guardian.
- [ ] **FirebaseConfig.kt** – keep only FCM (FirebaseMessaging) for receiving push if you still use it; remove or bypass Auth and Firestore for “current user” and instead use your API + stored token.

## 2.3 User profile and guardian link

| Current (Firebase) | Update to |
|-------------------|-----------|
| UserService: create/update user in Firestore | After register/login, user is created by API; optionally **PATCH /api/users/me** for name/fcm_token. |
| UserService: get user doc | **GET /api/users/me**. |
| UserService: findUserByEmail | **GET /api/users/by-email?email=**. |
| UserService: addGuardianToUser / addWardToGuardian | **POST /api/users/me/guardians** or **POST /api/guardians/link** with ward email or id. |
| UserService: getGuardians, getWards | **GET /api/users/me** and use guardians[] or wards[] from response; or add **GET /api/users/me/guardians** and **GET /api/users/me/wards** if you expose them. |

**Files to change:**

- [ ] **UserService.kt** – replace all Firestore calls with Retrofit/API calls to the endpoints above. Keep the same public API (e.g. `createOrUpdateUser`, `getUser`, `findUserByEmail`, `addGuardianToUser`, etc.) so activities don’t need big changes.
- [ ] **GuardianActivity.kt** – ensure it uses UserService; any direct `FirebaseConfig.firestore` or `FirebaseConfig.auth` should be removed in favor of UserService (which now uses API).

## 2.4 Alerts (panic and wandering)

| Current (Firebase) | Update to |
|-------------------|-----------|
| EmergencyService: write to `alerts` collection | **POST /api/alerts** with userId, alertType "PANIC", location. |
| EmergencyService: updateAlertStatus | **PATCH /api/alerts/:id** with status. |
| EmergencyService: getActiveAlerts | **GET /api/alerts** with status=ACTIVE (and userId if needed). |
| Guardian: read alerts for wards | **GET /api/alerts** (backend returns alerts for wards of the guardian). |

**Files to change:**

- [ ] **EmergencyService.kt** – replace Firestore with API client (POST/GET/PATCH to /api/alerts). Get userId from your “current user” (token payload or stored user).
- [ ] **PanicActivity.kt** – keep UI; it already uses EmergencyService; only ensure userId is from your auth (not Firebase).
- [ ] **GuardianActivity.kt** – replace direct Firestore alerts queries with **GET /api/alerts** (via a small AlertsApi or EmergencyService).

## 2.5 Movement logs (location)

| Current (Firebase) | Update to |
|-------------------|-----------|
| HomeActivity: logLocationToFirestore | Call **POST /api/movement-logs** with userId, lat, lng, timestamp, etc. (wandering logic runs on server). |
| WardLocationService: query movement_logs for a ward | **GET /api/movement-logs?userId=<wardId>**. |

**Files to change:**

- [ ] **HomeActivity.kt** – replace `logLocationToFirestore` with a call to **POST /api/movement-logs** (same payload). Remove Firestore dependency for this.
- [ ] **WardLocationService.kt** – replace Firestore collection with **GET /api/movement-logs** for the given ward id.

## 2.6 Reminders

| Current (Firebase) | Update to |
|-------------------|-----------|
| ReminderService: list reminders (and listener) | **GET /api/reminders** (poll or use one-shot; optional: WebSocket later for real-time). |
| ReminderService: create reminder | **POST /api/reminders**. |
| ReminderService: update (complete, etc.) | **PATCH /api/reminders/:id**. |

**Files to change:**

- [ ] **ReminderService.kt** – replace all Firestore reads/writes with API: GET/POST/PATCH /api/reminders. Remove Firestore listener; use refresh after create/update or periodic fetch.
- [ ] **RemindersActivity.kt** – ensure it uses ReminderService and gets userId from your auth (not Firebase).
- [ ] **GuardianActivity.kt** – when creating reminder for ward, **POST /api/reminders** with that ward’s userId; backend checks guardian is linked.

## 2.7 FCM (receiving push)

- [ ] **Keep Firebase Messaging in the app** for receiving push (or switch to another provider). FCM tokens can still be obtained on the device; send the token to your backend via **PATCH /api/users/me** (e.g. `{ fcmToken }`) so Vercel can send notifications.
- [ ] **FirebaseConfig.kt** – keep `FirebaseMessaging.getInstance()` and token handling; remove or stub Auth and Firestore if everything else goes through API.

## 2.8 Data models

- [ ] **Reminder.kt**, **EmergencyAlert.kt** – can keep; change `fromDocument` (or equivalent) to parse from your API JSON instead of Firestore DocumentSnapshot.
- [ ] **LocationData** – keep; use same fields in API request/response.

## 2.9 Guardian screen – direct Firestore usage

- [ ] **GuardianActivity.kt** – replace every `FirebaseConfig.firestore.collection(...)` and `FirebaseConfig.auth` usage with:
  - UserService (users, guardians, wards)
  - Alerts API (alerts list, update status)
  - Reminders API (create for ward, list)
  - WardLocationService (movement_logs via API)

---

# Part 3: Summary checklist

| # | Functionality | Vercel | Mobile app |
|---|---------------|--------|------------|
| 1 | Auth (login, register, sign out, user type) | POST /api/auth/login, register; JWT | LoginActivity + stored token; replace Firebase Auth |
| 2 | Current user / profile | GET /api/users/me; PATCH for fcm_token | UserService + token; FirebaseConfig no Auth |
| 3 | Find user by email (guardian add ward) | GET /api/users/by-email | UserService.findUserByEmail → API |
| 4 | Link guardian–ward | POST /api/guardians/link or similar | UserService.addGuardianToUser → API |
| 5 | Create panic alert + notify guardians | POST /api/alerts + FCM in backend | EmergencyService → POST /api/alerts |
| 6 | List/update alerts | GET/PATCH /api/alerts | EmergencyService + GuardianActivity → API |
| 7 | Log location + wandering detection | POST /api/movement-logs (server runs anomaly) | HomeActivity → POST /api/movement-logs |
| 8 | Get ward location history | GET /api/movement-logs | WardLocationService → API |
| 9 | Reminders CRUD | GET/POST/PATCH /api/reminders | ReminderService → API |
| 10 | Overdue reminders → push | Cron → /api/cron/overdue-reminders + FCM | No change (receive FCM) |
| 11 | Receive push notifications | FCM sent from Vercel | Keep FCM in app; send token to API |

Use this list to track progress: implement each Vercel endpoint and then switch the corresponding mobile code to use the API instead of Firebase.
