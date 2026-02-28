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

        loginButton.setOnClickListener { handleLogin() }
        signUpButton.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
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

    private fun parseServerError(body: String?): String? {
        if (body.isNullOrBlank()) return null
        val trimmed = body.trim()
        if (!trimmed.startsWith("{")) return null
        return try {
            val json = org.json.JSONObject(trimmed)
            json.optString("error").takeIf { it.isNotEmpty() }
                ?: json.optString("message").takeIf { it.isNotEmpty() }
        } catch (_: Exception) { null }
    }

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
}
