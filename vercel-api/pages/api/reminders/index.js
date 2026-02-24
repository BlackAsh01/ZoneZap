const { supabase } = require('../../../lib/supabase');
const { getBearerToken, verifyToken } = require('../../../lib/auth');

export default async function handler(req, res) {
  if (req.method !== 'GET' && req.method !== 'POST') return res.status(405).json({ error: 'Method not allowed' });
  const token = getBearerToken(req);
  const payload = verifyToken(token);
  if (!payload) return res.status(401).json({ error: 'Unauthorized' });
  const userId = payload.userId;

  try {
    if (req.method === 'POST') {
      const { user_id: bodyUserId, title, description, scheduled_time, type } = req.body || {};
      const uid = bodyUserId || userId;
      if (uid !== userId) {
        const { data: me } = await supabase.from('users').select('wards').eq('id', userId).single();
        if (!(me?.wards || []).includes(uid)) return res.status(403).json({ error: 'Can only create reminders for yourself or your wards' });
      }
      const { data, error } = await supabase
        .from('reminders')
        .insert({
          user_id: uid,
          title: title || 'Reminder',
          description: description || null,
          scheduled_time: new Date(scheduled_time || Date.now()).toISOString(),
          type: type || 'GENERAL',
          created_by: uid === userId ? null : userId,
        })
        .select()
        .single();
      if (error) return res.status(500).json({ error: error.message });
      return res.status(201).json(data);
    }
    const forUserId = req.query.user_id || userId;
    if (forUserId !== userId) {
      const { data: me } = await supabase.from('users').select('wards').eq('id', userId).single();
      if (!(me?.wards || []).includes(forUserId)) return res.status(403).json({ error: 'Not allowed' });
    }
    const { data, error } = await supabase
      .from('reminders')
      .select('*')
      .eq('user_id', forUserId)
      .order('scheduled_time', { ascending: true });
    if (error) return res.status(500).json({ error: error.message });
    return res.status(200).json(data || []);
  } catch (e) {
    return res.status(500).json({ error: e.message });
  }
}
