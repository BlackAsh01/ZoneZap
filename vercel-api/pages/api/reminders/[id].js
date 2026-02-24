const { supabase } = require('../../../lib/supabase');
const { getBearerToken, verifyToken } = require('../../../lib/auth');

export default async function handler(req, res) {
  if (req.method !== 'PATCH' && req.method !== 'DELETE') return res.status(405).json({ error: 'Method not allowed' });
  const token = getBearerToken(req);
  const payload = verifyToken(token);
  if (!payload) return res.status(401).json({ error: 'Unauthorized' });
  const id = req.query.id;
  if (!id) return res.status(400).json({ error: 'Missing reminder id' });
  try {
    const { data: reminder } = await supabase.from('reminders').select('user_id').eq('id', id).single();
    if (!reminder) return res.status(404).json({ error: 'Reminder not found' });
    if (reminder.user_id !== payload.userId) return res.status(403).json({ error: 'Not allowed' });
    if (req.method === 'DELETE') {
      await supabase.from('reminders').delete().eq('id', id);
      return res.status(200).json({ success: true });
    }
    const { is_completed, title, description, scheduled_time } = req.body || {};
    const updates = { updated_at: new Date().toISOString() };
    if (typeof is_completed === 'boolean') {
      updates.is_completed = is_completed;
      if (is_completed) updates.completed_at = new Date().toISOString();
    }
    if (title != null) updates.title = title;
    if (description != null) updates.description = description;
    if (scheduled_time != null) updates.scheduled_time = new Date(scheduled_time).toISOString();
    const { data, error } = await supabase.from('reminders').update(updates).eq('id', id).select().single();
    if (error) return res.status(500).json({ error: error.message });
    return res.status(200).json(data);
  } catch (e) {
    return res.status(500).json({ error: e.message });
  }
}
