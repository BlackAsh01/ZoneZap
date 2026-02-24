# ZoneZap: Complete Vercel Migration Setup

Use this after the code migration (Vercel API + Android on API) to **test** and **host** the app.

---

## 1. Supabase (Database)

1. Go to [supabase.com](https://supabase.com) and create a new project.
2. In the dashboard: **SQL Editor** → New query.
3. Copy the entire contents of **`vercel-api/supabase/schema.sql`** and run it.
4. Note:
   - **Project URL**: Settings → API → Project URL (e.g. `https://xxxx.supabase.co`)
   - **Service role key**: Settings → API → `service_role` (secret) — use this in the API only, never in the app.

---

## 2. Backend env (local and Vercel)

**`vercel-api/.env.local`** is already created with placeholders. Edit it and set your real values:

```env
NEXT_PUBLIC_SUPABASE_URL=https://YOUR_PROJECT.supabase.co
SUPABASE_URL=https://YOUR_PROJECT.supabase.co
SUPABASE_SERVICE_ROLE_KEY=your_service_role_key
JWT_SECRET=your-jwt-secret-at-least-32-characters-long
```

Optional (for push notifications and cron):

- `FIREBASE_SERVICE_ACCOUNT_KEY` – Firebase service account JSON (single line) for FCM.
- `CRON_SECRET` – secret for the overdue-reminders cron endpoint.

---

## 3. Run API locally (test)

```bash
cd vercel-api
npm install
npm run dev
```

- API base: **http://localhost:3000**
- For Android emulator use **http://10.0.2.2:3000** as `API_BASE_URL` in the app.

Test: register → login → create alert/reminder/movement-log.

---

## 4. Deploy backend to Vercel

### One-time: log in to Vercel

In a terminal run **`vercel login`** and complete the browser sign-in. Do this once before deploying.

### Option A: Vercel CLI

1. Install CLI: `npm i -g vercel` (or use `npx vercel` from `vercel-api`).
1. **Log in** (if not done): run `vercel login` and complete the browser login.
2. From project root:
   ```bash
   cd vercel-api
   vercel
   ```
   Follow prompts (link to existing project or create new). Set **root** to `vercel-api` if asked.
3. In [Vercel Dashboard](https://vercel.com/dashboard) → your project → **Settings → Environment Variables**, add the same variables as in `.env.local` (e.g. `SUPABASE_URL`, `SUPABASE_SERVICE_ROLE_KEY`, `JWT_SECRET`, optional FCM/CRON).
4. Redeploy if you added env after first deploy: **Deployments** → … → Redeploy.

### Option B: GitHub + Vercel

1. Push the repo to GitHub.
2. [Vercel](https://vercel.com) → New Project → Import repo.
3. Set **Root Directory** to **`vercel-api`**.
4. Add environment variables (same as above).
5. Deploy. Your API URL will be `https://your-project.vercel.app`.

---

## 5. Android app → point to hosted API

In **`mobile-app-native/app/build.gradle`** the API base URL is set via:

```gradle
buildConfigField "String", "API_BASE_URL", "\"https://zonezap-api.vercel.app\""
```

- Replace **`https://zonezap-api.vercel.app`** with your actual Vercel URL (e.g. `https://your-project.vercel.app`) and rebuild the app.
- For **emulator + local API** use: `"http://10.0.2.2:3000"`.

---

## 6. Cron (overdue reminders)

- **Vercel Pro**: Cron in `vercel.json` runs every 5 minutes.
- **Vercel Hobby**: Use an external cron (e.g. [cron-job.org](https://cron-job.org)) to call:
  `GET https://your-app.vercel.app/api/cron/overdue-reminders`
  with header `Authorization: Bearer YOUR_CRON_SECRET` or query `?secret=YOUR_CRON_SECRET` if you added that check.

---

## Checklist

- [ ] Supabase project created, `schema.sql` run
- [ ] Fill in `vercel-api/.env.local` (and add same vars in Vercel env)
- [ ] API runs locally: `cd vercel-api && npm run dev` (after Supabase env is set)
- [ ] Run `vercel login` then `vercel` from `vercel-api` to deploy
- [ ] In Vercel Dashboard add env vars and redeploy if needed
- [ ] Android: set `API_BASE_URL` in `app/build.gradle` to your Vercel URL (or 10.0.2.2:3000 for local)
- [ ] App tested: register, login (user + guardian), alerts, reminders, add ward, movement log
