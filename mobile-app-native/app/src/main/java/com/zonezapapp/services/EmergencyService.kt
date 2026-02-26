package com.zonezapapp.services

import com.zonezapapp.api.ApiClient
import com.zonezapapp.data.EmergencyAlert
import com.zonezapapp.data.LocationData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EmergencyService {
    private val api get() = ApiClient.api()

    suspend fun sendEmergencyAlert(
        userId: String,
        alertType: String = "PANIC",
        location: LocationData?,
        additionalData: Map<String, Any> = emptyMap()
    ): String {
        val body = mutableMapOf<String, Any>(
            "userId" to userId,
            "alertType" to alertType
        )
        location?.let {
            body["location"] = mapOf(
                "latitude" to it.latitude,
                "longitude" to it.longitude,
                "accuracy" to it.accuracy
            )
        }
        additionalData.forEach { (k, v) -> body[k] = v }
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
                timestamp = null,
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
