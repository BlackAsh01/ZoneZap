const { supabase } = require('../../../lib/supabase');
const { getBearerToken, verifyToken } = require('../../../lib/auth');
const { detectAnomalies } = require('../../../lib/anomaly');
const { sendFCM } = require('../../../lib/fcm');

export default async function handler(req, res) {
  if (req.method !== 'GET' && req.method !== 'POST') return res.status(405).json({ error: 'Method not allowed' });
  const token = getBearerToken(req);
  const payload = verifyToken(token);
  if (!payload) return res.status(401).json({ error: 'Unauthorized' });
  const userId = payload.userId;

  try {
    if (req.method === 'POST') {
      const { userId: bodyUserId, latitude, longitude, timestamp, speed, heading, accuracy } = req.body || {};
      const uid = bodyUserId || userId;
      if (uid !== userId) return res.status(403).json({ error: 'Can only log for yourself' });
      const ts = timestamp ? new Date(timestamp) : new Date();
      const { data: log, error: insertErr } = await supabase
        .from('movement_logs')
        .insert({
          user_id: uid,
          latitude: Number(latitude),
          longitude: Number(longitude),
          timestamp: ts.toISOString(),
          speed: speed != null ? Number(speed) : null,
          heading: heading != null ? Number(heading) : null,
          accuracy: accuracy != null ? Number(accuracy) : null,
        })
        .select()
        .single();
      if (insertErr) return res.status(500).json({ error: insertErr.message });
      const { data: recent } = await supabase
        .from('movement_logs')
        .select('latitude, longitude, timestamp')
        .eq('user_id', uid)
        .order('timestamp', { ascending: false })
        .limit(30);
      const locations = (recent || []).map((r) => ({
        lat: r.latitude,
        lng: r.longitude,
        timestamp: r.timestamp,
      }));
      const anomalies = locations.length >= 10 ? detectAnomalies(locations) : [];
      if (anomalies.length > 0) {
        const { data: alert } = await supabase
          .from('alerts')
          .insert({
            user_id: uid,
            alert_type: 'WANDERING',
            level: 'MEDIUM',
            latitude: log.latitude,
            longitude: log.longitude,
            status: 'ACTIVE',
            anomalies,
          })
          .select()
          .single();
        const { data: user } = await supabase.from('users').select('name, guardians').eq('id', uid).single();
        const guardians = user?.guardians || [];
        for (const gid of guardians) {
          const { data: g } = await supabase.from('users').select('fcm_token').eq('id', gid).single();
          if (g?.fcm_token) {
            await sendFCM(g.fcm_token, 'Wandering Alert', `${user?.name || 'User'} may be wandering`, {
              alertId: alert?.id,
              userId: uid,
              alertType: 'WANDERING',
            });
          }
        }
      }
      return res.status(201).json(log);
    }
    const targetUserId = req.query.user_id || userId;
    if (targetUserId !== userId) {
      const { data: me } = await supabase.from('users').select('wards').eq('id', userId).single();
      if (!(me?.wards || []).includes(targetUserId)) return res.status(403).json({ error: 'Not allowed' });
    }
    let q = supabase.from('movement_logs').select('*').eq('user_id', targetUserId).order('timestamp', { ascending: false });
    const limit = Math.min(parseInt(req.query.limit, 10) || 50, 100);
    const { data, error } = await q.limit(limit);
    if (error) return res.status(500).json({ error: error.message });
    return res.status(200).json(data || []);
  } catch (e) {
    return res.status(500).json({ error: e.message });
  }
}
