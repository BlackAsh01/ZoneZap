package com.zonezapapp.api

import com.google.gson.annotations.SerializedName

/** Request body for register; avoids Gson "parameter type must include a type var" with Map<String, Any>. */
data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String,
    val type: String
)

/** Request body for login. */
data class LoginRequest(
    val email: String,
    val password: String
)

/** Request body for POST api/guardians/link. Use ward_email or ward_id (server looks up by email if needed). */
data class LinkWardRequest(
    @SerializedName("ward_id") val wardId: String? = null,
    @SerializedName("ward_email") val wardEmail: String? = null
)

data class LoginRegisterResponse(
    val token: String,
    val user: ApiUser
)

data class ApiUser(
    val id: String,
    val email: String?,
    val name: String?,
    val type: String,
    val guardians: List<String>? = null,
    val wards: List<String>? = null,
    @SerializedName("fcm_token") val fcmToken: String? = null
)

/** Request body for POST api/alerts (emergency/panic). */
data class CreateAlertRequest(
    @SerializedName("userId") val userId: String,
    @SerializedName("alertType") val alertType: String = "PANIC",
    val location: AlertLocation?
)

data class AlertLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float = 0f
)

data class AlertResponse(
    val id: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("alert_type") val alertType: String,
    val level: String?,
    val latitude: Double?,
    val longitude: Double?,
    val status: String?,
    @SerializedName("created_at") val createdAt: String?
)

/** Request body for POST api/reminders (guardian creating for ward or user creating for self). */
data class CreateReminderRequest(
    @SerializedName("user_id") val userId: String,
    val title: String,
    val description: String? = null,
    @SerializedName("scheduled_time") val scheduledTime: Long,
    val type: String = "GENERAL",
    @SerializedName("created_by") val createdBy: String? = null
)

data class ReminderResponse(
    val id: String,
    @SerializedName("user_id") val userId: String,
    val title: String,
    val description: String?,
    @SerializedName("scheduled_time") val scheduledTime: String,
    val type: String?,
    @SerializedName("is_completed") val isCompleted: Boolean,
    @SerializedName("created_by") val createdBy: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("completed_at") val completedAt: String?
)

/** Ward summary returned by GET /api/guardians/wards */
data class WardInfo(
    val id: String,
    val name: String?,
    val email: String?
)

/** Request body for POST api/movement-logs */
data class MovementLogRequest(
    @SerializedName("userId") val userId: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val speed: Double,
    val heading: Double,
    val accuracy: Double
)

data class MovementLogResponse(
    val id: String,
    @SerializedName("user_id") val userId: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: String,
    val speed: Double?,
    val heading: Double?,
    val accuracy: Double?
)
