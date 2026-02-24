package com.zonezapapp.api

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

object AuthManager {
    private const val PREFS = "zonezap_auth"
    private const val KEY_TOKEN = "token"
    private const val KEY_USER = "user"

    private var prefs: SharedPreferences? = null
    private var cachedUser: ApiUser? = null

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            prefs?.getString(KEY_USER, null)?.let { cachedUser = Gson().fromJson(it, ApiUser::class.java) }
        }
    }

    fun setAuth(token: String, user: ApiUser) {
        prefs?.edit()?.apply {
            putString(KEY_TOKEN, token)
            putString(KEY_USER, Gson().toJson(user))
            apply()
        }
        cachedUser = user
    }

    fun getToken(): String? = prefs?.getString(KEY_TOKEN, null)
    fun getUserId(): String? = cachedUser?.id ?: prefs?.getString(KEY_USER, null)?.let { Gson().fromJson(it, ApiUser::class.java)?.id }
    fun getUser(): ApiUser? = cachedUser ?: prefs?.getString(KEY_USER, null)?.let { Gson().fromJson(it, ApiUser::class.java).also { cachedUser = it } }
    fun getUserType(): String? = getUser()?.type
    fun isLoggedIn(): Boolean = !getToken().isNullOrBlank()

    fun clear() {
        prefs?.edit()?.remove(KEY_TOKEN)?.remove(KEY_USER)?.apply()
        cachedUser = null
    }
}
