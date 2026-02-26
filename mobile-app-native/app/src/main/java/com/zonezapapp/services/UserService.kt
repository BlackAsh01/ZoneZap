package com.zonezapapp.services

import com.zonezapapp.api.ApiClient
import com.zonezapapp.api.AuthManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserService {
    private val api get() = ApiClient.api()

    suspend fun createOrUpdateUser(userId: String, email: String, type: String, name: String?) {
        withContext(Dispatchers.IO) {
            api.updateMe(buildMap {
                put("name", name ?: email.substringBefore("@"))
            })
        }
    }

    suspend fun getUser(userId: String): Map<String, Any>? {
        return try {
            withContext(Dispatchers.IO) {
                val u = api.getMe()
                mapOf(
                    "userId" to u.id,
                    "id" to u.id,
                    "email" to (u.email ?: ""),
                    "name" to (u.name ?: ""),
                    "type" to u.type,
                    "guardians" to (u.guardians ?: emptyList<String>()),
                    "wards" to (u.wards ?: emptyList<String>())
                )
            }
        } catch (_: Exception) {
            null
        }
    }

    suspend fun updateUserTimestamp(userId: String) {}

    /** Links the given ward (userId) to the current guardian. Throws on API error so caller can show the server message. */
    suspend fun addGuardianToUser(userId: String, guardianId: String): Boolean {
        withContext(Dispatchers.IO) {
            api.linkWard(mapOf("ward_id" to userId))
        }
        return true
    }

    suspend fun removeGuardianFromUser(userId: String, guardianId: String): Boolean {
        return false
    }

    suspend fun getAllGuardians(): List<Map<String, Any>> = emptyList()

    suspend fun getAllUsers(): List<Map<String, Any>> = emptyList()

    suspend fun getUserGuardians(userId: String): List<Map<String, Any>> = emptyList()

    suspend fun addWardToGuardian(guardianId: String, userId: String): Boolean =
        addGuardianToUser(userId, guardianId)

    suspend fun removeWardFromGuardian(guardianId: String, userId: String): Boolean =
        removeGuardianFromUser(userId, guardianId)

    suspend fun findUserByEmail(email: String): Map<String, Any>? {
        return try {
            withContext(Dispatchers.IO) {
                val u = api.getUserByEmail(email)
                mapOf("userId" to u.id, "name" to (u.name ?: ""), "email" to (u.email ?: ""))
            }
        } catch (_: Exception) {
            null
        }
    }
}
