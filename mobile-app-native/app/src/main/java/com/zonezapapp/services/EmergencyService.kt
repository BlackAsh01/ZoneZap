package com.zonezapapp.services

import com.google.firebase.Timestamp
import com.zonezapapp.api.ApiClient
import com.zonezapapp.api.AlertLocation
import com.zonezapapp.api.CreateAlertRequest
import com.zonezapapp.data.EmergencyAlert
import com.zonezapapp.data.LocationData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class EmergencyService {
    private fun parseCreatedAt(iso: String?): Timestamp? {
        if (iso.isNullOrBlank()) return null
        // Normalize: API returns e.g. "2026-02-28T20:35:23.82481+00:00" (variable fractional seconds, +00:00)
        var normalized = iso.replace("+00:00", "Z")
        // Reduce fractional seconds to exactly 3 digits (millis) for SimpleDateFormat
        normalized = Regex("""\.(\d+)Z$""").replace(normalized) { ".${it.groupValues[1].padEnd(3, '0').take(3)}Z" }
        return try {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).parse(normalized)?.let { Timestamp(it) }
        } catch (_: Exception) {
            try {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).parse(normalized)?.let { Timestamp(it) }
            } catch (_: Exception) { null }
        }
    }
    private val api get() = ApiClient.api()

    suspend fun sendEmergencyAlert(
        userId: String,
        alertType: String = "PANIC",
        location: LocationData?,
        additionalData: Map<String, Any> = emptyMap()
    ): String {
        val body = CreateAlertRequest(
            userId = userId,
            alertType = alertType,
            location = location?.let {
                AlertLocation(latitude = it.latitude, longitude = it.longitude, accuracy = it.accuracy)
            }
        )
        val res = withContext(Dispatchers.IO) { api.createAlert(body) }
        return res.id
    }

    suspend fun updateAlertStatus(alertId: String, status: String) {
        withContext(Dispatchers.IO) {
            api.updateAlert(alertId, mapOf("status" to status))
        }
    }

    suspend fun getActiveAlerts(userId: String): List<EmergencyAlert> {
        val list = withContext(Dispatchers.IO) {
            api.getAlerts(userId = userId, status = "ACTIVE")
        }
        return list.map { r ->
            EmergencyAlert(
                id = r.id,
                userId = r.userId,
                alertType = r.alertType ?: "PANIC",
                level = r.level ?: "CRITICAL",
                location = if (r.latitude != null && r.longitude != null) LocationData(r.latitude, r.longitude, 0f, 0f, 0f) else null,
                timestamp = parseCreatedAt(r.createdAt),
                status = r.status ?: "ACTIVE",
                message = ""
            )
        }
    }

    suspend fun getAlertsForWards(wardIds: List<String>): List<EmergencyAlert> {
        val all = mutableListOf<EmergencyAlert>()
        for (wardId in wardIds) {
            all.addAll(getActiveAlerts(wardId))
        }
        return all.sortedByDescending { it.id }
    }
}
