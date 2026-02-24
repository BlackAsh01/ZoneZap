export default function Home() {
  return (
    <div style={{ padding: 40, fontFamily: 'sans-serif' }}>
      <h1>ZoneZap API</h1>
      <p>Backend for ZoneZap mobile app. Use the Android app or call the API directly.</p>
      <p><strong>Endpoints:</strong> /api/auth/login, /api/auth/register, /api/users/me, /api/alerts, /api/movement-logs, /api/reminders, /api/cron/overdue-reminders</p>
    </div>
  );
}
