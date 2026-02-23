# ZoneZap – Setup on Another System

This guide explains how to run the **ZoneZap** project (backend, mobile app, and AI engine) on a **different computer**, including Firebase and all dependencies.

**For someone cloning the repo (the other user):** do **Firebase setup** first (see section below), then follow **After `git clone`** to run the project.

---

## What This Project Contains

| Part | Tech | Purpose |
|------|------|---------|
| **Backend** | Firebase (Firestore, Cloud Functions) | Auth, DB, alerts, notifications |
| **Mobile app** | Android (Kotlin) | ZoneZap native Android app |
| **AI engine** | Python | Anomaly detection / wandering behavior |

---

## Firebase setup for the other user

Do this **once**, separately from cloning. You either use the existing Firebase project (Option A) or create a new one (Option B). At the end you will have two files: **google-services.json** and a **service account key** JSON. Place them as shown in **Step 3** of **After `git clone`**.

### Option A: Use the existing Firebase project (zonezap-a6953)

Use this if the project owner has given you access to the same Firebase project (same data, same app).

1. **Get access**  
   The project owner adds your Google account in [Firebase Console](https://console.firebase.google.com/) → **Project Settings** → **Users and permissions** → **Add member** (e.g. Viewer or Editor).

2. **Download google-services.json**  
   - Open [Firebase Console](https://console.firebase.google.com/) → select project **zonezap-a6953**.  
   - **Project Settings** (gear) → **Your apps** → Android app (`com.zonezapapp`) → **Download google-services.json**.  
   - Save the file (e.g. to Downloads). You will copy it to `mobile-app-native/app/google-services.json` in **Step 3** of **After `git clone`**.

3. **Download the service account key**  
   - **Project Settings** → **Service accounts**.  
   - Click **Generate new private key** → confirm.  
   - Save the JSON file (e.g. `firebase-service-account.json`).  
   - You will copy it to `ai-engine/` in **Step 3** of **After `git clone`**.

**Do not commit these files to Git.** They are already in `.gitignore`.

### Option B: Create a new Firebase project

Use this if you want your own Firebase project (separate data, e.g. for development).

1. **Create the project**  
   - Go to [Firebase Console](https://console.firebase.google.com/) → **Add project**.  
   - Name it (e.g. "ZoneZap Dev") → follow the steps (Analytics optional).  
   - Note the **Project ID** (e.g. `zonezap-dev-xxxxx`).

2. **Enable Authentication**  
   - In the project, go to **Build** → **Authentication** → **Get started**.  
   - **Sign-in method** tab → **Email/Password** → Enable → **Save**.

3. **Create Firestore**  
   - **Build** → **Firestore Database** → **Create database**.  
   - Choose a region → **Next**.  
   - Start in **test mode** (we will deploy rules in a later step) → **Enable**.

4. **Register the Android app**  
   - **Project Overview** → click the Android icon.  
   - **Android package name:** `com.zonezapapp` (must match the app).  
   - Register app → **Download google-services.json**.  
   - Save it; you will put it in `mobile-app-native/app/` in **Step 3** of **After `git clone`**.

5. **Get the service account key**  
   - **Project Settings** (gear) → **Service accounts**.  
   - **Generate new private key** → confirm → save the JSON.  
   - You will put a copy in `ai-engine/` in **Step 3** of **After `git clone`**.

6. **Link the repo to this Firebase project and deploy (after cloning)**  
   From the **project root** (after you have cloned the repo):

   ```bash
   cd backend
   firebase login
   firebase use --add
   ```
   Select your new project and give it an alias (e.g. `default`).

   ```bash
   firebase deploy --only firestore
   cd functions
   npm install
   cd ..
   firebase deploy --only functions
   cd ..
   ```

   When following **After `git clone`**, use **Step 4** but run `firebase use --add` and choose this project instead of `zonezap-a6953` (or run `firebase use <your-project-id>`).

---

## After `git clone` – steps to run the project

Follow these **after** you have completed **Firebase setup for the other user** (above) and have the two config files ready.

### Step 1: Clone the repo

```bash
git clone https://github.com/BlackAsh01/ZoneZap.git
cd ZoneZap
```

### Step 2: Install prerequisites

Install these if not already installed:

- **Node.js 18+** – download from [nodejs.org](https://nodejs.org), then run the installer.
- **Firebase CLI** – run in terminal: `npm install -g firebase-tools`
- **Python 3.9+** – download from [python.org](https://www.python.org/downloads/) or use your OS package manager.
- **Android Studio** – download from [developer.android.com/studio](https://developer.android.com/studio); install JDK 17 when prompted.

**Verify installations (run these in a new terminal):**

```bash
node --version
npm --version
firebase --version
python --version
```

If `firebase` is not found, install it:

```bash
npm install -g firebase-tools
```

### Step 3: Add the Firebase config files (from Firebase setup)

You should already have these two files from **Firebase setup for the other user** (Option A or B above). Put them in the project as follows:

| File | Put it here |
|------|-------------|
| **google-services.json** | `mobile-app-native/app/google-services.json` |
| **Service account key** (e.g. `firebase-service-account.json`) | `ai-engine/` |

**Copy commands (adjust paths if your clone or download location is different):**

```bash
# Windows (PowerShell) – if files are in Downloads
Copy-Item "$env:USERPROFILE\Downloads\google-services.json" -Destination "mobile-app-native\app\google-services.json"
Copy-Item "$env:USERPROFILE\Downloads\firebase-service-account.json" -Destination "ai-engine\firebase-service-account.json"
```

```bash
# macOS / Linux – if files are in Downloads
cp ~/Downloads/google-services.json mobile-app-native/app/
cp ~/Downloads/firebase-service-account.json ai-engine/
```

### Step 4: Backend – Firebase and Cloud Functions

From the **project root** (the `ZoneZap` folder):

```bash
cd backend
firebase login
firebase use zonezap-a6953
cd functions
npm install
cd ..
firebase deploy --only firestore
firebase deploy --only functions
cd ..
```

*(If you use a different Firebase project, run `firebase use --add` and pick that project instead of `zonezap-a6953`.)*

### Step 5: Mobile app – build and run

1. Ensure **google-services.json** is in `mobile-app-native/app/`.
2. Open **Android Studio** → **File** → **Open** → select the `mobile-app-native` folder → **OK**.
3. Wait for Gradle sync to finish.
4. Connect a device or start an emulator (**Tools** → **Device Manager**), then click **Run** (green play button) or press `Shift+F10`.

**Optional – build from command line (from project root):**

```bash
cd mobile-app-native
./gradlew assembleDebug
cd ..
```

The APK will be at `mobile-app-native/app/build/outputs/apk/debug/app-debug.apk`. Install it on a device or run with `./gradlew installDebug` (device connected).

### Step 6: AI engine (optional – for training/prediction)

From the **project root**:

```bash
cd ai-engine
python -m venv .venv
```

**Activate the virtual environment:**

```bash
# Windows (PowerShell or CMD)
.venv\Scripts\activate

# Windows (Git Bash)
source .venv/Scripts/activate

# macOS / Linux
source .venv/bin/activate
```

**Install dependencies and run training:**

```bash
pip install -r requirements.txt
python train_with_firebase.py
```

If your service account key has a different name:

```bash
python train.py --firebase-cred firebase-service-account.json
```

Return to project root when done:

```bash
cd ..
```

---

## Option A: Use the Same Firebase Project (zonezap-a6953)

*(Summary: **Firebase setup for the other user** → Option A above. This section adds extra detail.)*

If you want to use the **existing** Firebase project (same data, same app):

### 1. Get These Files From the Original System (or Team)

You need two files that are **not** in the repo (and should stay secret):

| File | Where to put it | Where to get it |
|------|------------------|------------------|
| **google-services.json** | `mobile-app-native/app/google-services.json` | Firebase Console → Project Settings → Your apps → Android app → Download |
| **Service account key** (e.g. `firebase-service-account.json` or `serviceAccountKey.json`) | `ai-engine/` and optionally for local testing | Firebase Console → Project Settings → Service accounts → Generate new private key |

**Important:** Never commit these files to Git. Add them to `.gitignore` if not already.

### 2. Install Prerequisites on the New System

- **Node.js 18+** – [nodejs.org](https://nodejs.org)
- **Firebase CLI** – `npm install -g firebase-tools`
- **Python 3.9+** – for AI engine
- **Android Studio** (Hedgehog or newer) + JDK 17 – for the mobile app

### 3. Backend (Firebase)

```bash
cd backend

# Log in to Firebase (use the same Google account that owns the project)
firebase login

# Link to the existing project (if not already linked)
firebase use zonezap-a6953

# Install Cloud Functions dependencies
cd functions
npm install
cd ..

# Deploy (optional – only if you want to update live backend)
firebase deploy

# Or run emulators locally (no deploy)
firebase emulators:start --only firestore,functions,auth
```

### 4. Mobile App (Android)

1. Copy **google-services.json** to `mobile-app-native/app/`.
2. Open the `mobile-app-native` folder in **Android Studio**.
3. Let Gradle sync.
4. Run on a device or emulator.

Details: see `mobile-app-native/README.md`.

### 5. AI Engine (Python)

```bash
cd ai-engine

# Create virtual environment (recommended)
python -m venv .venv
.venv\Scripts\activate   # Windows
# source .venv/bin/activate   # macOS/Linux

pip install -r requirements.txt

# Put your service account key in ai-engine/ (e.g. firebase-service-account.json)
# Train (optional)
python train_with_firebase.py
# or: python train.py --firebase-cred firebase-service-account.json
```

---

## Option B: Create a New Firebase Project (Fresh Setup)

*(Summary: **Firebase setup for the other user** → Option B above. This section adds extra detail and commands.)*

Use this when you want a **new** Firebase project (e.g. for a new team or environment).

### 1. Create the Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/).
2. **Add project** → choose a name (e.g. “ZoneZap Dev”).
3. Enable **Google Analytics** if you want (optional).

### 2. Enable Services

- **Authentication** → Sign-in method → enable **Email/Password**.
- **Firestore Database** → Create database → choose region → start in **test mode** (we’ll replace with rules next).
- **Cloud Functions** – no extra step; it’s enabled with Blaze (pay-as-you-go) when you deploy.

### 3. Register the Android App

1. In Project Overview, click the Android icon.
2. Use package name: `com.zonezapapp` (must match `mobile-app-native`).
3. Download **google-services.json** and put it in `mobile-app-native/app/`.

### 4. Service Account Key (Backend + AI)

1. **Project Settings** (gear) → **Service accounts**.
2. **Generate new private key**.
3. Save the JSON file (e.g. `firebase-service-account.json`).
4. Put a copy in `ai-engine/` for the Python scripts.

### 5. Link This Codebase to the New Project

```bash
cd backend
firebase login
firebase use --add
# Select the new project and give it an alias (e.g. default).
```

### 6. Deploy Firestore Rules and Indexes

From the `backend` folder:

```bash
firebase deploy --only firestore
```

This deploys:

- `firestore.rules` (security rules)
- `firestore.indexes.json` (composite indexes)

### 7. Deploy Cloud Functions

```bash
cd backend/functions
npm install
cd ..
firebase deploy --only functions
```

### 8. Mobile App and AI Engine

- **Mobile:** Use the **new** `google-services.json` in `mobile-app-native/app/` (from step 3).
- **AI engine:** Use the **new** service account JSON in `ai-engine/` and run `train_with_firebase.py` or `train.py --firebase-cred <path>` as in Option A.

---

## Quick Checklist for Any New System

- [ ] **Node 18+** and **Firebase CLI** (`npm install -g firebase-tools`)
- [ ] **Python 3.9+** and `pip` (for AI engine)
- [ ] **Android Studio** + JDK 17 (for mobile app)
- [ ] **google-services.json** in `mobile-app-native/app/`
- [ ] **Service account key** in `ai-engine/` (and path set in train/predict scripts if needed)
- [ ] `firebase login` and `firebase use <project-id>` in `backend/`
- [ ] `backend/functions`: `npm install`
- [ ] `ai-engine`: `pip install -r requirements.txt` (ideally inside a venv)
- [ ] Firestore rules/indexes: `firebase deploy --only firestore`
- [ ] Cloud Functions: `firebase deploy --only functions` (when you want to update backend)

---

## Files You Must Add Manually (Do Not Commit)

| File | Used by |
|------|--------|
| `mobile-app-native/app/google-services.json` | Android app |
| `ai-engine/firebase-service-account.json` (or similar name) | AI training/prediction |

Keep these out of version control (e.g. in `.gitignore`).

---

## Troubleshooting

- **“Permission denied” in Firestore**  
  Deploy rules: `firebase deploy --only firestore`. Ensure the project is linked: `firebase use`.

- **Android: “Default FirebaseApp is not initialized”**  
  Ensure `google-services.json` is in `mobile-app-native/app/` and that the package name in Firebase matches `com.zonezapapp`.

- **AI engine: “Firebase initialization failed”**  
  Ensure the service account JSON path is correct and the file is in `ai-engine/` (or pass `--firebase-cred path/to/file.json`).

- **Cloud Functions deploy fails**  
  Ensure you’re on the **Blaze** plan for the project. Check `firebase use` and that `backend/functions` has `npm install` run.

---

## Moving this project to a Git repo

Use these steps to put the project on GitHub, GitLab, or any other Git remote.

### 1. Create a new repository on the host

- **GitHub:** [github.com/new](https://github.com/new) – create an empty repo (do **not** add README, .gitignore, or license).
- **GitLab / Bitbucket / other:** Create an empty repository and copy its URL (e.g. `https://github.com/username/zonezap.git`).

### 2. Initialize Git and push from your project folder

Open a terminal in the **project root** (the folder that contains `backend`, `mobile-app-native`, `ai-engine`) and run:

```bash
# Initialize Git (only needed once)
git init

# Stage all files (secrets are ignored by .gitignore)
git add .

# First commit
git commit -m "Initial commit: ZoneZap backend, mobile app, AI engine"

# Add your remote (example: https://github.com/BlackAsh01/ZoneZap.git)
git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO_NAME.git

# Push (main branch; use master if your host uses that)
git branch -M main
git push -u origin main
```

### 3. What gets committed

- **Included:** All source code, `firestore.rules`, `firestore.indexes.json`, `package.json`, `requirements.txt`, READMEs, `SETUP-OTHER-SYSTEM.md`, etc.
- **Excluded by .gitignore:** `google-services.json`, service account keys, `node_modules/`, `.venv/`, `*.pkl`, `.firebase/`, `.firebaserc`, and other generated or secret files.

Anyone who clones the repo will need to add `google-services.json` and the service account key on their machine (see Option A or B above).

---

## Summary

- **Same project on another PC:** Get `google-services.json` and the service account key from the original setup, install Node/Python/Android Studio/Firebase CLI, then follow **Option A**.
- **Brand new project:** Create a new Firebase project, enable Auth and Firestore, add the Android app, download `google-services.json` and the service account key, then follow **Option B** and deploy rules, indexes, and functions.

For more detail:

- Backend/Firebase: `backend/` and `backend/firebase.json`
- Mobile: `mobile-app-native/README.md`
- AI: `ai-engine/README.md`
