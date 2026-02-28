const { supabase } = require('../../../lib/supabase');
const { getBearerToken, verifyToken } = require('../../../lib/auth');

export default async function handler(req, res) {
  if (req.method !== 'POST') return res.status(405).json({ error: 'Method not allowed' });
  const token = getBearerToken(req);
  const payload = verifyToken(token);
  if (!payload) return res.status(401).json({ error: 'Unauthorized' });
  if (payload.type !== 'guardian') return res.status(403).json({ error: 'Only guardians can link wards' });
  const guardianId = payload.userId;
  const body = req.body || {};
  const wardIdRaw = body.ward_id != null ? String(body.ward_id).trim() : '';
  const wardEmailRaw = body.ward_email != null ? String(body.ward_email).trim().toLowerCase() : '';
  const wardId = wardIdRaw !== '' ? wardIdRaw : null;
  const wardEmail = wardEmailRaw !== '' ? wardEmailRaw : null;
  try {
    let wardUuid = wardId;
    if (!wardUuid && wardEmail) {
      const { data: ward, error: lookupErr } = await supabase
        .from('users')
        .select('id')
        .eq('email', wardEmail)
        .eq('type', 'user')
        .limit(1)
        .maybeSingle();
      if (lookupErr) return res.status(500).json({ error: lookupErr.message });
      if (!ward) return res.status(404).json({ error: 'Ward not found by email. User must sign up as a ward (user) first.' });
      wardUuid = ward.id;
    }
    if (!wardUuid) return res.status(400).json({ error: 'Provide ward_id or ward_email in the request body' });
    const { data: wardRow, error: wardErr } = await supabase.from('users').select('guardians, type').eq('id', wardUuid).single();
    if (wardErr || !wardRow) return res.status(404).json({ error: 'Ward not found' });
    if (wardRow.type !== 'user') return res.status(400).json({ error: 'That account is not a ward (user). Only user accounts can be added as wards.' });
    const guardians = Array.isArray(wardRow.guardians) ? [...wardRow.guardians] : [];
    if (!guardians.includes(guardianId)) guardians.push(guardianId);
    const { data: guardianRow, error: guardianErr } = await supabase.from('users').select('wards').eq('id', guardianId).single();
    if (guardianErr || !guardianRow) return res.status(500).json({ error: 'Guardian record not found' });
    const wards = Array.isArray(guardianRow.wards) ? [...guardianRow.wards] : [];
    if (!wards.includes(wardUuid)) wards.push(wardUuid);
    const { error: updateWardErr } = await supabase.from('users').update({ guardians, updated_at: new Date().toISOString() }).eq('id', wardUuid);
    if (updateWardErr) return res.status(500).json({ error: 'Failed to link ward: ' + updateWardErr.message });
    const { error: updateGuardianErr } = await supabase.from('users').update({ wards, updated_at: new Date().toISOString() }).eq('id', guardianId);
    if (updateGuardianErr) return res.status(500).json({ error: 'Failed to link guardian: ' + updateGuardianErr.message });
    return res.status(200).json({ success: true, ward_id: wardUuid });
  } catch (e) {
    return res.status(500).json({ error: e.message });
  }
}
