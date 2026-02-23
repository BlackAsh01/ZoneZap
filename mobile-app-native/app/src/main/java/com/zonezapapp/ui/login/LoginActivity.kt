package com.zonezapapp.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.zonezapapp.R
import com.zonezapapp.config.FirebaseConfig
import com.zonezapapp.services.UserService
import com.zonezapapp.ui.guardian.GuardianActivity
import com.zonezapapp.ui.home.HomeActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var loginButton: MaterialButton
    private lateinit var signUpButton: MaterialButton
    private lateinit var userModeButton: MaterialButton
    private lateinit var guardianModeButton: MaterialButton
    
    private var isGuardianMode = false
    private val userService = UserService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Check if this is coming from logout
        val isFromLogout = intent.getBooleanExtra("from_logout", false) || 
                          (intent.flags and Intent.FLAG_ACTIVITY_CLEAR_TASK != 0)
        
        // Check if user is already logged in
        // Skip auto-navigation if coming from logout (user should be signed out)
        val currentUser = FirebaseConfig.auth.currentUser
        if (currentUser != null && !isFromLogout) {
            // Check user type and navigate accordingly
            lifecycleScope.launch {
                try {
                    val userData = userService.getUser(currentUser.uid)
                    val userType = userData?.get("type") as? String ?: "user"
                    
                    if (userType == "guardian") {
                        startActivity(Intent(this@LoginActivity, GuardianActivity::class.java))
                    } else {
                        startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                    }
                    finish()
                } catch (e: Exception) {
                    // Default to HomeActivity if error
                    startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                    finish()
                }
            }
            return
        }
        
        // User is not logged in or coming from logout, show login screen

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        signUpButton = findViewById(R.id.signUpButton)
        userModeButton = findViewById(R.id.userModeButton)
        guardianModeButton = findViewById(R.id.guardianModeButton)

        // Set default mode to User
        updateModeSelection(false)

        userModeButton.setOnClickListener {
            isGuardianMode = false
            updateModeSelection(false)
        }

        guardianModeButton.setOnClickListener {
            isGuardianMode = true
            updateModeSelection(true)
        }

        loginButton.setOnClickListener {
            handleLogin()
        }

        signUpButton.setOnClickListener {
            handleSignUp()
        }
    }

    private fun handleLogin() {
        val email = emailEditText.text?.toString() ?: ""
        val password = passwordEditText.text?.toString() ?: ""

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
            return
        }

        android.util.Log.d("LoginActivity", "Attempting login for: $email")
        
        loginButton.isEnabled = false
        signUpButton.isEnabled = false

        FirebaseConfig.auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                loginButton.isEnabled = true
                signUpButton.isEnabled = true

                if (task.isSuccessful) {
                    val user = task.result?.user
                    val userId = user?.uid ?: return@addOnCompleteListener
                    
                    android.util.Log.d("LoginActivity", "Login successful! User ID: $userId")
                    android.util.Log.d("LoginActivity", "User email: ${user?.email}")
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    
                    // Check user type from Firestore and navigate accordingly
                    lifecycleScope.launch {
                        try {
                            var userData = userService.getUser(userId)
                            
                            // If user document doesn't exist, create it
                            if (userData == null) {
                                android.util.Log.d("LoginActivity", "User document not found, creating it...")
                                val userEmail = user?.email ?: email
                                val userType = if (isGuardianMode) "guardian" else "user"
                                userService.createOrUpdateUser(
                                    userId = userId,
                                    email = userEmail,
                                    type = userType,
                                    name = null
                                )
                                userData = userService.getUser(userId)
                            }
                            
                            val userType = userData?.get("type") as? String
                            
                            // If user type not found, use selected mode (for backward compatibility)
                            val finalType = userType ?: if (isGuardianMode) "guardian" else "user"
                            
                            if (finalType == "guardian") {
                                startActivity(Intent(this@LoginActivity, GuardianActivity::class.java))
                            } else {
                                startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                            }
                            finish()
                        } catch (e: Exception) {
                            android.util.Log.e("LoginActivity", "Error getting user type", e)
                            // Fallback to mode selection
                            if (isGuardianMode) {
                                startActivity(Intent(this@LoginActivity, GuardianActivity::class.java))
                            } else {
                                startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                            }
                            finish()
                        }
                    }
                } else {
                    val exception = task.exception
                    val errorMessage = when {
                        exception is com.google.firebase.auth.FirebaseAuthInvalidUserException -> {
                            "Account not found. Please sign up first."
                        }
                        exception is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> {
                            "Invalid email or password. Please try again."
                        }
                        exception is com.google.firebase.auth.FirebaseAuthUserCollisionException -> {
                            "An account with this email already exists."
                        }
                        else -> {
                            exception?.message ?: "Login failed. Please try again."
                        }
                    }
                    android.util.Log.e("LoginActivity", "Login failed: $errorMessage", exception)
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun handleSignUp() {
        val email = emailEditText.text?.toString() ?: ""
        val password = passwordEditText.text?.toString() ?: ""

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        android.util.Log.d("LoginActivity", "Attempting signup for: $email")
        
        loginButton.isEnabled = false
        signUpButton.isEnabled = false

        FirebaseConfig.auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                loginButton.isEnabled = true
                signUpButton.isEnabled = true

                if (task.isSuccessful) {
                    val user = task.result?.user
                    val userId = user?.uid ?: return@addOnCompleteListener
                    val userEmail = user?.email ?: email
                    
                    android.util.Log.d("LoginActivity", "Signup successful! User ID: $userId")
                    android.util.Log.d("LoginActivity", "User email: $userEmail")
                    
                    // Create user document in Firestore
                    lifecycleScope.launch {
                        try {
                            val userType = if (isGuardianMode) "guardian" else "user"
                            userService.createOrUpdateUser(
                                userId = userId,
                                email = userEmail,
                                type = userType,
                                name = null // Can be updated later
                            )
                            android.util.Log.d("LoginActivity", "User document created in Firestore (type: $userType)")
                        } catch (e: Exception) {
                            android.util.Log.e("LoginActivity", "Error creating user document", e)
                            // Continue anyway - user can still use the app
                        }
                    }
                    
                    Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                    
                    // Navigate based on user type
                    if (isGuardianMode) {
                        startActivity(Intent(this, GuardianActivity::class.java))
                    } else {
                        startActivity(Intent(this, HomeActivity::class.java))
                    }
                    finish()
                } else {
                    val error = task.exception?.message ?: "Unknown error"
                    android.util.Log.e("LoginActivity", "Signup failed: $error", task.exception)
                    Toast.makeText(this, "Sign up failed: $error", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun updateModeSelection(isGuardian: Boolean) {
        if (isGuardian) {
            // Guardian mode selected
            userModeButton.alpha = 0.5f
            guardianModeButton.alpha = 1.0f
            userModeButton.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            guardianModeButton.setBackgroundColor(getColor(R.color.primary_blue))
            guardianModeButton.setTextColor(getColor(R.color.white))
            userModeButton.setTextColor(getColor(R.color.primary_blue))
        } else {
            // User mode selected
            userModeButton.alpha = 1.0f
            guardianModeButton.alpha = 0.5f
            userModeButton.setBackgroundColor(getColor(R.color.primary_blue))
            guardianModeButton.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            userModeButton.setTextColor(getColor(R.color.white))
            guardianModeButton.setTextColor(getColor(R.color.primary_blue))
        }
    }
}
