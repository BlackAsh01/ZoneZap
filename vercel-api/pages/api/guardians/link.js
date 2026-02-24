const { supabase } = require('../../../lib/supabase');
const { getBearerToken, verifyToken } = require('../../../lib/auth');

export default async function handler(req, res) {
  if (req.method !== 'POST') return res.status(405).json({ error: 'Method not allowed' });
  const token = getBearerToken(req);
  const payload = verifyToken(token);
  if (!payload) return res.status(401).json({ error: 'Unauthorized' });
  if (payload.type !== 'guardian') return res.status(403).json({ error: 'Only guardians can link wards' });
  const guardianId = payload.userId;
  const { ward_id: wardId, ward_email: wardEmail } = req.body || {};
  const targetWardId = wardId || (wardEmail ? null : null);
  try {
    let wardUuid = targetWardId;
    if (!wardUuid && wardEmail) {
      const { data: ward } = await supabase
        .from('users')
        .select('id')
        .eq('email', String(wardEmail).toLowerCase())
        .eq('type', 'user')
        .limit(1)
        .single();
      if (!ward) return res.status(404).json({ error: 'Ward not found by email' });
      wardUuid = ward.id;
    }
    if (!wardUuid) return res.status(400).json({ error: 'Provide ward_id or ward_email' });
    const { data: wardRow } = await supabase.from('users').select('guardians, type').eq('id', wardUuid).single();
    if (!wardRow || wardRow.type !== 'user') return res.status(404).json({ error: 'Ward not found' });
    const guardians = Array.isArray(wardRow.guardians) ? [...wardRow.guardians] : [];
    if (!guardians.includes(guardianId)) guardians.push(guardianId);
    const { data: guardianRow } = await supabase.from('users').select('wards').eq('id', guardianId).single();
    const wards = Array.isArray(guardianRow?.wards) ? [...guardianRow.wards] : [];
    if (!wards.includes(wardUuid)) wards.push(wardUuid);
    await supabase.from('users').update({ guardians, updated_at: new Date().toISOString() }).eq('id', wardUuid);
    await supabase.from('users').update({ wards, updated_at: new Date().toISOString() }).eq('id', guardianId);
    return res.status(200).json({ success: true, ward_id: wardUuid });
  } catch (e) {
    return res.status(500).json({ error: e.message });
  }
}
