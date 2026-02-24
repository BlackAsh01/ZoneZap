const { supabase } = require('../../../lib/supabase');
const { getBearerToken, verifyToken } = require('../../../lib/auth');

export default async function handler(req, res) {
  if (req.method !== 'GET' && req.method !== 'PATCH') {
    return res.status(405).json({ error: 'Method not allowed' });
  }
  const token = getBearerToken(req);
  const payload = verifyToken(token);
  if (!payload) return res.status(401).json({ error: 'Unauthorized' });
  const userId = payload.userId;

  try {
    if (req.method === 'GET') {
      const { data, error } = await supabase
        .from('users')
        .select('id, email, name, type, guardians, wards, fcm_token, created_at, updated_at')
        .eq('id', userId)
        .single();
      if (error || !data) return res.status(404).json({ error: 'User not found' });
      return res.status(200).json(data);
    }
    if (req.method === 'PATCH') {
      const body = req.body || {};
      const updates = {};
      if (body.name != null) updates.name = body.name;
      if (body.fcm_token != null) updates.fcm_token = body.fcm_token;
      updates.updated_at = new Date().toISOString();
      const { data, error } = await supabase
        .from('users')
        .update(updates)
        .eq('id', userId)
        .select('id, email, name, type, guardians, wards, fcm_token')
        .single();
      if (error) return res.status(500).json({ error: error.message });
      return res.status(200).json(data);
    }
  } catch (e) {
    return res.status(500).json({ error: e.message });
  }
}
