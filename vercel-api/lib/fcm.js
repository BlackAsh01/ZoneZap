let admin = null;

function getAdmin() {
  if (admin) return admin;
  const key = process.env.FIREBASE_SERVICE_ACCOUNT_KEY;
  if (!key) {
    console.warn('FIREBASE_SERVICE_ACCOUNT_KEY not set; FCM will be no-op.');
    return null;
  }
  try {
    const serviceAccount = typeof key === 'string' && key.startsWith('{')
      ? JSON.parse(key)
      : require(key);
    const firebaseAdmin = require('firebase-admin');
    if (!firebaseAdmin.apps.length) {
      firebaseAdmin.initializeApp({ credential: firebaseAdmin.credential.cert(serviceAccount) });
    }
    admin = firebaseAdmin;
    return admin;
  } catch (e) {
    console.error('Firebase admin init error:', e.message);
    return null;
  }
}

async function sendFCM(token, title, body, data = {}) {
  const a = getAdmin();
  if (!a || !token) return;
  try {
    await a.messaging().send({
      notification: { title, body },
      data: Object.fromEntries(Object.entries(data).map(([k, v]) => [k, String(v)])),
      token,
      android: { priority: 'high' },
    });
  } catch (e) {
    console.error('FCM send error:', e.message);
  }
}

module.exports = { sendFCM, getAdmin };
