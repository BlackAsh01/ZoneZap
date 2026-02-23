package com.zonezapapp.services

import com.google.firebase.Timestamp
import com.zonezapapp.config.FirebaseConfig
import kotlinx.coroutines.tasks.await

class UserService {
    private val firestore = FirebaseConfig.firestore

    /**
     * Create or update user document in Firestore
     * @param userId Firebase Auth user ID
     * @param email User email
     * @param type "user" or "guardian"
     * @param name Optional user name (defaults to email if not provided)
     */
    suspend fun createOrUpdateUser(
        userId: String,
        email: String,
        type: String,
        name: String? = null
    ) {
        try {
            // Check if user document already exists
            val existingDoc = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            val userData = hashMapOf<String, Any>(
                "userId" to userId,
                "email" to email,
                "type" to type,
                "name" to (name ?: email.substringBefore("@")),
                "updatedAt" to Timestamp.now()
            )

            if (!existingDoc.exists()) {
                // New user - set createdAt and initialize type-specific arrays
                userData["createdAt"] = Timestamp.now()
                
                when (type) {
                    "user" -> {
                        // Users have guardians array (empty initially)
                        userData["guardians"] = emptyList<String>()
                    }
                    "guardian" -> {
                        // Guardians have wards array (empty initially)
                        userData["wards"] = emptyList<String>()
                    }
                }
            } else {
                // Existing user - preserve createdAt and arrays
                val existingData = existingDoc.data ?: emptyMap()
                userData["createdAt"] = existingData["createdAt"] ?: Timestamp.now()
                
                // Preserve existing arrays if they exist
                if (existingData.containsKey("guardians")) {
                    userData["guardians"] = existingData["guardians"] ?: emptyList<String>()
                } else if (type == "user") {
                    userData["guardians"] = emptyList<String>()
                }
                
                if (existingData.containsKey("wards")) {
                    userData["wards"] = existingData["wards"] ?: emptyList<String>()
                } else if (type == "guardian") {
                    userData["wards"] = emptyList<String>()
                }
            }

            // Use set with merge to update if exists, create if not
            firestore.collection("users")
                .document(userId)
                .set(userData, com.google.firebase.firestore.SetOptions.merge())
                .await()

            android.util.Log.d("UserService", "User document created/updated: $userId (type: $type)")
        } catch (e: Exception) {
            android.util.Log.e("UserService", "Error creating/updating user document", e)
            throw e
        }
    }

