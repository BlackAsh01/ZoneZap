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
import com.zonezapapp.api.RegisterRequest
import com.zonezapapp.ui.guardian.GuardianActivity
import com.zonezapapp.ui.home.HomeActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class SignUpActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ZoneZapSignUp"
    }

    private lateinit var nameEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var signUpButton: MaterialButton
    private lateinit var loginButton: MaterialButton
    private lateinit var userModeButton: MaterialButton
    private lateinit var guardianModeButton: MaterialButton

    private var isGuardianMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        ApiClient.init(applicationContext)
        setSupportActionBar(findViewById(R.id.toolbar))

        nameEditText = findViewById(R.id.nameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        signUpButton = findViewById(R.id.signUpButton)
        loginButton = findViewById(R.id.loginButton)
        userModeButton = findViewById(R.id.userModeButton)
        guardianModeButton = findViewById(R.id.guardianModeButton)
        updateModeSelection(false)

        userModeButton.setOnClickListener { isGuardianMode = false; updateModeSelection(false) }
        guardianModeButton.setOnClickListener { isGuardianMode = true; updateModeSelection(true) }
        signUpButton.setOnClickListener { handleSignUp() }
        loginButton.setOnClickListener { finish() }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun handleSignUp() {
        val name = nameEditText.text?.toString()?.trim() ?: ""
        val email = emailEditText.text?.toString()?.trim() ?: ""
        val password = passwordEditText.text?.toString() ?: ""
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter name, email and password", Toast.LENGTH_SHORT).show()
            return
        }
        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }
        signUpButton.isEnabled = false
        lifecycleScope.launch {
            try {
                val type = if (isGuardianMode) "guardian" else "user"
                val res = withContext(Dispatchers.IO) {
                    ApiClient.api().register(RegisterRequest(
                        email = email,
                        password = password,
                        name = name,
                        type = type
                    ))
                }
                AuthManager.setAuth(res.token, res.user)
                Toast.makeText(this@SignUpActivity, "Account created successfully!", Toast.LENGTH_SHORT).show()
                if (res.user.type == "guardian") {
                    startActivity(Intent(this@SignUpActivity, GuardianActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                } else {
                    startActivity(Intent(this@SignUpActivity, HomeActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                }
                finish()
            } catch (e: HttpException) {
                val body = e.response()?.errorBody()?.string()
                val msg = when (e.code()) {
                    401 -> "Vercel is blocking the request. Check Deployment Protection settings."
                    409 -> "An account with this email already exists."
                    404 -> "API URL not found. Check build.gradle API_BASE_URL."
                    405 -> "Server received wrong method. Check API_BASE_URL."
                    400 -> parseServerError(body) ?: "Invalid request. Check email, password (min 6 chars), and type."
                    500 -> parseServerError(body) ?: "Server error. Try again later."
                    else -> parseServerError(body) ?: "Sign up failed (${e.code()})."
                }
                Toast.makeText(this@SignUpActivity, msg, Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Log.e(TAG, "Signup error", e)
                Toast.makeText(this@SignUpActivity, "Sign up failed: ${e.message ?: "Check your connection."}", Toast.LENGTH_LONG).show()
            } finally {
                signUpButton.isEnabled = true
            }
        }
    }

    private fun parseServerError(body: String?): String? {
        if (body.isNullOrBlank() || !body.trim().startsWith("{")) return null
        return try {
            org.json.JSONObject(body.trim()).optString("error").takeIf { it.isNotEmpty() }
                ?: org.json.JSONObject(body.trim()).optString("message").takeIf { it.isNotEmpty() }
        } catch (_: Exception) { null }
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
