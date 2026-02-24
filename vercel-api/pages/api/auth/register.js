const { supabase } = require('../../../lib/supabase');
const { hashPassword, signToken } = require('../../../lib/auth');

export default async function handler(req, res) {
  if (req.method !== 'POST') {
    return res.status(405).json({ error: 'Method not allowed' });
  }
  try {
    const { email, password, name, type } = req.body || {};
    if (!email || !password || !type || !['user', 'guardian'].includes(type)) {
      return res.status(400).json({ error: 'Missing or invalid email, password, or type (user|guardian)' });
    }
    const password_hash = await hashPassword(password);
    const displayName = name || email.split('@')[0];
    const { data: user, error } = await supabase
      .from('users')
      .insert({
        email: email.toLowerCase(),
        password_hash,
        name: displayName,
        type,
        ...(type === 'user' ? { guardians: [] } : { wards: [] }),
      })
      .select('id, email, name, type, guardians, wards, created_at')
      .single();
    if (error) {
      if (error.code === '23505') return res.status(409).json({ error: 'Email already registered' });
      return res.status(500).json({ error: error.message });
    }
    const token = signToken({ userId: user.id, email: user.email, type: user.type });
    return res.status(201).json({ token, user: { ...user, id: user.id } });
  } catch (e) {
    return res.status(500).json({ error: e.message });
  }
}
