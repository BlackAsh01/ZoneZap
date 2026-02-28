package com.zonezapapp.api

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface ZoneZapApi {
    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): LoginRegisterResponse

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): LoginRegisterResponse

    @GET("api/users/me")
    suspend fun getMe(): ApiUser

    @PATCH("api/users/me")
    suspend fun updateMe(@Body body: Map<String, Any>): ApiUser

    @GET("api/users/by-email")
    suspend fun getUserByEmail(@Query("email") email: String): ApiUser

    @POST("api/guardians/link")
    suspend fun linkWard(@Body body: LinkWardRequest): Map<String, Any>

    @GET("api/guardians/wards")
    suspend fun getGuardianWards(): List<WardInfo>

    /** Fallback when /api/guardians/wards is not available: get one ward's name/email by id (guardian auth). */
    @GET("api/users/{id}")
    suspend fun getWardUser(@Path("id") wardId: String): WardInfo

    @GET("api/alerts")
    suspend fun getAlerts(@Query("user_id") userId: String? = null, @Query("status") status: String? = null): List<AlertResponse>

    @POST("api/alerts")
    suspend fun createAlert(@Body body: CreateAlertRequest): AlertResponse

    @PATCH("api/alerts/{id}")
    suspend fun updateAlert(@Path("id") id: String, @Body body: Map<String, Any>): AlertResponse

    @GET("api/movement-logs")
    suspend fun getMovementLogs(@Query("user_id") userId: String?, @Query("limit") limit: Int? = null): List<MovementLogResponse>

    @POST("api/movement-logs")
    suspend fun createMovementLog(@Body body: MovementLogRequest): MovementLogResponse

    @GET("api/reminders")
    suspend fun getReminders(@Query("user_id") userId: String? = null): List<ReminderResponse>

    @POST("api/reminders")
    suspend fun createReminder(@Body body: CreateReminderRequest): ReminderResponse

    @PATCH("api/reminders/{id}")
    suspend fun updateReminder(@Path("id") id: String, @Body body: Map<String, Any>): ReminderResponse

    @DELETE("api/reminders/{id}")
    suspend fun deleteReminder(@Path("id") id: String): Map<String, Any>

    companion object {
        fun create(context: android.content.Context, baseUrl: String): ZoneZapApi {
            AuthManager.init(context)
            val authInterceptor = Interceptor { chain ->
                val token = AuthManager.getToken()
                val request = chain.request().newBuilder()
                    .addHeader("Accept", "application/json")
                if (!token.isNullOrBlank()) request.addHeader("Authorization", "Bearer $token")
                val req = chain.request()
                Log.d("ZoneZapApi", "Request: ${req.method} ${req.url}")
                chain.proceed(request.build())
            }
            val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
            val client = OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()
            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl.trimEnd('/') + "/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(ZoneZapApi::class.java)
        }
    }
}
