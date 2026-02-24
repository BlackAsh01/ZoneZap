# ZoneZap API – Deploy via command line

Copy-paste these commands. Run them in **PowerShell** or **Command Prompt**.

---

## Option 1: Vercel (recommended)

### One-time: login
```powershell
npx vercel login
```
Complete the browser sign-in when it opens.

### Go to API folder
```powershell
cd "c:\Users\ashwi\OneDrive - School of Management Studies, Vels University(VISTAS), (Estd. u s 3 of the UGC Act 1956)\Desktop\Project - Shiv\vercel-api"
```

### Deploy (first time – will ask project name)
```powershell
npx vercel
```
- **Set up and deploy?** → `Y`
- **Which scope?** → Enter (your account)
- **Link to existing project?** → `N`
- **Project name?** → `zonezap-api` (or any name)
- **Directory?** → `./` (Enter)

Copy the **Production** URL it prints (e.g. `https://zonezap-api.vercel.app`).

### Add env vars (required)
1. Open **https://vercel.com/dashboard** → your project → **Settings** → **Environment Variables**
2. Add: `NEXT_PUBLIC_SUPABASE_URL`, `SUPABASE_URL`, `SUPABASE_SERVICE_ROLE_KEY`, `JWT_SECRET` (same as in `.env.local`)

### Redeploy so env vars apply
```powershell
cd "c:\Users\ashwi\OneDrive - School of Management Studies, Vels University(VISTAS), (Estd. u s 3 of the UGC Act 1956)\Desktop\Project - Shiv\vercel-api"
npx vercel --prod
```

### Later: deploy again after code changes
```powershell
cd "c:\Users\ashwi\OneDrive - School of Management Studies, Vels University(VISTAS), (Estd. u s 3 of the UGC Act 1956)\Desktop\Project - Shiv\vercel-api"
npx vercel --prod
```

---

## Option 2: Render

Render doesn’t have a one-command deploy like Vercel. Use **GitHub + Render**:

### 1. Push code to GitHub
```powershell
cd "c:\Users\ashwi\OneDrive - School of Management Studies, Vels University(VISTAS), (Estd. u s 3 of the UGC Act 1956)\Desktop\Project - Shiv"
git init
git add .
git commit -m "ZoneZap API"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO.git
git push -u origin main
```
(Replace `YOUR_USERNAME` and `YOUR_REPO` with your GitHub repo URL.)

### 2. Create Web Service on Render
1. Go to **https://dashboard.render.com** → **New** → **Web Service**
2. Connect your **GitHub** account and select the repo
3. Settings:
   - **Name:** `zonezap-api`
   - **Root Directory:** `vercel-api`
   - **Runtime:** `Node`
   - **Build Command:** `npm install && npm run build`
   - **Start Command:** `npm run start`
4. **Environment** → Add:
   - `NEXT_PUBLIC_SUPABASE_URL`
   - `SUPABASE_URL`
   - `SUPABASE_SERVICE_ROLE_KEY`
   - `JWT_SECRET`
5. Click **Create Web Service**. Render will build and deploy; your API URL will be like `https://zonezap-api.onrender.com`.

### 3. Redeploy from CLI (after first setup)
Install Render CLI: **https://render.com/docs/cli**
```powershell
render deploy
```
(Run from project root after `render login` and linking the service once.)

---

## After deploy (either platform)

Set the API URL in the Android app:

**File:** `mobile-app-native/app/build.gradle`

```gradle
buildConfigField "String", "API_BASE_URL", "\"https://YOUR-DEPLOY-URL\""
```
- Vercel: `https://zonezap-api.vercel.app` (or your project URL)
- Render: `https://zonezap-api.onrender.com` (or your service URL)

Then rebuild the app.
