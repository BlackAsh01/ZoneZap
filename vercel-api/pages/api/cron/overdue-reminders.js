const { supabase } = require('../../../lib/supabase');
const { sendFCM } = require('../../../lib/fcm');

export default async function handler(req, res) {
  if (req.method !== 'GET' && req.method !== 'POST') return res.status(405).json({ error: 'Method not allowed' });
  const cronSecret = process.env.CRON_SECRET;
  if (cronSecret && req.headers['authorization'] !== `Bearer ${cronSecret}` && req.query.secret !== cronSecret) {
    return res.status(401).json({ error: 'Unauthorized' });
  }
  try {
    const now = new Date().toISOString();
    const { data: reminders } = await supabase
      .from('reminders')
      .select('id, user_id, title, description')
      .eq('is_completed', false)
      .lte('scheduled_time', now);
    let sent = 0;
    for (const r of reminders || []) {
      const { data: user } = await supabase.from('users').select('fcm_token').eq('id', r.user_id).single();
      if (user?.fcm_token) {
        await sendFCM(user.fcm_token, `Reminder: ${r.title}`, r.description || 'You have a scheduled reminder', { reminderId: r.id });
        sent++;
      }
    }
    return res.status(200).json({ processed: reminders?.length || 0, sent });
  } catch (e) {
    return res.status(500).json({ error: e.message });
  }
}
