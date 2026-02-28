/**
 * Export movement_logs from Supabase to CSV for AI model training.
 * Run from project root: node vercel-api/scripts/export-movement-logs-to-csv.js
 * Requires: SUPABASE_URL, SUPABASE_SERVICE_ROLE_KEY (or NEXT_PUBLIC_SUPABASE_URL + SUPABASE_SERVICE_ROLE_KEY)
 * Output: movement_export.csv (same columns as train.py expects: latitude, longitude, timestamp, speed, heading)
 */

const { createClient } = require('@supabase/supabase-js');
const fs = require('fs');
const path = require('path');

const supabaseUrl = process.env.SUPABASE_URL || process.env.NEXT_PUBLIC_SUPABASE_URL;
const supabaseKey = process.env.SUPABASE_SERVICE_ROLE_KEY;

if (!supabaseUrl || !supabaseKey) {
  console.error('Missing SUPABASE_URL and/or SUPABASE_SERVICE_ROLE_KEY. Set them in .env or environment.');
  process.exit(1);
}

const limit = parseInt(process.env.EXPORT_LIMIT || '5000', 10);
const outPath = path.join(__dirname, 'movement_export.csv');

async function main() {
  const supabase = createClient(supabaseUrl, supabaseKey);

  console.log('Fetching movement_logs from Supabase (limit=%d)...', limit);
  const { data, error } = await supabase
    .from('movement_logs')
    .select('latitude, longitude, timestamp, speed, heading')
    .order('timestamp', { ascending: false })
    .limit(limit);

  if (error) {
    console.error('Supabase error:', error.message);
    process.exit(1);
  }

  if (!data || data.length === 0) {
    console.warn('No movement logs found. Use the app to record some location data, then run this again.');
    fs.writeFileSync(outPath, 'latitude,longitude,timestamp,speed,heading\n', 'utf8');
    console.log('Wrote empty CSV to', outPath);
    return;
  }

  const rows = data.reverse();
  const header = 'latitude,longitude,timestamp,speed,heading';
  const lines = [header].concat(
    rows.map((r) => {
      const ts = r.timestamp ? new Date(r.timestamp).toISOString() : '';
      const speed = r.speed != null ? String(r.speed) : '';
      const heading = r.heading != null ? String(r.heading) : '';
      return `${r.latitude},${r.longitude},${ts},${speed},${heading}`;
    })
  );

  fs.writeFileSync(outPath, lines.join('\n'), 'utf8');
  console.log('Exported %d rows to %s', rows.length, outPath);
  console.log('To train the model on this data:');
  console.log('  cd ai-engine');
  console.log('  python train.py --csv ../vercel-api/scripts/movement_export.csv');
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
