package com.zonezapapp.services

import com.zonezapapp.api.ApiClient
import com.zonezapapp.data.LocationData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class WardLocationService {
    private val api get() = ApiClient.api()

    private fun parseTimestamp(s: String?): Long {
        if (s == null) return System.currentTimeMillis()
        return try {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).parse(s)?.time ?: System.currentTimeMillis()
        } catch (_: Exception) {
            try { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).parse(s)?.time ?: System.currentTimeMillis() } catch (_: Exception) { System.currentTimeMillis() }
        }
    }

    suspend fun getLatestWardLocation(wardId: String): LocationData? {
        val list = withContext(Dispatchers.IO) { api.getMovementLogs(wardId, 1) }
        val r = list.firstOrNull() ?: return null
        return LocationData(
            latitude = r.latitude,
            longitude = r.longitude,
            accuracy = r.accuracy?.toFloat() ?: 0f,
            speed = r.speed?.toFloat() ?: 0f,
            heading = r.heading?.toFloat() ?: 0f,
            timestamp = parseTimestamp(r.timestamp)
        )
    }

    suspend fun getWardLocationHistory(wardId: String, limit: Int = 50): List<LocationData> {
        val list = withContext(Dispatchers.IO) { api.getMovementLogs(wardId, limit) }
        return list.map { r ->
            LocationData(
                latitude = r.latitude,
                longitude = r.longitude,
                accuracy = r.accuracy?.toFloat() ?: 0f,
                speed = r.speed?.toFloat() ?: 0f,
                heading = r.heading?.toFloat() ?: 0f,
                timestamp = parseTimestamp(r.timestamp)
            )
        }
    }
}
