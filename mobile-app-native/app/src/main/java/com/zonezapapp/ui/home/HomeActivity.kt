package com.zonezapapp.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.zonezapapp.R
import com.zonezapapp.api.ApiClient
import com.zonezapapp.api.AuthManager
import com.zonezapapp.api.MovementLogRequest
import com.zonezapapp.data.LocationData
import com.zonezapapp.data.Reminder
import com.zonezapapp.services.EmergencyService
import com.zonezapapp.services.LocationService
import com.zonezapapp.services.ReminderService
import com.zonezapapp.ui.login.LoginActivity
import com.zonezapapp.ui.panic.PanicActivity
import com.zonezapapp.ui.reminders.RemindersActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeActivity : AppCompatActivity() {
    private lateinit var locationText: TextView
    private lateinit var locationCard: MaterialCardView
    private lateinit var remindersRecyclerView: RecyclerView
    private lateinit var panicButton: MaterialButton
    private lateinit var fab: FloatingActionButton

    private lateinit var locationService: LocationService
    private val emergencyService = EmergencyService()
    private val reminderService = ReminderService()
    private val remindersAdapter = RemindersAdapter()
    private var locationTrackingJob: Job? = null
    private var remindersJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Check authentication
        if (!AuthManager.isLoggedIn()) {
            startActivity(Intent(this, com.zonezapapp.ui.login.LoginActivity::class.java))
            finish()
            return
        }

        // Initialize LocationService after Activity is created
        locationService = LocationService(this)
        
        setupViews()
        requestLocationPermission()
        loadReminders()
    }

    private fun setupViews() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "ZoneZap"
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        locationText = findViewById(R.id.locationText)
        locationCard = findViewById(R.id.locationCard)
        remindersRecyclerView = findViewById(R.id.remindersRecyclerView)
        panicButton = findViewById(R.id.panicButton)
        fab = findViewById(R.id.fab)

        remindersRecyclerView.layoutManager = LinearLayoutManager(this)
        remindersRecyclerView.adapter = remindersAdapter

        findViewById<MaterialButton>(R.id.viewAllRemindersButton).setOnClickListener {
            startActivity(Intent(this, RemindersActivity::class.java))
        }

        panicButton.setOnClickListener {
            handlePanic()
        }

        fab.setOnClickListener {
            startActivity(Intent(this, RemindersActivity::class.java))
        }
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            startLocationTracking()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationTracking()
            } else {
                Toast.makeText(this, "Location permission is required for safety tracking", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun startLocationTracking() {
        // Cancel any existing tracking job
        locationTrackingJob?.cancel()
        // Send last known location immediately so guardian sees at least one point (e.g. if ward leaves app quickly)
        lifecycleScope.launch {
            val immediate = withContext(Dispatchers.IO) { locationService.getCurrentLocation() }
            if (immediate != null && !isFinishing && !isDestroyed) {
                updateLocationUI(immediate)
                logLocationToFirestore(immediate)
            }
        }
        locationTrackingJob = lifecycleScope.launch {
            locationService.trackLocation().onEach { location ->
                // Check if activity is still valid before updating UI
                if (!isFinishing && !isDestroyed) {
                    updateLocationUI(location)
                    logLocationToFirestore(location)
                }
            }.launchIn(this)
        }
    }

    private fun updateLocationUI(location: LocationData) {
        locationText.text = "Lat: ${String.format("%.6f", location.latitude)}\n" +
                "Lng: ${String.format("%.6f", location.longitude)}\n" +
                "Accuracy: ${location.accuracy.toInt()}m"
    }

    private fun logLocationToFirestore(location: LocationData) {
        lifecycleScope.launch {
            try {
                val userId = AuthManager.getUserId() ?: return@launch
                withContext(Dispatchers.IO) {
                    ApiClient.api().createMovementLog(
                        MovementLogRequest(
                            userId = userId,
                            latitude = location.latitude,
                            longitude = location.longitude,
                            timestamp = location.timestamp,
                            speed = location.speed.toDouble(),
                            heading = location.heading.toDouble(),
                            accuracy = location.accuracy.toDouble()
                        )
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeActivity", "Failed to send location", e)
            }
        }
    }

    private fun loadReminders() {
        val userId = AuthManager.getUserId() ?: return
        // Cancel any existing reminders job
        remindersJob?.cancel()
        remindersJob = lifecycleScope.launch {
            try {
                reminderService.getUserRemindersFlow(userId)
                    .onEach { reminders ->
                        // Check if activity is still valid before updating UI
                        if (!isFinishing && !isDestroyed) {
                            remindersAdapter.submitList(reminders.take(3))
                        }
                    }
                    .launchIn(this)
            } catch (e: Exception) {
                android.util.Log.e("HomeActivity", "Error loading reminders", e)
                // Don't crash if reminders fail to load
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
        lifecycleScope.launch {
            try {
                val userId = AuthManager.getUserId() ?: return@launch
                val location = locationService.getCurrentLocation()
                
                emergencyService.sendEmergencyAlert(userId, "PANIC", location)
                
                Toast.makeText(this@HomeActivity, "Emergency alert sent to your guardians", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@HomeActivity, PanicActivity::class.java))
            } catch (e: Exception) {
                Toast.makeText(this@HomeActivity, "Failed to send emergency alert", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun logout() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                // Cancel all active coroutines and listeners
                locationTrackingJob?.cancel()
                remindersJob?.cancel()
                
                // Sign out (clear API auth)
                AuthManager.clear()
                
                // Wait a moment for signOut to complete, then navigate
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    if (!isFinishing && !isDestroyed) {
                        val intent = Intent(this, LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            putExtra("from_logout", true) // Flag to indicate logout
                        }
                        startActivity(intent)
                        finish() // Finish current activity
                    }
                }, 300) // Delay to ensure signOut completes
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Cancel all coroutines when activity is destroyed
        locationTrackingJob?.cancel()
        remindersJob?.cancel()
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}

class RemindersAdapter : RecyclerView.Adapter<RemindersAdapter.ViewHolder>() {
    private var reminders = listOf<Reminder>()

    fun submitList(newList: List<Reminder>) {
        reminders = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reminder = reminders[position]
        // Show title with guardian indicator if created by guardian
        val titleDisplay = if (reminder.createdBy != null) {
            "${reminder.title} (From Guardian)"
        } else {
            reminder.title
        }
        holder.titleText.text = titleDisplay
        holder.subtitleText.text = reminder.scheduledTime?.toDate()?.toString() ?: ""
    }

    override fun getItemCount() = reminders.size

    class ViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val titleText: TextView = view.findViewById(android.R.id.text1)
        val subtitleText: TextView = view.findViewById(android.R.id.text2)
    }
}
