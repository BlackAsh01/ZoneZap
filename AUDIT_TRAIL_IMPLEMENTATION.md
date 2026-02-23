# Audit Trail Implementation

This document describes the audit trail implementation for reminders and location tracking in the ZoneZap application.

## Overview

All data operations in the application now include comprehensive audit fields to track:
- **Who** created/modified the data
- **When** the data was created/modified
- **What** type of operation was performed
- **For whom** the data was created (guardian-ward relationships)

## Reminder Audit Fields

### Data Model (`Reminder.kt`)

Each reminder now includes the following audit fields:

```kotlin
data class Reminder(
    val id: String = "",
    val userId: String = "", // Ward ID
    val title: String = "",
    val description: String = "",
    val scheduledTime: Timestamp? = null,
    val type: String = "GENERAL",
    val isCompleted: Boolean = false,
    
    // Audit fields
    val createdBy: String? = null,        // Guardian ID who created this reminder
    val guardianId: String? = null,      // Guardian ID (same as createdBy, kept for clarity)
    val wardId: String? = null,           // Ward ID (same as userId, kept for clarity)
    val createdAt: Timestamp? = null,     // When the reminder was created
    val updatedAt: Timestamp? = null,     // When the reminder was last updated
    val completedAt: Timestamp? = null,   // When the reminder was completed (if applicable)
    val deletedAt: Timestamp? = null     // When the reminder was deleted (soft delete)
)
```

### Firestore Document Structure

When a reminder is created, it includes:

```json
{
  "userId": "ward_user_id",
  "wardId": "ward_user_id",
  "title": "Take medication",
  "description": "Take morning medication",
  "scheduledTime": "2026-01-25T10:00:00Z",
  "type": "GUARDIAN_REMINDER",
  "isCompleted": false,
  "createdBy": "guardian_user_id",
  "guardianId": "guardian_user_id",
  "createdAt": "2026-01-25T08:00:00Z",
  "updatedAt": "2026-01-25T08:00:00Z",
  "completedAt": null,
  "deletedAt": null
}
```

### ReminderService Operations

#### Creating a Reminder

```kotlin
suspend fun createReminder(userId: String, reminder: Reminder, createdBy: String? = null): String {
    val now = Timestamp.now()
    val reminderData = hashMapOf<String, Any>(
        "userId" to userId,
        "wardId" to userId,
        "title" to reminder.title,
        "description" to reminder.description,
        "scheduledTime" to (reminder.scheduledTime ?: now),
        "type" to reminder.type,
        "isCompleted" to false,
        "createdAt" to now,
        "updatedAt" to now
    )
    
    if (createdBy != null) {
        reminderData["createdBy"] = createdBy
        reminderData["guardianId"] = createdBy
    }
    
    // ... save to Firestore
}
```

#### Updating a Reminder

```kotlin
suspend fun updateReminder(reminderId: String, updates: Map<String, Any>) {
    val updateData = hashMapOf<String, Any>(
        "updatedAt" to Timestamp.now()
    )
    updateData.putAll(updates)
    // ... update Firestore document
}
```

#### Completing a Reminder

```kotlin
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
```

#### Deleting a Reminder (Soft Delete)

```kotlin
suspend fun deleteReminder(reminderId: String) {
    val now = Timestamp.now()
    firestore.collection("reminders").document(reminderId).update(
        mapOf(
            "deletedAt" to now,
            "updatedAt" to now
        )
    ).await()
}
```

**Note:** Reminders are soft-deleted (marked with `deletedAt`) rather than actually deleted, preserving audit history.

## Location Tracking Audit

### Movement Logs Collection

Location data is stored in the `movement_logs` collection with the following structure:

```json
{
  "userId": "user_id",
  "latitude": 12.9716,
  "longitude": 77.5946,
  "timestamp": "2026-01-25T10:30:00Z",
  "speed": 1.4,
  "heading": 45.0,
  "accuracy": 10.5
}
```

### WardLocationService

The `WardLocationService` provides methods for guardians to access ward location data:

```kotlin
class WardLocationService {
    /**
     * Get the latest location for a ward
     */
    suspend fun getLatestWardLocation(wardId: String): LocationData?
    
    /**
     * Get recent location history for a ward
     */
    suspend fun getWardLocationHistory(wardId: String, limit: Int = 50): List<LocationData>
}
```

## Guardian Location Viewing

### UI Features

1. **Ward List with Location Indicators**
   - Each ward in the guardian's list shows their latest location status
   - Displays time since last location update (e.g., "📍 5 min ago")

2. **Ward Details Dialog**
   - Shows ward name and email
   - Displays latest location with coordinates and accuracy
   - Shows time since last update
   - Options to:
     - Add reminder for the ward
     - View location history

