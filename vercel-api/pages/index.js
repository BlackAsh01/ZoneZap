export default function Home() {
  const endpoints = [
    '/api/auth/login (POST)',
    '/api/auth/register (POST)',
    '/api/users/me (GET, PATCH)',
    '/api/users/by-email (GET)',
    '/api/guardians/link (POST)',
    '/api/guardians/wards (GET)',
    '/api/alerts (GET, POST)',
    '/api/alerts/[id] (PATCH)',
    '/api/movement-logs (GET, POST)',
    '/api/reminders (GET, POST)',
    '/api/reminders/[id] (PATCH, DELETE)',
    '/api/cron/overdue-reminders',
  ];
  return (
    <div style={{ padding: 40, fontFamily: 'sans-serif', maxWidth: 560 }}>
      <h1>ZoneZap API</h1>
      <p>Backend for ZoneZap mobile app. Use the Android app or call the API directly.</p>
      <p><strong>Endpoints:</strong></p>
      <ul style={{ lineHeight: 1.8 }}>
        {endpoints.map((ep) => (
          <li key={ep}><code>{ep}</code></li>
        ))}
      </ul>
    </div>
  );
}
