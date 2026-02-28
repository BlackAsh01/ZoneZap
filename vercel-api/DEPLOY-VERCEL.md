# How to Deploy ZoneZap API to Vercel

Follow these steps in order.

**If you deploy by pushing to GitHub (auto-deploy):** In Vercel, set **Root Directory** to **`vercel-api`** (Settings → General → Root Directory). Otherwise `/api/guardians/wards`, `/api/users/[id]`, and other routes under `vercel-api/pages/api/` will return 404.

---

## Prerequisites

- **Supabase** project created and **`vercel-api/supabase/schema.sql`** run in Supabase SQL Editor.
- **`.env.local`** in `vercel-api` filled with real values (see below).

---

## Step 1: Install Vercel CLI (one-time)

Open **PowerShell** or **Command Prompt** and run:

```bash
npm install -g vercel
```

Or you can skip global install and use `npx vercel` in the next steps.

---

## Step 2: Log in to Vercel (one-time)

```bash
vercel login
```

- A browser window will open.
- Sign in with **GitHub**, **GitLab**, or **Email**.
- When it says “Success! Authentication complete,” you can close the browser and return to the terminal.

---

## Step 3: Deploy from the `vercel-api` folder

1. Go to your project folder and into `vercel-api`:

   ```bash
   cd "c:\Users\ashwi\OneDrive - School of Management Studies, Vels University(VISTAS), (Estd. u s 3 of the UGC Act 1956)\Desktop\Project - Shiv\vercel-api"
   ```

   Or from the project root:

   ```bash
   cd "Desktop\Project - Shiv\vercel-api"
   ```

2. Run:

   ```bash
   vercel
   ```

   (Or: `npx vercel` if you didn’t install globally.)

3. Answer the prompts:

   - **Set up and deploy?** → **Y**
   - **Which scope?** → choose your account (press Enter).
   - **Link to existing project?** → **N** (first time) or **Y** (if you already have a Vercel project for this).
   - **What’s your project’s name?** → e.g. **zonezap-api** (or any name). This becomes `https://zonezap-api.vercel.app`.
   - **In which directory is your code located?** → **./** (press Enter; you’re already in `vercel-api`).

4. Wait for the build to finish. At the end you’ll see something like:

   ```
   ✅ Production: https://zonezap-api.vercel.app [or your project name]
   ```

   **Copy that URL** — the Android app will use it.

---

## Step 4: Add environment variables in Vercel

The API needs Supabase and JWT to work. Add them in the Vercel dashboard:

1. Open **[vercel.com/dashboard](https://vercel.com/dashboard)**.
2. Click your project (e.g. **zonezap-api**).
3. Go to **Settings** → **Environment Variables**.
4. Add these (use the same values as in your `vercel-api/.env.local`):

   | Name                         | Value                          | Environment   |
   |-----------------------------|--------------------------------|---------------|
   | `NEXT_PUBLIC_SUPABASE_URL`  | `https://YOUR_PROJECT.supabase.co` | Production, Preview |
   | `SUPABASE_URL`              | `https://YOUR_PROJECT.supabase.co` | Production, Preview |
   | `SUPABASE_SERVICE_ROLE_KEY` | your Supabase service_role key | Production, Preview |
   | `JWT_SECRET`                | a long random string (32+ chars) | Production, Preview |

   Optional (for FCM and cron):

   - `FIREBASE_SERVICE_ACCOUNT_KEY` — Firebase service account JSON (single line).
   - `CRON_SECRET` — secret for the cron endpoint.

5. Click **Save** for each.

---

## Step 5: Redeploy so env vars are used

1. In the same project, go to **Deployments**.
2. Click the **⋯** (three dots) on the latest deployment.
3. Click **Redeploy**.
4. Wait until status is **Ready**.

Your API is now live at the URL from Step 3 (e.g. `https://zonezap-api.vercel.app`).

---

## Step 6: Point the Android app to the deployment

1. Open **`mobile-app-native/app/build.gradle`**.
2. Find:

   ```gradle
   buildConfigField "String", "API_BASE_URL", "\"https://zonezap-api.vercel.app\""
   ```

3. Replace the URL with **your** Vercel URL from Step 3 (e.g. `https://your-project-name.vercel.app`).
4. Sync Gradle and rebuild the app.

---

## Quick test

- In a browser or Postman: **GET** `https://your-vercel-url.vercel.app/`  
  You should see the Next.js landing page (or a short message), not 404.

- **POST** `https://your-vercel-url.vercel.app/api/auth/register`  
  Body (JSON): `{"email":"test@example.com","password":"test123","type":"user"}`  
  You should get a response with a token or user, not 404 or 500 (after env vars are set).

---

## Deploy again after code changes

From the `vercel-api` folder run:

```bash
vercel --prod
```

Or push to GitHub if the project is connected — Vercel will auto-deploy.

---

## Troubleshooting

| Issue | What to do |
|-------|------------|
| **404 on /api/guardians/wards, /api/users/xxx, or “Ward &lt;id&gt;” in app** | You use **Git auto-deploy** (push → Vercel builds). Vercel is building from the **repo root**, so it never sees `vercel-api/pages/api/`. **Fix:** In Vercel → Project → **Settings** → **General** → **Root Directory**: set to **`vercel-api`** (type it, then Save). Then **Redeploy** (Deployments → ⋯ → Redeploy). After that, `/api/guardians/wards` and `/api/users/[id]` will work and the Guardian dashboard will show ward names. |
| **404 / DEPLOYMENT_NOT_FOUND** | Use the exact URL from the Vercel dashboard (Deployments → your deployment). Update `API_BASE_URL` in `build.gradle` to that URL. |
| **401 / 500 on login or register** | Add or fix `SUPABASE_URL`, `SUPABASE_SERVICE_ROLE_KEY`, and `JWT_SECRET` in Vercel → Settings → Environment Variables, then redeploy. |
| **`vercel` not found** | Use `npx vercel` instead of `vercel`, or run `npm install -g vercel` again. |
| **Build fails on Vercel** | Check the build log in Deployments. If you use Git: set Root Directory to `vercel-api`. If you deploy with `vercel` from inside `vercel-api`, leave Root Directory empty. |
