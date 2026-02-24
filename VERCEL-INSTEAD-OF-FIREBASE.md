# ZoneZap – Using Vercel Instead of Firebase

This document explains whether you can host ZoneZap on **Vercel** instead of **Firebase**, and what would need to change.

---

## Short answer

- **You cannot “move Firebase to Vercel.”** Firebase (Auth, Firestore, Cloud Functions, FCM) is a different platform from Vercel.
- **You can host the backend (API + logic) on Vercel** and keep the Android app, but you must replace Firebase services with alternatives that work with Vercel.

---

## What ZoneZap uses Firebase for today

| Firebase service      | Role in ZoneZap |
|-----------------------|-----------------|
| **Authentication**    | Email/password sign-in (User vs Guardian). |
| **Firestore**         | Database: `users`, `alerts`, `reminders`, `movement_logs`, `alert_logs`. |
| **Cloud Functions**   | `onEmergencyAlert` (on create alert), `analyzeLocationPatterns` (on new movement_log), `checkOverdueReminders` (every 5 min). |
| **FCM**               | Push notifications to guardians (panic, wandering) and users (reminders). |

---

## What Vercel provides

| Vercel feature           | What it is |
|--------------------------|------------|
| **Serverless Functions** | HTTP-triggered API routes (Node.js, Python, etc.). No Firestore-style “on document create” triggers. |
| **Cron Jobs**            | Scheduled runs (e.g. every 5 minutes) that call your API. Available on **Pro** plan. |
| **Hosting**              | Static sites and full-stack frameworks (e.g. Next.js). |
| **No built-in**          | No database, no auth, no push. You plug in external services. |

So: **Vercel can host your API and optional web app**, but **not** Firestore, Firebase Auth, or FCM. Those must be replaced or kept elsewhere.

---

## Option 1: Backend on Vercel, replace Firebase (recommended if you want Vercel)

Use **Vercel for all backend logic** and replace Firebase with other services.

### Replacements

| Instead of           | Use on Vercel |
|----------------------|----------------|
| **Firestore**        | A database with an HTTP/API layer, e.g. **Vercel Postgres**, **Supabase** (Postgres + REST/API), **Neon**, **PlanetScale**, or any DB + REST API. |
| **Firebase Auth**     | Auth that works for mobile + API: **Supabase Auth**, **Auth0**, **Clerk**, or custom **JWT** issued by your Vercel API. |
| **Cloud Functions**   | **Vercel Serverless Functions** (API routes). The Android app calls `POST /api/alerts`, `POST /api/movement-logs`, etc. Your API does the same logic (notify guardians, analyze movement, etc.) inside the request or via internal calls. |
| **Scheduled function**| **Vercel Cron** (Pro) that hits an API route like `GET /api/cron/check-overdue-reminders` every 5 minutes. |
| **FCM**               | Keep **Firebase Cloud Messaging** for mobile push. Your Vercel API calls the **FCM HTTP v1 API** (with a service account key) to send notifications. No need to host FCM yourself. |

### Architecture (high level)

```
Android App (Kotlin)
    → HTTPS → Vercel API (e.g. /api/auth, /api/alerts, /api/reminders, /api/movement-logs)
                  → Database (e.g. Vercel Postgres or Supabase)
                  → FCM API (to send push to guardians/users)
    ← Push notifications ← FCM (unchanged)

Vercel Cron (every 5 min) → /api/cron/overdue-reminders → DB + FCM
```

### What you’d need to do

1. **Database**  
   - Choose a DB (e.g. Vercel Postgres or Supabase).  
   - Create tables/collections for users, alerts, reminders, movement_logs, alert_logs.  
   - Use environment variables for connection strings (set in Vercel project settings).

2. **Auth**  
   - Implement login/register API routes (e.g. `/api/auth/login`, `/api/auth/register`) that validate email/password and return JWTs (or use Supabase/Auth0 SDK on the server).  
   - Android app sends credentials to your API and stores the token; sends token in `Authorization` header for protected routes.

