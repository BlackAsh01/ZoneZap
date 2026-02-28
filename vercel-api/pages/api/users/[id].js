const { supabase } = require('../../../lib/supabase');
const { getBearerToken, verifyToken } = require('../../../lib/auth');

/**
 * GET /api/users/[id] - Returns id, name, email for a user.
 * Allowed when: requester is the user themselves, or requester is a guardian and id is in their wards.
 */
export default async function handler(req, res) {
  if (req.method !== 'GET') return res.status(405).json({ error: 'Method not allowed' });
  const token = getBearerToken(req);
  const payload = verifyToken(token);
  if (!payload) return res.status(401).json({ error: 'Unauthorized' });
  const requestedId = req.query.id;
  if (!requestedId) return res.status(400).json({ error: 'User id required' });

  try {
    if (payload.userId === requestedId) {
      const { data, error } = await supabase
        .from('users')
        .select('id, name, email')
        .eq('id', requestedId)
        .single();
      if (error || !data) return res.status(404).json({ error: 'User not found' });
      return res.status(200).json(data);
    }
    if (payload.type === 'guardian') {
      const { data: me, error: meErr } = await supabase
        .from('users')
        .select('wards')
        .eq('id', payload.userId)
        .single();
      if (meErr || !me) return res.status(403).json({ error: 'Not allowed' });
      const wards = Array.isArray(me.wards) ? me.wards : [];
      if (!wards.includes(requestedId)) return res.status(403).json({ error: 'Ward not found' });
      const { data, error } = await supabase
        .from('users')
        .select('id, name, email')
        .eq('id', requestedId)
        .single();
      if (error || !data) return res.status(404).json({ error: 'User not found' });
      return res.status(200).json(data);
    }
    return res.status(403).json({ error: 'Not allowed' });
  } catch (e) {
    return res.status(500).json({ error: e.message });
  }
}
