package com.zonezapapp.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot

data class Reminder(
    val id: String = "",
    val userId: String = "", // Ward ID
    val title: String = "",
    val description: String = "",
    val scheduledTime: Timestamp? = null,
    val type: String = "GENERAL",
    val isCompleted: Boolean = false,
    // Audit fields
    val createdBy: String? = null, // Guardian ID who created this reminder (null if created by user)
    val guardianId: String? = null, // Guardian ID (same as createdBy, kept for clarity)
    val wardId: String? = null, // Ward ID (same as userId, kept for clarity)
    val createdAt: Timestamp? = null, // When the reminder was created
    val updatedAt: Timestamp? = null, // When the reminder was last updated
    val completedAt: Timestamp? = null, // When the reminder was completed (if applicable)
    val deletedAt: Timestamp? = null // When the reminder was deleted (soft delete)
) {
    companion object {
        fun fromDocument(doc: DocumentSnapshot): Reminder {
            return Reminder(
                id = doc.id,
                userId = doc.getString("userId") ?: "",
                title = doc.getString("title") ?: "",
                description = doc.getString("description") ?: "",
                scheduledTime = doc.getTimestamp("scheduledTime"),
                type = doc.getString("type") ?: "GENERAL",
                isCompleted = doc.getBoolean("isCompleted") ?: false,
                createdBy = doc.getString("createdBy"),
                guardianId = doc.getString("guardianId"),
                wardId = doc.getString("wardId"),
                createdAt = doc.getTimestamp("createdAt"),
                updatedAt = doc.getTimestamp("updatedAt"),
                completedAt = doc.getTimestamp("completedAt"),
                deletedAt = doc.getTimestamp("deletedAt")
            )
        }
    }
}