3. **API routes (replace Cloud Functions)**  
   - **Panic:** `POST /api/alerts` – body: `{ userId, alertType: "PANIC", location }`. API writes to DB, loads guardians, calls FCM to notify them.  
   - **Movement:** `POST /api/movement-logs` – API writes log, then runs “analyze last 30” + anomaly logic; if anomaly, create WANDERING alert and notify guardians via FCM.  
   - **Reminders:** CRUD routes + cron: `GET /api/cron/overdue-reminders` (called by Vercel Cron) reads overdue reminders, fetches FCM tokens, sends push.

4. **FCM from Vercel**  
   - Keep a Firebase project only for FCM (and optionally for generating FCM tokens on the client if you still use Firebase SDK for that).  
   - Store the **service account key** as a Vercel env var and use the **FCM HTTP v1 API** from your serverless functions to send notifications.

5. **Android app**  
   - Replace Firestore/Auth SDK calls with **HTTP calls** to your Vercel API (e.g. Retrofit or OkHttp).  
   - Keep FCM in the app for receiving push (or switch to another push provider if you prefer).

So: **yes, you can host the complete backend on Vercel**, but you must use a non-Firebase database and a non–Firebase Auth solution; FCM can stay and be used from Vercel.

---

## Option 2: Keep Firebase, add a web app on Vercel

- Keep **Firebase (Auth, Firestore, Cloud Functions, FCM)** as-is for the Android app.  
- Use **Vercel only** to host a **web dashboard** (e.g. Next.js) for guardians that talks to Firebase (Firebase JS SDK or your own API in front of Firestore).  

No migration of backend; Vercel only hosts the web UI.

---

## Option 3: Fully on Vercel (web app only, no Android)

- Build a **web app** (e.g. Next.js) on Vercel with API routes + DB + auth (as in Option 1).  
- Use **Web Push** or a push service for browser notifications instead of FCM.  
- The “complete application” would be web-only; the current Android app would need a separate backend or be retired.

---

## Summary

| Goal                               | Possible? | Approach |
|------------------------------------|-----------|----------|
| Host **only** Firebase on Vercel  | No        | Firebase is a different platform. |
| Host **backend logic** on Vercel  | Yes       | Use Vercel API + DB + auth + FCM from API (Option 1). |
| Host **web dashboard** on Vercel  | Yes       | Keep Firebase for mobile; add Next.js (or similar) on Vercel (Option 2). |
| **Complete app** (backend + clients) “on Vercel” | Partially | Backend and web on Vercel; Android app stays as a client that calls your Vercel API and still uses FCM for push (Option 1). |

So: **you can use Vercel to host the complete backend (and optionally a web app)** by replacing Firestore and Firebase Auth with a database and auth solution that work with Vercel, and by moving your Cloud Function logic into Vercel serverless + cron while still using FCM for push.

For step-by-step migration (e.g. “first move alerts API, then auth, then cron”), you can follow this document and implement one piece at a time.

---

## Is Vercel free when using a mobile app?

**Vercel does not host the mobile app itself.** The Android app lives on the user's device (and in the Play Store). Vercel hosts only:

- Your **backend API** (serverless functions) that the app calls over HTTPS
- Optionally a **web app** (e.g. dashboard)

So the question is: **is hosting that backend on Vercel free?**

| Plan | Cost | What you get |
|------|------|----------------|
| **Hobby (free)** | $0, no card required | Serverless functions (e.g. 1M invocations/month, 100 GB-hours execution), static hosting. Enough for a small mobile app's API. |
| **Pro** | Paid | More invocations, longer timeouts, **cron that can run more than once per day** (e.g. every 5 minutes). |

**Important limits on the free (Hobby) plan:**

- **Cron jobs:** On Hobby, cron can run **at most once per day**. Your current "every 5 minutes" overdue-reminders job would need either **Vercel Pro** (cron can run every few minutes) or an **external cron** (e.g. cron-job.org) that calls your API every 5 minutes (still uses your free function invocations but does not require Pro).
- **Function duration:** e.g. 10 s (configurable up to 60 s on Hobby).
- **Bandwidth / invocations:** Check Vercel's limits; for a small or student project they are often enough.

**Summary:** Using Vercel to host the **API that your mobile app calls** can be **free** on the Hobby plan, within those limits. The app itself is not "hosted" on Vercel; only the backend is. For "every 5 minutes" cron you need Pro or an external cron hitting your API.
