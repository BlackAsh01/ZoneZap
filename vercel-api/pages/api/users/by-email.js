const { supabase } = require('../../../lib/supabase');
const { getBearerToken, verifyToken } = require('../../../lib/auth');

export default async function handler(req, res) {
  if (req.method !== 'GET') return res.status(405).json({ error: 'Method not allowed' });
  const token = getBearerToken(req);
  const payload = verifyToken(token);
  if (!payload) return res.status(401).json({ error: 'Unauthorized' });
  const email = (req.query.email || '').toString().trim().toLowerCase();
  if (!email) return res.status(400).json({ error: 'Missing email query' });
  try {
    const { data, error } = await supabase
      .from('users')
      .select('id, email, name, type')
      .eq('email', email)
      .eq('type', 'user')
      .limit(1)
      .single();
    if (error || !data) return res.status(404).json({ error: 'User not found' });
    return res.status(200).json(data);
  } catch (e) {
    return res.status(500).json({ error: e.message });
  }
}
