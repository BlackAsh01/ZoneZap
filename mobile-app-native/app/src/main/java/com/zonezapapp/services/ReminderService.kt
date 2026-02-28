package com.zonezapapp.services

import com.google.firebase.Timestamp
import com.zonezapapp.api.ApiClient
import com.zonezapapp.api.CreateReminderRequest
import com.zonezapapp.api.ReminderResponse
import com.zonezapapp.data.Reminder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReminderService {
    private val api get() = ApiClient.api()

    private fun parseIso(s: String?): Timestamp? {
        if (s == null) return null
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            format.parse(s)?.let { Timestamp(it) }
        } catch (_: Exception) {
            try {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).parse(s)?.let { Timestamp(it) }
            } catch (_: Exception) { null }
        }
    }

    private fun ReminderResponse.toReminder(): Reminder = Reminder(
        id = id,
        userId = userId,
        title = title,
        description = description ?: "",
        scheduledTime = parseIso(scheduledTime),
        type = type ?: "GENERAL",
        isCompleted = isCompleted,
        createdBy = createdBy,
        guardianId = createdBy,
        wardId = userId,
        createdAt = parseIso(createdAt),
        updatedAt = parseIso(createdAt),
        completedAt = parseIso(completedAt)
    )

    suspend fun getUserReminders(userId: String): List<Reminder> {
        val list = withContext(Dispatchers.IO) { api.getReminders(userId) }
        return list.map { it.toReminder() }.filter { !it.isCompleted }
    }

    fun getUserRemindersFlow(userId: String) = flow {
        emit(getUserReminders(userId))
    }

    suspend fun createReminder(userId: String, reminder: Reminder, createdBy: String? = null): String {
        val body = CreateReminderRequest(
            userId = userId,
            title = reminder.title,
            description = reminder.description.ifEmpty { null },
            scheduledTime = reminder.scheduledTime?.toDate()?.time ?: System.currentTimeMillis(),
            type = reminder.type,
            createdBy = createdBy
        )
        val res = withContext(Dispatchers.IO) { api.createReminder(body) }
        return res.id
    }

    suspend fun createReminderForWard(wardId: String, reminder: Reminder, guardianId: String): String =
        createReminder(wardId, reminder, guardianId)

    suspend fun updateReminder(reminderId: String, updates: Map<String, Any>) {
        val body = mutableMapOf<String, Any>()
        updates["isCompleted"]?.let { body["is_completed"] = it }
        updates["title"]?.let { body["title"] = it }
        updates["description"]?.let { body["description"] = it }
        updates["scheduledTime"]?.let { body["scheduled_time"] = (it as? Timestamp)?.toDate()?.time ?: it }
        if (body.isNotEmpty()) withContext(Dispatchers.IO) { api.updateReminder(reminderId, body) }
    }

    suspend fun completeReminder(reminderId: String) {
        withContext(Dispatchers.IO) { api.updateReminder(reminderId, mapOf("is_completed" to true)) }
    }

    suspend fun deleteReminder(reminderId: String) {
        withContext(Dispatchers.IO) { api.deleteReminder(reminderId) }
    }
}
