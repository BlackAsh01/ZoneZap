# How to check logs and get an error report

Use this when something fails (e.g. "Add ward" gives a parameter error) so you can share the exact error.

---

## 1. Vercel (API) logs

Your API runs on Vercel. To see what the server actually returned:

1. Go to [vercel.com](https://vercel.com) and sign in.
2. Open your project (the one that contains `vercel-api`).
3. Go to **Deployments** → click the latest deployment → **Functions** (or **Logs**).
4. Or: **Logs** in the top nav → filter by time and look for requests to `/api/guardians/link`.

You’ll see:
- Request method and path
- Response status (e.g. 400, 404, 500)
- Any `console.log` / error output from the API

**What to copy for the error report:**  
The **status code** (e.g. 400) and the **response body or error message** shown in the logs for the failing request.

---

## 2. Android (Logcat) logs

The app now logs add-ward failures with code, body, and stack trace.

1. Connect the phone via USB (or use an emulator).
2. In Android Studio: **View → Tool Windows → Logcat** (or bottom tab **Logcat**).
3. **Important:** In the Logcat filter dropdown, choose **No Filters** or **Show only selected application**, then in the **search/filter box** type exactly:  
   **`ZoneZap`**  
   (This shows both `GuardianActivity` and `ZoneZap` logs; avoid filtering by process only or you may miss our tags.)
4. Click the **Clear logcat** (trash) icon so old messages are gone.
5. In the app, tap **Add Ward**, enter the ward’s email, tap **Add**.
6. In Logcat you should see lines like:  
   - `Add ward: calling API with ward_email=...`  
   - `linkWard body: ward_email=...`  
   - Either success, or a red line: `Add ward failed: code=XXX body=... apiMsg=...`

**What to copy for the error report:**  
The full red log line (code, body, and if present apiMsg). Example:

```text
E/GuardianActivity: Add ward failed: code=400 body={"error":"Provide ward_id or ward_email in the request body"} apiMsg=Provide ward_id or ward_email in the request body
```

---

## 3. Test the API directly (optional)

To see the raw API response without the app:

1. Get a **guardian** auth token:
   - Log in as guardian in the app (or use any tool that logs the token).
   - Or call `POST /api/auth/login` with guardian email/password and copy the `token` from the JSON response.

2. Call the link endpoint (replace `YOUR_TOKEN` and `YOUR_VERCEL_URL` and the ward email):

```bash
curl -X POST "https://YOUR_VERCEL_URL/api/guardians/link" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d "{\"ward_email\":\"ward@example.com\"}"
```

3. Copy the **exact** response (status line and body), e.g.:

```text
HTTP/1.1 400 Bad Request
{"error":"Provide ward_id or ward_email in the request body"}
```

That’s your error report for the API.

---

## What to send back

Paste any of the following (or all):

- **Vercel:** status code + response/error message for the failing `/api/guardians/link` request.
- **Logcat:** the full `Add ward failed: code=... body=... apiMsg=...` line (and any line right below it if it shows a stack trace).
- **curl:** the full response (status + body).

With that, we can see whether the problem is the request (e.g. missing body, wrong URL) or the server (e.g. validation, database, “parameter” error from Supabase).
