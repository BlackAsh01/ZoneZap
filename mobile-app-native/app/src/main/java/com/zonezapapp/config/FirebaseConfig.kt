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
        Log.d(TAG, "Firebase initialized (FCM only; auth/data use Vercel API)")
        Log.d(TAG, "Auth instance: ${auth.app.name}")
        Log.d(TAG, "Firestore instance: ${firestore.app.name}")
        // Emulators disabled: app uses Vercel API for auth and data.
        // To use Firebase emulators for local testing, set USE_FIREBASE_EMULATORS = true and run emulators.
        // auth.useEmulator("10.0.2.2", 9099)
        // firestore.useEmulator("10.0.2.2", 8080)
    }
}
