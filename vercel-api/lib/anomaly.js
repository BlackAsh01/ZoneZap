function calculateDistance(lat1, lon1, lat2, lon2) {
  const R = 6371000;
  const dLat = (lat2 - lat1) * Math.PI / 180;
  const dLon = (lon2 - lon1) * Math.PI / 180;
  const a =
    Math.sin(dLat / 2) ** 2 +
    Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) * Math.sin(dLon / 2) ** 2;
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c;
}

function detectAnomalies(locations) {
  const anomalies = [];
  if (locations.length < 2) return anomalies;
  let totalDistance = 0;
  let totalTime = 0;
  for (let i = 1; i < locations.length; i++) {
    const prev = locations[i - 1];
    const curr = locations[i];
    const distance = calculateDistance(prev.lat, prev.lng, curr.lat, curr.lng);
    const timeDiff = (curr.timestamp ? new Date(curr.timestamp).getTime() : 0) - (prev.timestamp ? new Date(prev.timestamp).getTime() : 0);
    if (timeDiff > 0) {
      totalDistance += distance;
      totalTime += timeDiff / 1000;
    }
  }
  const avgSpeed = totalTime > 0 ? totalDistance / totalTime : 0;
  if (avgSpeed > 5) {
    anomalies.push({ type: 'HIGH_SPEED', value: avgSpeed, threshold: 5 });
  }
  const distances = [];
  for (let i = 1; i < locations.length; i++) {
    distances.push(
      calculateDistance(
        locations[i - 1].lat,
        locations[i - 1].lng,
        locations[i].lat,
        locations[i].lng
      )
    );
  }
  const avgDistance = distances.reduce((a, b) => a + b, 0) / distances.length;
  const variance = distances.reduce((s, d) => s + (d - avgDistance) ** 2, 0) / distances.length;
  if (variance > 10000) {
    anomalies.push({ type: 'ERRATIC_MOVEMENT', variance });
  }
  return anomalies;
}

module.exports = { calculateDistance, detectAnomalies };
