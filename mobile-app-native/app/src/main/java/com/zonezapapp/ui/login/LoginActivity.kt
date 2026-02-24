package com.zonezapapp.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.zonezapapp.R
import com.zonezapapp.api.ApiClient
import com.zonezapapp.api.AuthManager
import com.zonezapapp.ui.guardian.GuardianActivity
import com.zonezapapp.ui.home.HomeActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class LoginActivity : AppCompatActivity() {
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
                    ApiClient.api().login(mapOf("email" to email, "password" to password))
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
                val msg = when (e.code()) {
                    401 -> "Invalid email or password."
                    404 -> "Account not found. Please sign up first."
                    else -> e.response()?.errorBody()?.string()?.let { b -> if (b.isNotEmpty()) b else "Login failed." } ?: "Login failed."
                }
                Toast.makeText(this@LoginActivity, msg, Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Login failed: ${e.message}", Toast.LENGTH_LONG).show()
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
                    ApiClient.api().register(mapOf(
                        "email" to email,
                        "password" to password,
                        "name" to email.substringBefore("@"),
                        "type" to type
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
                val msg = when (e.code()) {
                    409 -> "An account with this email already exists."
                    else -> e.response()?.errorBody()?.string() ?: "Sign up failed."
                }
                Toast.makeText(this@LoginActivity, msg, Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Sign up failed: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                loginButton.isEnabled = true
                signUpButton.isEnabled = true
            }
        }
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
