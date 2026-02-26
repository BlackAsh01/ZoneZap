package com.zonezapapp.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.zonezapapp.R
import com.zonezapapp.api.ApiClient
import com.zonezapapp.api.AuthManager
import com.zonezapapp.api.LoginRequest
import com.zonezapapp.api.RegisterRequest
import com.zonezapapp.ui.guardian.GuardianActivity
import com.zonezapapp.ui.home.HomeActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ZoneZapLogin"
    }
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var loginButton: MaterialButton
    private lateinit var signUpButton: MaterialButton
    private lateinit var userModeButton: MaterialButton
    private lateinit var guardianModeButton: MaterialButton

    private var isGuardianMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        ApiClient.init(applicationContext)

        val isFromLogout = intent.getBooleanExtra("from_logout", false) ||
                (intent.flags and Intent.FLAG_ACTIVITY_CLEAR_TASK != 0)

        if (AuthManager.isLoggedIn() && !isFromLogout) {
            when (AuthManager.getUserType()) {
                "guardian" -> startActivity(Intent(this, GuardianActivity::class.java))
                else -> startActivity(Intent(this, HomeActivity::class.java))
            }
            finish()
            return
        }

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        signUpButton = findViewById(R.id.signUpButton)
        userModeButton = findViewById(R.id.userModeButton)
        guardianModeButton = findViewById(R.id.guardianModeButton)
        updateModeSelection(false)

        userModeButton.setOnClickListener { isGuardianMode = false; updateModeSelection(false) }
        guardianModeButton.setOnClickListener { isGuardianMode = true; updateModeSelection(true) }
        loginButton.setOnClickListener { handleLogin() }
        signUpButton.setOnClickListener { handleSignUp() }
    }

    private fun handleLogin() {
        val email = emailEditText.text?.toString()?.trim() ?: ""
        val password = passwordEditText.text?.toString() ?: ""
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
            return
        }
        loginButton.isEnabled = false
        signUpButton.isEnabled = false
        lifecycleScope.launch {
            try {
                val res = withContext(Dispatchers.IO) {
                    ApiClient.api().login(LoginRequest(email = email, password = password))
                }
                AuthManager.setAuth(res.token, res.user)
                Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()
                if (res.user.type == "guardian") {
                    startActivity(Intent(this@LoginActivity, GuardianActivity::class.java))
                } else {
                    startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                }
                finish()
            } catch (e: HttpException) {
                logSignupLoginError("Login", e)
                val body = e.response()?.errorBody()?.string()
                val msg = when (e.code()) {
                    401 -> "Invalid email or password."
                    404 -> "Account not found or wrong API URL. Check build.gradle API_BASE_URL matches your Vercel URL."
                    else -> parseServerError(body) ?: sanitizeErrorMessage(body, "Login failed (${e.code()}).")
                }
                Toast.makeText(this@LoginActivity, msg, Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Log.e(TAG, "Login error", e)
                Toast.makeText(this@LoginActivity, "Login failed: ${sanitizeErrorMessage(e.message, "Check your connection.")}", Toast.LENGTH_LONG).show()
            } finally {
                loginButton.isEnabled = true
                signUpButton.isEnabled = true
            }
        }
    }

    private fun handleSignUp() {
        val email = emailEditText.text?.toString()?.trim() ?: ""
        val password = passwordEditText.text?.toString() ?: ""
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
            return
        }
        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }
        loginButton.isEnabled = false
        signUpButton.isEnabled = false
        lifecycleScope.launch {
            try {
                val type = if (isGuardianMode) "guardian" else "user"
                val res = withContext(Dispatchers.IO) {
                    ApiClient.api().register(RegisterRequest(
                        email = email,
                        password = password,
                        name = email.substringBefore("@"),
                        type = type
                    ))
                }
                AuthManager.setAuth(res.token, res.user)
                Toast.makeText(this@LoginActivity, "Account created successfully!", Toast.LENGTH_SHORT).show()
                if (res.user.type == "guardian") {
                    startActivity(Intent(this@LoginActivity, GuardianActivity::class.java))
                } else {
                    startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                }
                finish()
            } catch (e: HttpException) {
                logSignupLoginError("Signup", e)
                val body = e.response()?.errorBody()?.string()
                val msg = when (e.code()) {
                    401 -> "Vercel is blocking the request. In Vercel: Project → Settings → Deployment Protection → turn OFF for Production (or use 'Only Preview' so production is open)."
                    409 -> "An account with this email already exists."
                    404 -> "API URL not found. In app build.gradle set API_BASE_URL to your exact Vercel URL and rebuild."
                    405 -> "Server received GET instead of POST. Use exact Vercel URL in build.gradle (no trailing slash), then rebuild."
                    400 -> parseServerError(body) ?: "Invalid request. Check email, password (min 6 chars), and type."
                    500 -> parseServerError(body) ?: "Server error. Try again later."
                    else -> parseServerError(body) ?: "Sign up failed (${e.code()})."
                }
                Toast.makeText(this@LoginActivity, msg, Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Log.e(TAG, "Signup error", e)
                val detail = when {
                    e.message.isNullOrBlank() -> "Check your connection and API URL."
                    else -> sanitizeErrorMessage(e.message, "Check your connection.")
                }
                Toast.makeText(this@LoginActivity, "Sign up failed: $detail", Toast.LENGTH_LONG).show()
            } finally {
                loginButton.isEnabled = true
                signUpButton.isEnabled = true
            }
        }
    }

    private fun logSignupLoginError(operation: String, e: retrofit2.HttpException) {
        val url = e.response()?.raw()?.request?.url?.toString() ?: "unknown"
        val code = e.code()
        val body = e.response()?.errorBody()?.string()?.take(150) ?: ""
        Log.e(TAG, "$operation failed: code=$code url=$url body=${body.take(80)}...")
    }

    /** Parse API JSON error body: { "error": "msg" } or { "message": "msg" }. */
    private fun parseServerError(body: String?): String? {
        if (body.isNullOrBlank()) return null
        val trimmed = body.trim()
        if (!trimmed.startsWith("{")) return null
        return try {
            val json = org.json.JSONObject(trimmed)
            json.optString("error").takeIf { it.isNotEmpty() }
                ?: json.optString("message").takeIf { it.isNotEmpty() }
        } catch (_: Exception) {
            null
        }
    }

    /** Never show raw HTML (e.g. 404/500 page) to the user. */
    private fun sanitizeErrorMessage(raw: String?, fallback: String): String {
        if (raw.isNullOrBlank()) return fallback
        val trimmed = raw.trim()
        if (trimmed.startsWith("<", ignoreCase = true) ||
            trimmed.contains("<!DOCTYPE", ignoreCase = true) ||
            trimmed.contains("<html", ignoreCase = true)) {
            return "Server returned a web page instead of API. Set API_BASE_URL in app build.gradle to your exact Vercel URL and rebuild."
        }
        if (trimmed.startsWith("{")) return parseServerError(trimmed) ?: fallback
        return trimmed.take(200)
    }

    private fun updateModeSelection(isGuardian: Boolean) {
        if (isGuardian) {
            userModeButton.alpha = 0.5f
            guardianModeButton.alpha = 1.0f
            userModeButton.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            guardianModeButton.setBackgroundColor(getColor(R.color.primary_blue))
            guardianModeButton.setTextColor(getColor(R.color.white))
            userModeButton.setTextColor(getColor(R.color.primary_blue))
        } else {
            userModeButton.alpha = 1.0f
            guardianModeButton.alpha = 0.5f
            userModeButton.setBackgroundColor(getColor(R.color.primary_blue))
            guardianModeButton.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            userModeButton.setTextColor(getColor(R.color.white))
            guardianModeButton.setTextColor(getColor(R.color.primary_blue))
        }
    }
}
