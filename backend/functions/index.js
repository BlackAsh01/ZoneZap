const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

/**
 * Triggered when a new emergency alert is created
 */
exports.onEmergencyAlert = functions.firestore
  .document("alerts/{alertId}")
  .onCreate(async (snap, context) => {
    const alertData = snap.data();
    const alertId = context.params.alertId;

    console.log("CRITICAL ALERT RECEIVED:", alertId, alertData);

    try {
      // Get user information
      const userDoc = await admin
        .firestore()
        .collection("users")
        .doc(alertData.userId)
        .get();

      if (!userDoc.exists) {
        console.error("User not found:", alertData.userId);
        return;
      }

      const userData = userDoc.data();
      const guardians = userData.guardians || [];

      // Send notifications to all guardians
      const notificationPromises = guardians.map(async (guardianId) => {
        const guardianDoc = await admin
          .firestore()
          .collection("users")
          .doc(guardianId)
          .get();

        if (guardianDoc.exists) {
          const guardianData = guardianDoc.data();
          const fcmToken = guardianData.fcmToken;

          if (fcmToken) {
            const message = {
              notification: {
                title: `🚨 Emergency Alert: ${alertData.alertType}`,
                body: `${userData.name || "User"} needs immediate assistance`,
              },
              data: {
                alertId: alertId,
                userId: alertData.userId,
                alertType: alertData.alertType,
                latitude: alertData.location?.latitude?.toString() || "",
                longitude: alertData.location?.longitude?.toString() || "",
                timestamp: alertData.timestamp?.toMillis().toString() || "",
              },
              token: fcmToken,
              android: {
                priority: "high",
              },
              apns: {
                headers: {
                  "apns-priority": "10",
                },
              },
            };

            try {
              await admin.messaging().send(message);
              console.log("Notification sent to guardian:", guardianId);
            } catch (error) {
              console.error("Error sending notification:", error);
            }
          }
        }
      });

      await Promise.all(notificationPromises);

      // Log the alert for analytics
      await admin.firestore().collection("alert_logs").add({
        alertId: alertId,
        userId: alertData.userId,
        alertType: alertData.alertType,
        timestamp: admin.firestore.FieldValue.serverTimestamp(),
        guardiansNotified: guardians.length,
      });

      return { success: true, guardiansNotified: guardians.length };
    } catch (error) {
      console.error("Error processing emergency alert:", error);
      return { success: false, error: error.message };
    }
  });

/**
 * Monitor location logs for anomaly detection
 */
exports.analyzeLocationPatterns = functions.firestore
  .document("movement_logs/{logId}")
  .onCreate(async (snap, context) => {
    const logData = snap.data();
    const userId = logData.userId;

    try {
      // Get recent movement logs for this user (last 30 entries)
      const recentLogs = await admin
        .firestore()
        .collection("movement_logs")
        .where("userId", "==", userId)
        .orderBy("timestamp", "desc")
        .limit(30)
        .get();

      if (recentLogs.size < 10) {
        // Not enough data for analysis
        return;
      }

      const locations = recentLogs.docs.map((doc) => ({
        lat: doc.data().latitude,
        lng: doc.data().longitude,
        timestamp: doc.data().timestamp,
      }));

      // Simple anomaly detection: check for rapid movement or unusual patterns
      const anomalies = detectAnomalies(locations);

      if (anomalies.length > 0) {
        // Create a wandering alert
        await admin.firestore().collection("alerts").add({
          userId: userId,
          alertType: "WANDERING",
          level: "MEDIUM",
          location: {
            latitude: logData.latitude,
            longitude: logData.longitude,
          },
          timestamp: admin.firestore.FieldValue.serverTimestamp(),
          status: "ACTIVE",
          anomalies: anomalies,
        });
      }
    } catch (error) {
      console.error("Error analyzing location patterns:", error);
    }
  });

/**
 * Simple anomaly detection algorithm
 */
function detectAnomalies(locations) {
  const anomalies = [];

  if (locations.length < 2) return anomalies;

  // Calculate average speed
  let totalDistance = 0;
  let totalTime = 0;

  for (let i = 1; i < locations.length; i++) {
    const prev = locations[i - 1];
    const curr = locations[i];

    const distance = calculateDistance(
      prev.lat,
      prev.lng,
      curr.lat,
      curr.lng
    );

    const timeDiff = curr.timestamp.toMillis() - prev.timestamp.toMillis();
    const speed = distance / (timeDiff / 1000); // m/s

    totalDistance += distance;
    totalTime += timeDiff;
  }

  const avgSpeed = totalDistance / (totalTime / 1000);

  // Flag if average speed exceeds 5 m/s (18 km/h) - unusual for walking
  if (avgSpeed > 5) {
    anomalies.push({
      type: "HIGH_SPEED",
      value: avgSpeed,
      threshold: 5,
    });
  }

  // Check for erratic movement patterns
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
  const variance =
    distances.reduce((sum, d) => sum + Math.pow(d - avgDistance, 2), 0) /
    distances.length;

  // High variance indicates erratic movement
  if (variance > 10000) {
    anomalies.push({
      type: "ERRATIC_MOVEMENT",
      variance: variance,
    });
  }

  return anomalies;
}

/**
 * Calculate distance between two coordinates (Haversine formula)
 */
function calculateDistance(lat1, lon1, lat2, lon2) {
  const R = 6371000; // Earth's radius in meters
  const dLat = toRadians(lat2 - lat1);
  const dLon = toRadians(lon2 - lon1);

  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(toRadians(lat1)) *
      Math.cos(toRadians(lat2)) *
      Math.sin(dLon / 2) *
      Math.sin(dLon / 2);

  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c;
}

function toRadians(degrees) {
  return degrees * (Math.PI / 180);
}

/**
 * Scheduled function to check for overdue reminders
 */
exports.checkOverdueReminders = functions.pubsub
  .schedule("every 5 minutes")
  .onRun(async (context) => {
    const now = admin.firestore.Timestamp.now();

    try {
      const overdueReminders = await admin
        .firestore()
        .collection("reminders")
        .where("isCompleted", "==", false)
        .where("scheduledTime", "<=", now)
        .get();

      const notificationPromises = overdueReminders.docs.map(async (doc) => {
        const reminder = doc.data();
        const userDoc = await admin
          .firestore()
          .collection("users")
          .doc(reminder.userId)
          .get();

        if (userDoc.exists) {
          const userData = userDoc.data();
          const fcmToken = userData.fcmToken;

          if (fcmToken) {
            const message = {
              notification: {
                title: "⏰ Reminder: " + reminder.title,
                body: reminder.description || "You have a scheduled reminder",
              },
              token: fcmToken,
            };

            try {
              await admin.messaging().send(message);
            } catch (error) {
              console.error("Error sending reminder notification:", error);
            }
          }
        }
      });

      await Promise.all(notificationPromises);
      console.log(`Processed ${overdueReminders.size} overdue reminders`);
    } catch (error) {
      console.error("Error checking overdue reminders:", error);
    }
  });

