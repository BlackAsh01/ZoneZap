package com.zonezapapp.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot

data class EmergencyAlert(
    val id: String = "",
    val userId: String = "",
    val alertType: String = "PANIC",
    val level: String = "CRITICAL",
    val location: LocationData? = null,
    val timestamp: Timestamp? = null,
    val status: String = "ACTIVE",
    val message: String = ""
) {
    companion object {
        fun fromDocument(doc: DocumentSnapshot): EmergencyAlert {
            val locationData = doc.get("location") as? Map<*, *>
            return EmergencyAlert(
                id = doc.id,
                userId = doc.getString("userId") ?: "",
                alertType = doc.getString("alertType") ?: "PANIC",
                level = doc.getString("level") ?: "CRITICAL",
                location = locationData?.let {
                    LocationData(
                        latitude = (it["latitude"] as? Number)?.toDouble() ?: 0.0,
                        longitude = (it["longitude"] as? Number)?.toDouble() ?: 0.0,
                        accuracy = (it["accuracy"] as? Number)?.toFloat() ?: 0f
                    )
                },
                timestamp = doc.getTimestamp("timestamp"),
                status = doc.getString("status") ?: "ACTIVE",
                message = doc.getString("message") ?: ""
            )
        }
    }
}
