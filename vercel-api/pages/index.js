export default function Home() {
  const endpoints = [
    '/api/auth/login',
    '/api/auth/register',
    '/api/users/me',
    '/api/users/by-email',
    '/api/guardians/link',
    '/api/alerts',
    '/api/alerts/[id]',
    '/api/movement-logs',
    '/api/reminders',
    '/api/reminders/[id]',
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
