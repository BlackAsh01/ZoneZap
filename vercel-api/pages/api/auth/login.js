const { supabase } = require('../../../lib/supabase');
const { comparePassword, signToken } = require('../../../lib/auth');

export default async function handler(req, res) {
  if (req.method !== 'POST') {
    return res.status(405).json({ error: 'Method not allowed' });
  }
  try {
    const { email, password } = req.body || {};
    if (!email || !password) {
      return res.status(400).json({ error: 'Missing email or password' });
    }
    const { data: user, error } = await supabase
      .from('users')
      .select('id, email, name, type, password_hash, guardians, wards')
      .eq('email', email.toLowerCase())
      .single();
    if (error || !user) {
      return res.status(401).json({ error: 'Invalid email or password' });
    }
    const ok = await comparePassword(password, user.password_hash);
    if (!ok) return res.status(401).json({ error: 'Invalid email or password' });
    const { password_hash, ...safe } = user;
    const token = signToken({ userId: safe.id, email: safe.email, type: safe.type });
    return res.status(200).json({ token, user: safe });
  } catch (e) {
    return res.status(500).json({ error: e.message });
  }
}
