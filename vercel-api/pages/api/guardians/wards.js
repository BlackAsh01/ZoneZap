const { supabase } = require('../../../lib/supabase');
const { getBearerToken, verifyToken } = require('../../../lib/auth');

/** GET /api/guardians/wards - Returns list of wards with id, name, email for the current guardian. */
export default async function handler(req, res) {
  if (req.method !== 'GET') return res.status(405).json({ error: 'Method not allowed' });
  const token = getBearerToken(req);
  const payload = verifyToken(token);
  if (!payload) return res.status(401).json({ error: 'Unauthorized' });
  if (payload.type !== 'guardian') return res.status(403).json({ error: 'Only guardians can list wards' });

  try {
    const { data: me, error: meErr } = await supabase
      .from('users')
      .select('wards')
      .eq('id', payload.userId)
      .single();
    if (meErr || !me) return res.status(500).json({ error: 'Guardian record not found' });
    const wardIds = Array.isArray(me.wards) ? me.wards : [];
    if (wardIds.length === 0) return res.status(200).json([]);

    const { data: users, error } = await supabase
      .from('users')
      .select('id, name, email')
      .in('id', wardIds)
      .eq('type', 'user');
    if (error) return res.status(500).json({ error: error.message });
    const list = (users || []).map((u) => ({ id: u.id, name: u.name || '', email: u.email || '' }));
    return res.status(200).json(list);
  } catch (e) {
    return res.status(500).json({ error: e.message });
  }
}
