const { supabase } = require('../../../lib/supabase');
const { getBearerToken, verifyToken } = require('../../../lib/auth');

export default async function handler(req, res) {
  if (req.method !== 'PATCH') return res.status(405).json({ error: 'Method not allowed' });
  const token = getBearerToken(req);
  const payload = verifyToken(token);
  if (!payload) return res.status(401).json({ error: 'Unauthorized' });
  const id = req.query.id;
  if (!id) return res.status(400).json({ error: 'Missing alert id' });
  try {
    const { data: alert } = await supabase.from('alerts').select('user_id').eq('id', id).single();
    if (!alert) return res.status(404).json({ error: 'Alert not found' });
    const { data: me } = await supabase.from('users').select('wards').eq('id', payload.userId).single();
    const canUpdate = alert.user_id === payload.userId || (me?.wards || []).includes(alert.user_id);
    if (!canUpdate) return res.status(403).json({ error: 'Not allowed' });
    const { status } = req.body || {};
    const updates = {};
    if (status != null) updates.status = status;
    if (Object.keys(updates).length === 0) return res.status(400).json({ error: 'No updates' });
    const { data, error } = await supabase.from('alerts').update(updates).eq('id', id).select().single();
    if (error) return res.status(500).json({ error: error.message });
    return res.status(200).json(data);
  } catch (e) {
    return res.status(500).json({ error: e.message });
  }
}