3. **Location History**
   - Shows up to 20 recent locations
   - Displays timestamp, coordinates, and accuracy for each entry

### Implementation

```kotlin
// In GuardianActivity.kt
private fun showWardDetailsDialog(wardId: String) {
    val ward = wardsAdapter.getCurrentList().find { it.id == wardId }
    
    lifecycleScope.launch {
        val location = wardLocationService.getLatestWardLocation(wardId)
        // Display location information in dialog
    }
}
```

## Firestore Security Rules

### Reminders Collection

Guardians can:
- **Create** reminders for their wards
- **Read** reminders created for their wards
- **Query** reminders (filtered by userId)

Users can:
- **Create** their own reminders
- **Read** their own reminders
- **Update/Delete** their own reminders

### Movement Logs Collection

Guardians can:
- **Read** movement logs for their wards
- **Query** movement logs (filtered by userId)

Users can:
- **Create** their own movement logs
- **Read** their own movement logs

## Audit Trail Queries

### Find All Reminders Created by a Guardian

```kotlin
val reminders = firestore.collection("reminders")
    .whereEqualTo("guardianId", guardianId)
    .whereEqualTo("deletedAt", null) // Exclude soft-deleted
    .orderBy("createdAt", Query.Direction.DESCENDING)
    .get()
    .await()
```

### Find All Reminders for a Ward

```kotlin
val reminders = firestore.collection("reminders")
    .whereEqualTo("wardId", wardId)
    .whereEqualTo("deletedAt", null) // Exclude soft-deleted
    .orderBy("createdAt", Query.Direction.DESCENDING)
    .get()
    .await()
```

### Find Completed Reminders with Completion Time

```kotlin
val completedReminders = firestore.collection("reminders")
    .whereEqualTo("wardId", wardId)
    .whereEqualTo("isCompleted", true)
    .where(FieldPath.documentId(), ">=", "")
    .orderBy(FieldPath.documentId())
    .orderBy("completedAt", Query.Direction.DESCENDING)
    .get()
    .await()
```

### Location History for Audit

```kotlin
val locationHistory = firestore.collection("movement_logs")
    .whereEqualTo("userId", wardId)
    .orderBy("timestamp", Query.Direction.DESCENDING)
    .limit(100)
    .get()
    .await()
```

## Benefits of Audit Trail

1. **Accountability**: Track who created/modified each reminder
2. **Compliance**: Maintain records for regulatory requirements
3. **Debugging**: Identify issues by tracking when changes occurred
4. **Analytics**: Analyze guardian activity and ward compliance
5. **History**: Preserve data even after deletion (soft delete)
6. **Transparency**: Users can see when guardians created reminders for them

## Best Practices

1. **Always include timestamps**: Every create/update operation should update `updatedAt`
2. **Use soft deletes**: Mark records as deleted rather than removing them
3. **Track creator**: Always record `createdBy`/`guardianId` when applicable
4. **Query efficiently**: Use Firestore indexes for common query patterns
5. **Preserve history**: Don't overwrite audit fields during updates

## Firestore Indexes Required

Ensure these composite indexes exist in `firestore.indexes.json`:

```json
{
  "indexes": [
    {
      "collectionGroup": "reminders",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "wardId", "order": "ASCENDING" },
        { "fieldPath": "createdAt", "order": "DESCENDING" }
      ]
    },
    {
      "collectionGroup": "reminders",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "guardianId", "order": "ASCENDING" },
        { "fieldPath": "createdAt", "order": "DESCENDING" }
      ]
    },
    {
      "collectionGroup": "movement_logs",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "userId", "order": "ASCENDING" },
        { "fieldPath": "timestamp", "order": "DESCENDING" }
      ]
    }
  ]
}
```

## Testing Audit Trail

### Test Scenarios

1. **Guardian creates reminder for ward**
   - Verify `createdBy`, `guardianId`, `wardId`, `createdAt` are set
   - Verify `updatedAt` equals `createdAt`

2. **User completes reminder**
   - Verify `completedAt` is set
   - Verify `updatedAt` is updated
   - Verify `isCompleted` is true

3. **Guardian views ward location**
   - Verify guardian can query `movement_logs` for their ward
   - Verify location data includes timestamp

4. **Soft delete reminder**
   - Verify `deletedAt` is set
   - Verify reminder is excluded from normal queries
   - Verify reminder still exists in Firestore

## Summary

The audit trail implementation ensures:
- ✅ All reminders include guardian, ward, and timestamp information
- ✅ All operations are timestamped (created, updated, completed, deleted)
- ✅ Guardians can view ward locations with full history
- ✅ Data is preserved through soft deletes
- ✅ Complete audit trail for compliance and debugging
