package com.zonezapapp.services

import com.google.firebase.Timestamp
import com.zonezapapp.config.FirebaseConfig
import com.zonezapapp.data.LocationData
import kotlinx.coroutines.tasks.await

/**
 * Service for guardians to fetch ward locations from movement_logs
 */
class WardLocationService {
    private val firestore = FirebaseConfig.firestore

    /**
     * Get the latest location for a ward (user)
     * @param wardId The user ID (ward) whose location to fetch
     * @return LocationData with the latest location, or null if no location found
     */
    suspend fun getLatestWardLocation(wardId: String): LocationData? {
        return try {
            val snapshot = firestore.collection("movement_logs")
                .whereEqualTo("userId", wardId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            if (snapshot.isEmpty) {
                return null
            }

            val doc = snapshot.documents.first()
            val data = doc.data ?: return null

            LocationData(
                latitude = (data["latitude"] as? Number)?.toDouble() ?: 0.0,
                longitude = (data["longitude"] as? Number)?.toDouble() ?: 0.0,
                accuracy = ((data["accuracy"] as? Number)?.toFloat()) ?: 0f,
                speed = ((data["speed"] as? Number)?.toFloat()) ?: 0f,
                heading = ((data["heading"] as? Number)?.toFloat()) ?: 0f,
                timestamp = (data["timestamp"] as? Timestamp)?.toDate()?.time ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            android.util.Log.e("WardLocationService", "Error fetching ward location", e)
            null
        }
    }

    /**
     * Get recent location history for a ward
     * @param wardId The user ID (ward) whose location history to fetch
     * @param limit Maximum number of locations to return (default: 50)
     * @return List of LocationData ordered by timestamp (most recent first)
     */
    suspend fun getWardLocationHistory(wardId: String, limit: Int = 50): List<LocationData> {
        return try {
            val snapshot = firestore.collection("movement_logs")
                .whereEqualTo("userId", wardId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                LocationData(
                    latitude = (data["latitude"] as? Number)?.toDouble() ?: 0.0,
                    longitude = (data["longitude"] as? Number)?.toDouble() ?: 0.0,
                    accuracy = ((data["accuracy"] as? Number)?.toFloat()) ?: 0f,
                    speed = ((data["speed"] as? Number)?.toFloat()) ?: 0f,
                    heading = ((data["heading"] as? Number)?.toFloat()) ?: 0f,
                    timestamp = (data["timestamp"] as? Timestamp)?.toDate()?.time ?: System.currentTimeMillis()
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("WardLocationService", "Error fetching ward location history", e)
            emptyList()
        }
    }
}
