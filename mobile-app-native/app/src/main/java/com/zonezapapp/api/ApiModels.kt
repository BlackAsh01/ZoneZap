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
