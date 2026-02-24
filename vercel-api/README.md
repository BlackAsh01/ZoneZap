# ZoneZap API (Vercel)

Backend for the ZoneZap Android app when using Vercel instead of Firebase.

## Setup

1. **Supabase** – Create a project at [supabase.com](https://supabase.com). In SQL Editor, run the contents of `supabase/schema.sql`.
2. **Env** – Copy `.env.example` to `.env.local`. Set:
   - `NEXT_PUBLIC_SUPABASE_URL` / `SUPABASE_URL` – Supabase project URL
   - `SUPABASE_SERVICE_ROLE_KEY` – from Supabase Project Settings → API → service_role
   - `JWT_SECRET` – any long random string (min 32 chars)
   - `FIREBASE_SERVICE_ACCOUNT_KEY` – (optional) JSON string of Firebase service account key for FCM push
   - `CRON_SECRET` – (optional) secret for protecting `/api/cron/overdue-reminders`
3. **Install and run**
   ```bash
   npm install
   npm run dev
   ```
   API base: `http://localhost:3000`

## Deploy on Vercel

1. Push this folder to GitHub (or connect Vercel to your repo).
2. In Vercel: New Project → Import → set Root Directory to `vercel-api`.
3. Add the same environment variables in Vercel (Project Settings → Environment Variables).
4. Deploy. Your API URL will be `https://your-project.vercel.app`.
5. In the Android app set the API base URL to this URL (e.g. in BuildConfig or `ApiConfig.BASE_URL`).

## Cron (overdue reminders)

- On **Vercel Pro**, the `vercel.json` cron runs every 5 minutes.
- On **Hobby**, use an external cron (e.g. [cron-job.org](https://cron-job.org)) to call `GET https://your-app.vercel.app/api/cron/overdue-reminders` every 5 minutes (optional: add `?secret=YOUR_CRON_SECRET`).

## API summary

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | /api/auth/register | No | body: email, password, name?, type (user\|guardian) |
| POST | /api/auth/login | No | body: email, password |
| GET  | /api/users/me | Bearer | Current user |
| PATCH | /api/users/me | Bearer | Update name, fcm_token |
| GET  | /api/users/by-email?email= | Bearer | Find user by email (type=user) |
| POST | /api/guardians/link | Bearer (guardian) | body: ward_email or ward_id |
| GET/POST | /api/alerts | Bearer | List / create alert (FCM to guardians) |
| PATCH | /api/alerts/[id] | Bearer | Update status |
| GET/POST | /api/movement-logs | Bearer | List / log (POST runs anomaly → WANDERING + FCM) |
| GET/POST | /api/reminders | Bearer | List / create |
| PATCH | /api/reminders/[id] | Bearer | Update / complete |
| GET/POST | /api/cron/overdue-reminders | Cron secret | Send FCM for overdue reminders |
