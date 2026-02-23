package com.zonezapapp.ui.guardian

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.zonezapapp.R
import com.zonezapapp.config.FirebaseConfig
import com.zonezapapp.data.EmergencyAlert
import com.zonezapapp.data.LocationData
import com.zonezapapp.data.Reminder
import com.zonezapapp.services.ReminderService
import com.zonezapapp.services.UserService
import com.zonezapapp.services.WardLocationService
import com.zonezapapp.ui.login.LoginActivity
import com.google.firebase.Timestamp
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch

class GuardianActivity : AppCompatActivity() {
    private lateinit var alertsRecyclerView: RecyclerView
    private lateinit var wardsRecyclerView: RecyclerView
    private lateinit var noAlertsText: TextView
    private lateinit var noWardsText: TextView
    
    private val alertsAdapter = AlertsAdapter { alert ->
        showAlertDetails(alert)
    }
    private val wardsAdapter = WardsAdapter { wardId ->
        viewWardDetails(wardId)
    }
    private val userService = UserService()
    private val wardLocationService = WardLocationService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guardian)

        // Check authentication
        if (FirebaseConfig.auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setupViews()
        loadAlerts()
        loadWards()
    }

    private fun setupViews() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = "Guardian Dashboard"
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        alertsRecyclerView = findViewById(R.id.alertsRecyclerView)
        wardsRecyclerView = findViewById(R.id.wardsRecyclerView)
        noAlertsText = findViewById(R.id.noAlertsText)
        noWardsText = findViewById(R.id.noWardsText)

        alertsRecyclerView.layoutManager = LinearLayoutManager(this)
        alertsRecyclerView.adapter = alertsAdapter

        wardsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        wardsRecyclerView.adapter = wardsAdapter

        findViewById<MaterialButton>(R.id.logoutButton).setOnClickListener {
            logout()
        }

        findViewById<MaterialButton>(R.id.addWardButton).setOnClickListener {
            showAddWardDialog()
        }
    }

    private fun loadAlerts() {
        lifecycleScope.launch {
            try {
                val guardianId = FirebaseConfig.auth.currentUser?.uid ?: return@launch
                
                // Get all users where this guardian is in their guardians list
                val usersSnapshot = FirebaseConfig.firestore.collection("users")
                    .whereArrayContains("guardians", guardianId)
                    .get()
                    .await()

                val wardIds = usersSnapshot.documents.map { it.id }
                
                if (wardIds.isEmpty()) {
                    noAlertsText.text = "No wards assigned"
                    noAlertsText.visibility = android.view.View.VISIBLE
                    return@launch
                }

                // Get active alerts for all wards
                val alerts = mutableListOf<EmergencyAlert>()
                val userIdToNameMap = mutableMapOf<String, String>() // Cache user names
                
                wardIds.forEach { wardId ->
                    val alertsSnapshot = FirebaseConfig.firestore.collection("alerts")
                        .whereEqualTo("userId", wardId)
                        .whereEqualTo("status", "ACTIVE")
                        .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                        .limit(10)
                        .get()
                        .await()
                    
                    alerts.addAll(alertsSnapshot.documents.map { EmergencyAlert.fromDocument(it) })
                    
                    // Fetch user info for this ward if not already cached
                    if (!userIdToNameMap.containsKey(wardId)) {
                        try {
                            val userDoc = FirebaseConfig.firestore.collection("users")
                                .document(wardId)
                                .get()
                                .await()
                            val userData = userDoc.data ?: emptyMap()
                            val userName = userData["name"] as? String
                            val userEmail = userData["email"] as? String
                            userIdToNameMap[wardId] = userName ?: userEmail ?: wardId
                        } catch (e: Exception) {
                            android.util.Log.e("GuardianActivity", "Error fetching user info", e)
                            userIdToNameMap[wardId] = wardId // Fallback to UID
                        }
                    }
                }

                if (alerts.isEmpty()) {
                    noAlertsText.text = "No active alerts"
                    noAlertsText.visibility = android.view.View.VISIBLE
                    alertsRecyclerView.visibility = android.view.View.GONE
                } else {
                    noAlertsText.visibility = android.view.View.GONE
                    alertsRecyclerView.visibility = android.view.View.VISIBLE
                    // Pass user name map to adapter
                    alertsAdapter.setUserNameMap(userIdToNameMap)
                    alertsAdapter.submitList(alerts.sortedByDescending { it.timestamp?.toDate()?.time ?: 0L })
                }
            } catch (e: Exception) {
                android.util.Log.e("GuardianActivity", "Error loading alerts", e)
                Toast.makeText(this@GuardianActivity, "Error loading alerts", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadWards() {
        lifecycleScope.launch {
            try {
                val guardianId = FirebaseConfig.auth.currentUser?.uid ?: return@launch
                
                val usersSnapshot = FirebaseConfig.firestore.collection("users")
                    .whereArrayContains("guardians", guardianId)
                    .get()
                    .await()

                val wards = usersSnapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: emptyMap()
                    val wardId = doc.id
                    // Fetch latest location for each ward
                    val location = wardLocationService.getLatestWardLocation(wardId)
                    Ward(
                        id = wardId,
                        name = data["name"] as? String ?: data["email"] as? String ?: "Unknown",
                        email = data["email"] as? String ?: "",
                        latestLocation = location
                    )
                }

                if (wards.isEmpty()) {
                    noWardsText.text = "No wards assigned to you"
                    noWardsText.visibility = android.view.View.VISIBLE
                    wardsRecyclerView.visibility = android.view.View.GONE
                } else {
                    noWardsText.visibility = android.view.View.GONE
                    wardsRecyclerView.visibility = android.view.View.VISIBLE
                    wardsAdapter.submitList(wards)
                }
            } catch (e: Exception) {
                android.util.Log.e("GuardianActivity", "Error loading wards", e)
            }
        }
    }

    private fun showAlertDetails(alert: EmergencyAlert) {
        // Fetch user information to display name/email instead of UID
        lifecycleScope.launch {
            try {
                val userDoc = FirebaseConfig.firestore.collection("users")
                    .document(alert.userId)
                    .get()
                    .await()
                
                val userData = userDoc.data ?: emptyMap()
                val userName = userData["name"] as? String
                val userEmail = userData["email"] as? String
                val displayName = userName ?: userEmail ?: alert.userId
                
                val timeString = alert.timestamp?.toDate()?.toString() ?: "Unknown"
                val locationString = if (alert.location != null) {
                    "${alert.location.latitude}, ${alert.location.longitude}"
                } else {
                    "Not available"
                }
                
                android.app.AlertDialog.Builder(this@GuardianActivity)
                    .setTitle("Emergency Alert: ${alert.alertType}")
                    .setMessage(
                        "From: $displayName\n" +
                        "Email: ${userEmail ?: "N/A"}\n" +
                        "Time: $timeString\n" +
                        "Status: ${alert.status}\n" +
                        "Location: $locationString"
                    )
                    .setPositiveButton("Mark as Resolved") { _, _ ->
                        resolveAlert(alert.id)
                    }
                    .setNegativeButton("Close", null)
                    .show()
            } catch (e: Exception) {
                android.util.Log.e("GuardianActivity", "Error fetching user info for alert", e)
                // Fallback to showing UID if user fetch fails
                android.app.AlertDialog.Builder(this@GuardianActivity)
                    .setTitle("Emergency Alert: ${alert.alertType}")
                    .setMessage(
                        "From: ${alert.userId}\n" +
                        "Time: ${alert.timestamp?.toDate()}\n" +
                        "Status: ${alert.status}\n" +
                        if (alert.location != null) {
                            "Location: ${alert.location.latitude}, ${alert.location.longitude}"
                        } else {
                            "Location: Not available"
                        }
                    )
                    .setPositiveButton("Mark as Resolved") { _, _ ->
                        resolveAlert(alert.id)
                    }
                    .setNegativeButton("Close", null)
                    .show()
            }
        }
    }

    private fun resolveAlert(alertId: String) {
        lifecycleScope.launch {
            try {
                FirebaseConfig.firestore.collection("alerts").document(alertId)
                    .update("status", "RESOLVED")
                    .await()
                
                Toast.makeText(this@GuardianActivity, "Alert resolved", Toast.LENGTH_SHORT).show()
                loadAlerts()
            } catch (e: Exception) {
                Toast.makeText(this@GuardianActivity, "Failed to resolve alert", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun viewWardDetails(wardId: String) {
        // Show ward details dialog with location and options
        showWardDetailsDialog(wardId)
    }
    
    private fun showWardDetailsDialog(wardId: String) {
        val ward = wardsAdapter.getCurrentList().find { it.id == wardId }
        if (ward == null) {
            Toast.makeText(this, "Ward not found", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            try {
                // Refresh location
                val location = wardLocationService.getLatestWardLocation(wardId)
                
                val locationText = if (location != null) {
                    val timeAgo = if (location.timestamp > 0) {
                        val diff = System.currentTimeMillis() - location.timestamp
                        val minutesAgo = diff / 60000
                        if (minutesAgo < 1) "Just now"
                        else if (minutesAgo < 60) "$minutesAgo minutes ago"
                        else "${minutesAgo / 60} hours ago"
                    } else "Unknown"
                    "Lat: ${String.format("%.6f", location.latitude)}\n" +
                    "Lng: ${String.format("%.6f", location.longitude)}\n" +
                    "Accuracy: ${location.accuracy.toInt()}m\n" +
                    "Last updated: $timeAgo"
                } else {
                    "Location not available"
                }
                
                androidx.appcompat.app.AlertDialog.Builder(this@GuardianActivity)
                    .setTitle("Ward: ${ward.name}")
                    .setMessage("Email: ${ward.email}\n\nLocation:\n$locationText")
                    .setPositiveButton("Add Reminder") { _, _ ->
                        showAddReminderForWardDialog(wardId)
                    }
                    .setNeutralButton("View Location History") { _, _ ->
                        showLocationHistory(wardId, ward.name)
                    }
                    .setNegativeButton("Close", null)
                    .show()
            } catch (e: Exception) {
                android.util.Log.e("GuardianActivity", "Error showing ward details", e)
                Toast.makeText(this@GuardianActivity, "Error loading ward details", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showLocationHistory(wardId: String, wardName: String) {
        lifecycleScope.launch {
            try {
                val locations = wardLocationService.getWardLocationHistory(wardId, 20)
                if (locations.isEmpty()) {
                    Toast.makeText(this@GuardianActivity, "No location history available", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                
                val locationText = locations.take(10).joinToString("\n\n") { loc ->
                    val time = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
                        .format(java.util.Date(loc.timestamp))
                    "$time\nLat: ${String.format("%.6f", loc.latitude)}, Lng: ${String.format("%.6f", loc.longitude)}\nAccuracy: ${loc.accuracy.toInt()}m"
                }
                
                androidx.appcompat.app.AlertDialog.Builder(this@GuardianActivity)
                    .setTitle("Location History: $wardName")
                    .setMessage(locationText)
                    .setPositiveButton("OK", null)
                    .show()
            } catch (e: Exception) {
                android.util.Log.e("GuardianActivity", "Error loading location history", e)
                Toast.makeText(this@GuardianActivity, "Error loading location history", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showAddReminderForWardDialog(wardId: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_reminder, null)
        val titleEditText = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.titleEditText)
        val descriptionEditText = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.descriptionEditText)

        // Get ward name for dialog title
        val ward = wardsAdapter.getCurrentList().find { it.id == wardId }
        val wardName = ward?.name ?: "Ward"

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Add Reminder for $wardName")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val title = titleEditText.text?.toString() ?: ""
                val description = descriptionEditText.text?.toString() ?: ""

                if (title.isNotEmpty()) {
                    addReminderForWard(wardId, title, description)
                } else {
                    Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun addReminderForWard(wardId: String, title: String, description: String) {
        lifecycleScope.launch {
            try {
                val guardianId = FirebaseConfig.auth.currentUser?.uid ?: return@launch
                val scheduledTime = java.util.Date(System.currentTimeMillis() + 3600000) // 1 hour from now

                val reminder = Reminder(
                    userId = wardId,
                    title = title,
                    description = description,
                    scheduledTime = Timestamp(scheduledTime),
                    type = "GUARDIAN_REMINDER"
                )

                val reminderService = ReminderService()
                reminderService.createReminderForWard(wardId, reminder, guardianId)
                Toast.makeText(this@GuardianActivity, "Reminder added for ward", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                android.util.Log.e("GuardianActivity", "Error adding reminder for ward", e)
                Toast.makeText(this@GuardianActivity, "Failed to create reminder: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAddWardDialog() {
        val dialogView = layoutInflater.inflate(android.R.layout.simple_list_item_1, null)
        val emailInput = TextInputEditText(this)
        emailInput.hint = "Enter user email"
        emailInput.layoutParams = android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val container = android.widget.LinearLayout(this)
        container.orientation = android.widget.LinearLayout.VERTICAL
        container.setPadding(32, 16, 32, 16)
        container.addView(emailInput)

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Add Ward")
            .setMessage("Enter the email of the user you want to monitor")
            .setView(container)
            .setPositiveButton("Add") { _, _ ->
                val email = emailInput.text?.toString()?.trim() ?: ""
                if (email.isNotEmpty()) {
                    addWardByEmail(email)
                } else {
                    Toast.makeText(this, "Please enter an email", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addWardByEmail(email: String) {
        lifecycleScope.launch {
            try {
                val user = userService.findUserByEmail(email)
                if (user == null) {
                    Toast.makeText(this@GuardianActivity, "User not found with email: $email", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val guardianId = FirebaseConfig.auth.currentUser?.uid ?: return@launch
                val userId = user["userId"] as? String ?: return@launch

                val success = userService.addWardToGuardian(guardianId, userId)
                if (success) {
                    Toast.makeText(this@GuardianActivity, "Ward added successfully!", Toast.LENGTH_SHORT).show()
                    loadWards() // Refresh the wards list
                } else {
                    Toast.makeText(this@GuardianActivity, "Failed to add ward. User may already be added.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                android.util.Log.e("GuardianActivity", "Error adding ward", e)
                Toast.makeText(this@GuardianActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun logout() {
        FirebaseConfig.auth.signOut()
        // Wait a moment for signOut to complete, then navigate
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("from_logout", true) // Flag to indicate logout
            }
            startActivity(intent)
            finish() // Finish current activity
        }, 300) // Delay to ensure signOut completes
    }

    data class Ward(
        val id: String,
        val name: String,
        val email: String,
        val latestLocation: LocationData? = null
    )
}

class AlertsAdapter(
    private val onAlertClick: (EmergencyAlert) -> Unit
) : RecyclerView.Adapter<AlertsAdapter.ViewHolder>() {
    private var alerts = listOf<EmergencyAlert>()
    private var userIdToNameMap = mapOf<String, String>()

    fun setUserNameMap(map: Map<String, String>) {
        userIdToNameMap = map
        notifyDataSetChanged()
    }

    fun submitList(newList: List<EmergencyAlert>) {
        alerts = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val alert = alerts[position]
        val userName = userIdToNameMap[alert.userId] ?: alert.userId
        holder.titleText.text = "${alert.alertType} - $userName"
        holder.subtitleText.text = alert.timestamp?.toDate()?.toString() ?: "Unknown time"
        holder.itemView.setOnClickListener {
            onAlertClick(alert)
        }
    }

    override fun getItemCount() = alerts.size

    class ViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val titleText: TextView = view.findViewById(android.R.id.text1)
        val subtitleText: TextView = view.findViewById(android.R.id.text2)
    }
}

class WardsAdapter(
    private val onWardClick: (String) -> Unit
) : RecyclerView.Adapter<WardsAdapter.ViewHolder>() {
    private var wards = listOf<GuardianActivity.Ward>()
    
    fun getCurrentList(): List<GuardianActivity.Ward> = wards

    fun submitList(newList: List<GuardianActivity.Ward>) {
        wards = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ward = wards[position]
        // Display ward name with location indicator
        val locationIndicator = if (ward.latestLocation != null) {
            val timeAgo = if (ward.latestLocation.timestamp > 0) {
                val diff = System.currentTimeMillis() - ward.latestLocation.timestamp
                val minutesAgo = diff / 60000
                if (minutesAgo < 60) "$minutesAgo min ago"
                else "${minutesAgo / 60}h ago"
            } else ""
            "📍 $timeAgo"
        } else {
            "📍 No location"
        }
        holder.nameText.text = "${ward.name}\n$locationIndicator"
        holder.itemView.setOnClickListener {
            onWardClick(ward.id)
        }
    }

    override fun getItemCount() = wards.size

    class ViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(android.R.id.text1)
    }
}
