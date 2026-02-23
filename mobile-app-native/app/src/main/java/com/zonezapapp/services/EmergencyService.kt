package com.zonezapapp.services

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.zonezapapp.config.FirebaseConfig
import com.zonezapapp.data.EmergencyAlert
import com.zonezapapp.data.LocationData
import kotlinx.coroutines.tasks.await

class EmergencyService {
    private val firestore = FirebaseConfig.firestore

    suspend fun sendEmergencyAlert(
        userId: String,
        alertType: String = "PANIC",
        location: LocationData?,
        additionalData: Map<String, Any> = emptyMap()
    ): String {
        val alertData = hashMapOf<String, Any>(
            "userId" to userId,
            "alertType" to alertType,
            "level" to "CRITICAL",
            "timestamp" to Timestamp.now(),
            "status" to "ACTIVE"
        )

        location?.let {
            alertData["location"] = hashMapOf(
                "latitude" to it.latitude,
                "longitude" to it.longitude,
                "accuracy" to it.accuracy
            )
        }

        additionalData.forEach { (key, value) ->
            alertData[key] = value
        }

        val alertRef = firestore.collection("alerts").add(alertData).await()
        
        // Send notification to guardians (would typically use Cloud Functions)
        sendNotificationToGuardians(userId, alertType, location)
        
        return alertRef.id
    }

    private suspend fun sendNotificationToGuardians(
        userId: String,
        alertType: String,
        location: LocationData?
    ) {
        try {
            val userDoc = firestore.collection("users").document(userId).get().await()
            val guardians = userDoc.get("guardians") as? List<*> ?: emptyList<Any>()
            
            // In production, this would be handled by Cloud Functions
            // For now, we'll just log it
            guardians.forEach { guardianId ->
                // Cloud Function would send FCM notification here
            }
        } catch (e: Exception) {
            // Handle error
        }
    }

    suspend fun updateAlertStatus(alertId: String, status: String) {
        firestore.collection("alerts").document(alertId).update(
            mapOf(
                "status" to status,
                "resolvedAt" to Timestamp.now()
            )
        ).await()
    }

    suspend fun getActiveAlerts(userId: String): List<EmergencyAlert> {
        val snapshot = firestore.collection("alerts")
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", "ACTIVE")
            .orderBy("timestamp")
            .get()
            .await()

        return snapshot.documents.map { EmergencyAlert.fromDocument(it) }
    }
}