    /**
     * Get user document from Firestore
     */
    suspend fun getUser(userId: String): Map<String, Any>? {
        return try {
            val doc = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            
            if (doc.exists()) {
                doc.data
            } else {
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("UserService", "Error getting user document", e)
            null
        }
    }

    /**
     * Update user's updatedAt timestamp
     */
    suspend fun updateUserTimestamp(userId: String) {
        try {
            firestore.collection("users")
                .document(userId)
                .update("updatedAt", Timestamp.now())
                .await()
        } catch (e: Exception) {
            android.util.Log.e("UserService", "Error updating user timestamp", e)
        }
    }

    /**
     * Add a guardian to a user's guardians list
     * Also adds the user to the guardian's wards list (bidirectional relationship)
     * @param userId The user ID who needs a guardian
     * @param guardianId The guardian ID to add
     * @return true if successful, false otherwise
     */
    suspend fun addGuardianToUser(userId: String, guardianId: String): Boolean {
        return try {
            // Verify both users exist and have correct types
            val userDoc = firestore.collection("users").document(userId).get().await()
            val guardianDoc = firestore.collection("users").document(guardianId).get().await()

            if (!userDoc.exists() || !guardianDoc.exists()) {
                android.util.Log.e("UserService", "User or guardian not found")
                return false
            }

            val userData = userDoc.data ?: emptyMap()
            val guardianData = guardianDoc.data ?: emptyMap()

            // Verify types
            if (userData["type"] != "user") {
                android.util.Log.e("UserService", "User $userId is not of type 'user'")
                return false
            }

            if (guardianData["type"] != "guardian") {
                android.util.Log.e("UserService", "Guardian $guardianId is not of type 'guardian'")
                return false
            }

            // Get current arrays
            val currentGuardians = (userData["guardians"] as? List<*>)?.map { it.toString() }?.toMutableList() ?: mutableListOf()
            val currentWards = (guardianData["wards"] as? List<*>)?.map { it.toString() }?.toMutableList() ?: mutableListOf()

            // Add if not already present
            if (!currentGuardians.contains(guardianId)) {
                currentGuardians.add(guardianId)
                firestore.collection("users").document(userId)
                    .update(
                        "guardians", currentGuardians,
                        "updatedAt", Timestamp.now()
                    )
                    .await()
            }

            if (!currentWards.contains(userId)) {
                currentWards.add(userId)
                firestore.collection("users").document(guardianId)
                    .update(
                        "wards", currentWards,
                        "updatedAt", Timestamp.now()
                    )
                    .await()
            }

            android.util.Log.d("UserService", "Guardian $guardianId added to user $userId")
            true
        } catch (e: Exception) {
            android.util.Log.e("UserService", "Error adding guardian to user", e)
            false
        }
    }

    /**
     * Remove a guardian from a user's guardians list
     * Also removes the user from the guardian's wards list
     * @param userId The user ID
     * @param guardianId The guardian ID to remove
     * @return true if successful, false otherwise
     */
    suspend fun removeGuardianFromUser(userId: String, guardianId: String): Boolean {
        return try {
            val userDoc = firestore.collection("users").document(userId).get().await()
            val guardianDoc = firestore.collection("users").document(guardianId).get().await()

            if (!userDoc.exists() || !guardianDoc.exists()) {
                return false
            }

            val userData = userDoc.data ?: emptyMap()
            val guardianData = guardianDoc.data ?: emptyMap()

            // Get current arrays and remove
            val currentGuardians = (userData["guardians"] as? List<*>)?.map { it.toString() }?.toMutableList() ?: mutableListOf()
            val currentWards = (guardianData["wards"] as? List<*>)?.map { it.toString() }?.toMutableList() ?: mutableListOf()

            currentGuardians.remove(guardianId)
            currentWards.remove(userId)

            // Update both documents
            firestore.collection("users").document(userId)
                .update(
                    "guardians", currentGuardians,
                    "updatedAt", Timestamp.now()
                )
                .await()

            firestore.collection("users").document(guardianId)
                .update(
                    "wards", currentWards,
                    "updatedAt", Timestamp.now()
                )
                .await()

            android.util.Log.d("UserService", "Guardian $guardianId removed from user $userId")
            true
        } catch (e: Exception) {
            android.util.Log.e("UserService", "Error removing guardian from user", e)
            false
        }
    }

    /**
     * Get all available guardians (users with type "guardian")
     */
    suspend fun getAllGuardians(): List<Map<String, Any>> {
        return try {
            val snapshot = firestore.collection("users")
                .whereEqualTo("type", "guardian")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.data?.let { data ->
                    mapOf(
                        "userId" to doc.id,
                        "name" to (data["name"] as? String ?: ""),
                        "email" to (data["email"] as? String ?: "")
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("UserService", "Error getting all guardians", e)
            emptyList()
        }
    }

    /**
     * Get all users (for guardians to select wards)
     */
    suspend fun getAllUsers(): List<Map<String, Any>> {
        return try {
            val snapshot = firestore.collection("users")
                .whereEqualTo("type", "user")
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.data?.let { data ->
                    mapOf(
                        "userId" to doc.id,
                        "name" to (data["name"] as? String ?: ""),
                        "email" to (data["email"] as? String ?: "")
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("UserService", "Error getting all users", e)
            emptyList()
        }
    }

    /**
     * Get guardians for a specific user
     */
    suspend fun getUserGuardians(userId: String): List<Map<String, Any>> {
        return try {
            val userDoc = firestore.collection("users").document(userId).get().await()
            if (!userDoc.exists()) {
                return emptyList()
            }

            val userData = userDoc.data ?: emptyMap()
            val guardianIds = (userData["guardians"] as? List<*>)?.map { it.toString() } ?: emptyList()

            if (guardianIds.isEmpty()) {
                return emptyList()
            }

            // Fetch guardian documents
            val guardians = mutableListOf<Map<String, Any>>()
            guardianIds.forEach { guardianId ->
                val guardianDoc = firestore.collection("users").document(guardianId).get().await()
                if (guardianDoc.exists()) {
                    val data = guardianDoc.data ?: emptyMap()
                    guardians.add(
                        mapOf(
                            "userId" to guardianId,
                            "name" to (data["name"] as? String ?: ""),
                            "email" to (data["email"] as? String ?: "")
                        )
                    )
                }
            }

            guardians
        } catch (e: Exception) {
            android.util.Log.e("UserService", "Error getting user guardians", e)
            emptyList()
        }
    }

    /**
     * Add a user (ward) to a guardian's wards list
     * Also adds the guardian to the user's guardians list (bidirectional relationship)
     * This is the same as addGuardianToUser but called from guardian's perspective
     * @param guardianId The guardian ID
     * @param userId The user ID to add as a ward
     * @return true if successful, false otherwise
     */
    suspend fun addWardToGuardian(guardianId: String, userId: String): Boolean {
        // This is the same as addGuardianToUser, just with reversed parameters
        return addGuardianToUser(userId, guardianId)
    }

    /**
     * Remove a user (ward) from a guardian's wards list
     * Also removes the guardian from the user's guardians list
     * @param guardianId The guardian ID
     * @param userId The user ID to remove
     * @return true if successful, false otherwise
     */
    suspend fun removeWardFromGuardian(guardianId: String, userId: String): Boolean {
        // This is the same as removeGuardianFromUser, just with reversed parameters
        return removeGuardianFromUser(userId, guardianId)
    }

    /**
     * Find user by email (for searching users to add as wards)
     */
    suspend fun findUserByEmail(email: String): Map<String, Any>? {
        return try {
            val snapshot = firestore.collection("users")
                .whereEqualTo("email", email)
                .whereEqualTo("type", "user")
                .limit(1)
                .get()
                .await()

            if (snapshot.documents.isNotEmpty()) {
                val doc = snapshot.documents[0]
                val data = doc.data ?: emptyMap()
                mapOf(
                    "userId" to doc.id,
                    "name" to (data["name"] as? String ?: ""),
                    "email" to (data["email"] as? String ?: "")
                )
            } else {
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("UserService", "Error finding user by email", e)
            null
        }
    }
}
