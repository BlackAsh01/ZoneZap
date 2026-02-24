const { supabase } = require('../../../lib/supabase');
const { getBearerToken, verifyToken } = require('../../../lib/auth');
const { sendFCM } = require('../../../lib/fcm');

export default async function handler(req, res) {
  if (req.method !== 'GET' && req.method !== 'POST') return res.status(405).json({ error: 'Method not allowed' });
  const token = getBearerToken(req);
  const payload = verifyToken(token);
  if (!payload) return res.status(401).json({ error: 'Unauthorized' });
  const userId = payload.userId;

  try {
    if (req.method === 'POST') {
      const { userId: bodyUserId, alertType = 'PANIC', location } = req.body || {};
      const uid = bodyUserId || userId;
      if (uid !== userId) return res.status(403).json({ error: 'Can only create alerts for yourself' });
      const { data: alert, error: insertErr } = await supabase
        .from('alerts')
        .insert({
          user_id: uid,
          alert_type: alertType,
          level: 'CRITICAL',
          latitude: location?.latitude ?? null,
          longitude: location?.longitude ?? null,
          status: 'ACTIVE',
        })
        .select('id, user_id, alert_type, created_at')
        .single();
      if (insertErr) return res.status(500).json({ error: insertErr.message });
      const { data: user } = await supabase.from('users').select('name, guardians').eq('id', uid).single();
      const guardians = user?.guardians || [];
      for (const gid of guardians) {
        const { data: g } = await supabase.from('users').select('fcm_token').eq('id', gid).single();
        if (g?.fcm_token) {
          await sendFCM(g.fcm_token, `Emergency Alert: ${alertType}`, `${user?.name || 'User'} needs immediate assistance`, {
            alertId: alert.id,
            userId: uid,
            alertType,
            latitude: String(location?.latitude ?? ''),
            longitude: String(location?.longitude ?? ''),
            timestamp: new Date(alert.created_at).getTime().toString(),
          });
        }
      }
      await supabase.from('alert_logs').insert({
        alert_id: alert.id,
        user_id: uid,
        alert_type: alertType,
        guardians_notified: guardians.length,
      });
      return res.status(201).json(alert);
    }
    const userIdFilter = req.query.user_id || userId;
    if (userIdFilter !== userId) {
      const { data: me } = await supabase.from('users').select('wards').eq('id', userId).single();
      const wards = me?.wards || [];
      if (!wards.includes(userIdFilter)) return res.status(403).json({ error: 'Not allowed to list these alerts' });
    }
    let q = supabase.from('alerts').select('*').eq('user_id', userIdFilter).order('created_at', { ascending: false });
    if (req.query.status) q = q.eq('status', req.query.status);
    const { data, error } = await q.limit(100);
    if (error) return res.status(500).json({ error: error.message });
    return res.status(200).json(data || []);
  } catch (e) {
    return res.status(500).json({ error: e.message });
  }
}
