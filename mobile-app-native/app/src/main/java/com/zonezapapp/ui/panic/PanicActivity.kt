package com.zonezapapp.ui.panic

import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.zonezapapp.R
import com.zonezapapp.api.AuthManager
import com.zonezapapp.services.EmergencyService
import com.zonezapapp.services.LocationService
import kotlinx.coroutines.launch

class PanicActivity : AppCompatActivity() {
    private lateinit var locationText: TextView
    private lateinit var panicButton: MaterialButton
    private lateinit var wanderingButton: MaterialButton

    private lateinit var locationService: LocationService
    private val emergencyService = EmergencyService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_panic)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Emergency Alerts"

        locationText = findViewById(R.id.locationText)
        panicButton = findViewById(R.id.panicButton)
        wanderingButton = findViewById(R.id.wanderingButton)

        // Initialize LocationService after Activity is created
        locationService = LocationService(this)

        loadLocation()

        panicButton.setOnClickListener {
            handlePanic()
        }

        wanderingButton.setOnClickListener {
            handleWandering()
        }
    }

    private fun loadLocation() {
        lifecycleScope.launch {
            try {
                val location = locationService.getCurrentLocation()
                location?.let {
                    locationText.text = "${String.format("%.6f", it.latitude)}, ${String.format("%.6f", it.longitude)}"
                } ?: run {
                    locationText.text = "Location unavailable"
                }
            } catch (e: Exception) {
                locationText.text = "Error getting location"
            }
        }
    }

    private fun handlePanic() {
        AlertDialog.Builder(this)
            .setTitle("Emergency Alert")
            .setMessage("Are you sure you want to send an emergency alert?")
            .setPositiveButton("Send Alert") { _, _ ->
                sendPanicAlert()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun sendPanicAlert() {
        // Vibrate device
        val vibrator = getSystemService(VIBRATOR_SERVICE) as? Vibrator
        vibrator?.let {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 200, 500), -1))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(longArrayOf(0, 500, 200, 500), -1)
            }
        }

        lifecycleScope.launch {
            try {
                val userId = AuthManager.getUserId() ?: return@launch
                val location = locationService.getCurrentLocation()

                emergencyService.sendEmergencyAlert(
                    userId,
                    "PANIC",
                    location,
                    mapOf(
                        "message" to "User activated panic button",
                        "priority" to "HIGHEST"
                    )
                )

                AlertDialog.Builder(this@PanicActivity)
                    .setTitle("Alert Sent")
                    .setMessage("Your emergency alert has been sent to all registered guardians. Help is on the way!")
                    .setPositiveButton("OK") { _, _ ->
                        finish()
                    }
                    .show()
            } catch (e: Exception) {
                Toast.makeText(this@PanicActivity, "Failed to send emergency alert. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleWandering() {
        lifecycleScope.launch {
            try {
                val userId = AuthManager.getUserId() ?: return@launch
                val location = locationService.getCurrentLocation()

                emergencyService.sendEmergencyAlert(
                    userId,
                    "WANDERING",
                    location,
                    mapOf(
                        "message" to "User may be wandering outside safe zone",
                        "priority" to "HIGH"
                    )
                )

                Toast.makeText(this@PanicActivity, "Wandering alert sent to guardians", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@PanicActivity, "Failed to send alert", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
