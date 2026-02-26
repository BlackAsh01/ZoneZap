# Vercel Setup – Prerequisites

Complete these **before** deploying the ZoneZap API to Vercel.

---

## 1. Accounts

| Requirement | Details |
|-------------|---------|
| **Vercel account** | Sign up at [vercel.com](https://vercel.com) (use GitHub/Google/Email). |
| **Supabase account** | Sign up at [supabase.com](https://supabase.com) (free tier is enough). |
| **GitHub (optional)** | Only if you deploy by connecting a repo; not needed for CLI deploy. |

---

## 2. Supabase (database)

1. Go to [supabase.com](https://supabase.com) → **New Project**.
2. Choose organization, project name, database password, and region → **Create**.
3. When the project is ready: **SQL Editor** → **New query**.
4. Copy the **entire** contents of **`vercel-api/supabase/schema.sql`** from this repo and paste into the editor → **Run**.
5. Note these from **Project Settings** → **API**:
   - **Project URL** (e.g. `https://xxxxxxxx.supabase.co`)
   - **service_role** key (under "Project API keys" – **secret**, use only in backend env)

---

## 3. Environment variable values

You will add these in Vercel (and optionally in `vercel-api/.env.local` for local runs).

| Variable | Where to get it | Required |
|----------|-----------------|----------|
| **NEXT_PUBLIC_SUPABASE_URL** | Supabase → Settings → API → Project URL | Yes |
| **SUPABASE_URL** | Same as above | Yes |
| **SUPABASE_SERVICE_ROLE_KEY** | Supabase → Settings → API → `service_role` (secret) | Yes |
| **JWT_SECRET** | Create your own: long random string, **at least 32 characters** (e.g. use a password generator) | Yes |
| **FIREBASE_SERVICE_ACCOUNT_KEY** | Firebase Console → Project Settings → Service accounts → Generate new private key → copy JSON (single line) | No (only for FCM push) |
| **CRON_SECRET** | Create your own secret string for the cron endpoint | No (only for cron) |

---

## 4. For deploy from GitHub

- Code pushed to a **GitHub** repo (e.g. `BlackAsh01/ZoneZap` or `shivaniudhay17-cmyk/ZoneZap`).
- Vercel can access that repo (you’ll connect/link GitHub when importing the project).

---

## 5. For deploy from CLI

| Requirement | Details |
|-------------|---------|
| **Node.js** | Installed (e.g. v18 or v20). Check: `node -v`. |
| **npm** | Comes with Node. Check: `npm -v`. |
| **Vercel CLI** | Run `npx vercel login` once; use `npx vercel` so you don’t need a global install. |

---

## Checklist before deploy

- [ ] Vercel account created
- [ ] Supabase project created
- [ ] `vercel-api/supabase/schema.sql` run in Supabase SQL Editor
- [ ] Supabase **Project URL** and **service_role** key copied
- [ ] **JWT_SECRET** prepared (32+ character random string)
- [ ] If using GitHub deploy: code pushed to GitHub and repo visible to your Vercel account
- [ ] If using CLI: `npx vercel login` completed

After this, follow **DEPLOY-VERCEL.md** to deploy and add the same env vars in the Vercel project settings.
