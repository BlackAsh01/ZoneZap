package com.zonezapapp.services

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import com.zonezapapp.config.FirebaseConfig
import com.zonezapapp.data.Reminder
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ReminderService {
    private val firestore = FirebaseConfig.firestore

    suspend fun getUserReminders(userId: String): List<Reminder> {
        val snapshot = firestore.collection("reminders")
            .whereEqualTo("userId", userId)
            .whereEqualTo("isCompleted", false)
            .get()
            .await()

        return snapshot.documents.map { Reminder.fromDocument(it) }
    }

    fun getUserRemindersFlow(userId: String): Flow<List<Reminder>> = callbackFlow {
        val listenerRegistration: ListenerRegistration = firestore.collection("reminders")
            .whereEqualTo("userId", userId)
            .whereEqualTo("isCompleted", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.documents.map { Reminder.fromDocument(it) })
                }
            }
        
        awaitClose {
            listenerRegistration.remove()
        }
    }

    suspend fun createReminder(userId: String, reminder: Reminder, createdBy: String? = null): String {
        val now = Timestamp.now()
        val reminderData = hashMapOf<String, Any>(
            "userId" to userId,
            "title" to reminder.title,
            "description" to reminder.description,
            "scheduledTime" to (reminder.scheduledTime ?: now),
            "type" to reminder.type,
            "isCompleted" to false,
            // Audit fields
            "createdAt" to now,
            "updatedAt" to now,
            "wardId" to userId // Explicit ward ID for clarity
        )
        
        // Add guardian/creator fields if provided (guardian creating reminder for ward)
        if (createdBy != null) {
            reminderData["createdBy"] = createdBy
            reminderData["guardianId"] = createdBy
        }

        val docRef = firestore.collection("reminders").add(reminderData).await()
        return docRef.id
    }
    
    /**
     * Create a reminder for a ward (called by guardian)
     * @param wardId The user ID (ward) for whom the reminder is created
     * @param reminder The reminder data
     * @param guardianId The guardian ID creating the reminder
     */
    suspend fun createReminderForWard(wardId: String, reminder: Reminder, guardianId: String): String {
        return createReminder(wardId, reminder, guardianId)
    }
    
    /**
     * Update a reminder with audit tracking
     */
    suspend fun updateReminder(reminderId: String, updates: Map<String, Any>) {
        val updateData = hashMapOf<String, Any>(
            "updatedAt" to Timestamp.now()
        )
        updateData.putAll(updates)
        firestore.collection("reminders").document(reminderId).update(updateData).await()
    }

    suspend fun completeReminder(reminderId: String) {
        val now = Timestamp.now()
        firestore.collection("reminders").document(reminderId).update(
            mapOf(
                "isCompleted" to true,
                "completedAt" to now,
                "updatedAt" to now
            )
        ).await()
    }

    suspend fun deleteReminder(reminderId: String) {
        // Soft delete: mark as deleted instead of actually deleting
        val now = Timestamp.now()
        firestore.collection("reminders").document(reminderId).update(
            mapOf(
                "deletedAt" to now,
                "updatedAt" to now
            )
        ).await()
    }
}
