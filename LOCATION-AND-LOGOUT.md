# How ward location works

## Does logging out remove location?

**No.** Location is stored on the server (Supabase `movement_logs`). When the ward shares location, each update is sent to the API and saved. **Logging out does not delete that data.** The guardian can still see the **last stored location** for the ward.

## When is location sent?

1. **As soon as the ward opens the Home screen** (with location permission), the app now sends the **last known location** once so the guardian has at least one point even if the ward leaves quickly.
2. **While the ward stays on Home**, the app keeps sending location every few seconds (and when the ward moves ~10 m). Each update is stored.

So the guardian sees the **most recent location that was successfully sent** before the ward logged out (or closed the app).

## If the guardian still sees “No location”

- **Ward:** Stay on the Home screen for a few seconds after login so at least one location is sent. Ensure location permission is allowed.
- **Guardian:** Make sure the ward (e.g. ashwin@gmail.com) was added as a ward via “Add Ward” so the guardian’s account is linked.
- **Network:** If the ward’s device had no internet when the app tried to send, that update was not stored. Try again with Wi‑Fi or mobile data on.
- **Debug:** In Logcat (filter `HomeActivity`), a “Failed to send location” log means the API call failed (e.g. auth or server error).

## Demo flow that should show last location

1. **Ward:** Log in as ashwin@gmail.com → open Home → wait a few seconds (you should see lat/lng on screen) → then log out.
2. **Guardian:** Log in as guardian@gmail.com → dashboard should show the ward and “📍 X min ago” or a location; tap the ward to see details or “Track on map”.

The app now sends one location as soon as Home loads (if the device has a cached fix), so even a short visit can leave a “last location” for the guardian.
