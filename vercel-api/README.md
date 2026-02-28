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

1. Push the repo to GitHub and connect it to Vercel (or use Vercel CLI from inside `vercel-api`).
2. **Critical:** In Vercel → your project → **Settings** → **General** → **Root Directory**: set to **`vercel-api`** and Save. If you leave this blank when the repo root is the project root, routes like `/api/guardians/wards` and `/api/users/[id]` will 404 (Vercel won’t see `pages/api/`).
3. Add the same environment variables in Vercel (Project Settings → Environment Variables).
4. Deploy (or redeploy). Your API URL will be `https://your-project.vercel.app`.
5. In the Android app set the API base URL to this URL (e.g. in BuildConfig or `ApiConfig.BASE_URL`).

**If the Guardian app shows "Ward a8171fbb" (ID) instead of names:**  
1. **Names are stored:** Signup sends `name` to `POST /api/auth/register`, and the backend saves it in Supabase `users.name`. You can confirm in Supabase → Table Editor → `users` → check the `name` column for your ward rows.  
2. **App needs the API:** The app shows names only when it gets them from the API. Redeploy the Vercel project with **Root Directory = `vercel-api`** so these routes are live:  
   - `GET /api/guardians/wards` – returns all wards with id, name, email (preferred).  
   - `GET /api/users/[id]` – fallback: returns one ward’s id, name, email when the first route returns 404.  
   After redeploy, open the Guardian dashboard again; ward names should load. If they still don’t, check Logcat for `GuardianActivity` logs: "getGuardianWards 404" and "getWardUser(...) failed" to see which endpoint is missing.

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
| GET  | /api/users/[id] | Bearer | Ward/user id, name, email (guardian: only if id in wards) |
| POST | /api/guardians/link | Bearer (guardian) | body: ward_email or ward_id |
| GET | /api/guardians/wards | Bearer (guardian) | List wards with id, name, email (required for Guardian dashboard ward names and multiple wards) |
| GET/POST | /api/alerts | Bearer | List / create alert (FCM to guardians) |
| PATCH | /api/alerts/[id] | Bearer | Update status |
| GET/POST | /api/movement-logs | Bearer | List / log (POST runs anomaly → WANDERING + FCM) |
| GET/POST | /api/reminders | Bearer | List / create |
| PATCH / DELETE | /api/reminders/[id] | Bearer | Update / complete / delete |
| GET/POST | /api/cron/overdue-reminders | Cron secret | Send FCM for overdue reminders |

## Export movement data for AI training

To train the ZoneZap AI model on the same location data the app sends:

1. From the `vercel-api` directory (with `.env` or env vars set): `npm run export-movement-logs`
2. This writes `scripts/movement_export.csv`. Then in `ai-engine`: `python train.py --csv ../vercel-api/scripts/movement_export.csv`
