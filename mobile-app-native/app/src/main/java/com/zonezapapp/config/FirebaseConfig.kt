package com.zonezapapp.config

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

object FirebaseConfig {
    private const val TAG = "FirebaseConfig"
    
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    val messaging: FirebaseMessaging = FirebaseMessaging.getInstance()
    
    init {
        // Log Firebase initialization
        Log.d(TAG, "Firebase initialized")
        Log.d(TAG, "Auth instance: ${auth.app.name}")
        Log.d(TAG, "Firestore instance: ${firestore.app.name}")
        
        // Connect to Firebase emulators for local development
        // Uncomment to use production Firebase instead
        try {
            // For Android Emulator: 10.0.2.2 maps to host machine's localhost
            // For Physical Device: Replace with your computer's IP address
            auth.useEmulator("10.0.2.2", 9099) // Auth emulator port
            firestore.useEmulator("10.0.2.2", 8080) // Firestore emulator port
            Log.d(TAG, "✅ Connected to Firebase emulators (10.0.2.2:9099, 10.0.2.2:8080)")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to connect to emulators: ${e.message}")
            Log.w(TAG, "Will try to use production Firebase instead")
        }
    }
}
