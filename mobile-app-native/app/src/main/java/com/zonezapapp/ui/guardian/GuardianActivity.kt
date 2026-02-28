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
import com.zonezapapp.api.ApiClient
import com.zonezapapp.api.AuthManager
import com.zonezapapp.api.WardInfo
import com.zonezapapp.data.EmergencyAlert
import com.zonezapapp.data.LocationData
import com.zonezapapp.data.Reminder
import com.zonezapapp.services.EmergencyService
import com.zonezapapp.services.ReminderService
import com.zonezapapp.services.UserService
import com.zonezapapp.services.GeocodingHelper
import com.zonezapapp.services.WardLocationService
import com.zonezapapp.ui.login.LoginActivity
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.Locale

private fun formatAlertTime(timestamp: Timestamp?): String {
    if (timestamp == null) return "Unknown time"
    return try {
        SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(timestamp.toDate())
    } catch (_: Exception) { "Unknown time" }
}

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
    private val emergencyService = EmergencyService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guardian)

        // Check authentication
        if (!AuthManager.isLoggedIn()) {
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

        wardsRecyclerView.layoutManager = LinearLayoutManager(this)
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
                val wardIds = AuthManager.getUser()?.wards ?: emptyList()
                if (wardIds.isEmpty()) {
                    noAlertsText.text = "No wards assigned"
                    noAlertsText.visibility = android.view.View.VISIBLE
                    return@launch
                }
                val alerts = emergencyService.getAlertsForWards(wardIds)
                val wardInfosForNames = try {
                    val list = withContext(Dispatchers.IO) { ApiClient.api().getGuardianWards() }
                    if (list.isNotEmpty()) list else fetchWardInfosById(wardIds)
                } catch (e: HttpException) {
                    if (e.code() == 404) fetchWardInfosById(wardIds) else throw e
                }
                val userIdToNameMap = wardInfosForNames.associate { it.id to (it.name?.takeIf { n -> n.isNotBlank() } ?: it.email?.takeIf { e -> e.isNotBlank() } ?: "Ward ${it.id.take(8)}") }
                if (alerts.isEmpty()) {
                    noAlertsText.text = "No active alerts"
                    noAlertsText.visibility = android.view.View.VISIBLE
                    alertsRecyclerView.visibility = android.view.View.GONE
                } else {
                    noAlertsText.visibility = android.view.View.GONE
                    alertsRecyclerView.visibility = android.view.View.VISIBLE
                    alertsAdapter.setUserNameMap(userIdToNameMap)
                    alertsAdapter.submitList(alerts)
                }
            } catch (e: Exception) {
                android.util.Log.e("GuardianActivity", "Error loading alerts", e)
                Toast.makeText(this@GuardianActivity, "Error loading alerts", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** Resolve ward id, name, email via GET /api/users/{id} for each ward (used when guardians/wards is unavailable). */
    private suspend fun fetchWardInfosById(wardIds: List<String>): List<WardInfo> = withContext(Dispatchers.IO) {
        wardIds.map { id ->
            try {
                ApiClient.api().getWardUserById(id)
            } catch (e: Exception) {
                android.util.Log.w("GuardianActivity", "getWardUserById(${id.take(8)}...) failed: ${e.message}")
                WardInfo(id = id, name = null, email = null)
            }
        }
    }

    private fun loadWards() {
        lifecycleScope.launch {
            try {
                // Refresh current user so we have latest wards list (e.g. after adding a ward)
                val token = AuthManager.getToken()
                if (!token.isNullOrBlank()) {
                    val me = withContext(Dispatchers.IO) { ApiClient.api().getMe() }
                    AuthManager.setAuth(token, me)
                }
                val wardIds = AuthManager.getUser()?.wards ?: emptyList()
                if (wardIds.isEmpty()) {
                    noWardsText.text = "No wards assigned to you"
                    noWardsText.visibility = android.view.View.VISIBLE
                    wardsRecyclerView.visibility = android.view.View.GONE
                    return@launch
                }
                // Prefer batch GET /api/guardians/wards; otherwise resolve each ward's name via GET /api/users/{id}
                val wardInfos = try {
                    val list = withContext(Dispatchers.IO) { ApiClient.api().getGuardianWards() }
                    if (list.isNotEmpty()) list else fetchWardInfosById(wardIds)
                } catch (e: HttpException) {
                    if (e.code() == 404) fetchWardInfosById(wardIds) else throw e
                }
                val wards = (if (wardInfos.isEmpty()) wardIds.map { id -> WardInfo(id = id, name = null, email = null) } else wardInfos).map { info ->
                    val location = wardLocationService.getLatestWardLocation(info.id)
                    Ward(
                        id = info.id,
                        name = info.name?.takeIf { it.isNotBlank() } ?: info.email?.takeIf { it.isNotBlank() } ?: "Ward ${info.id.take(8)}",
                        email = info.email ?: "",
                        latestLocation = location
                    )
                }
                noWardsText.visibility = android.view.View.GONE
                wardsRecyclerView.visibility = android.view.View.VISIBLE
                wardsAdapter.submitList(wards)
            } catch (e: Exception) {
                android.util.Log.e("GuardianActivity", "Error loading wards", e)
                Toast.makeText(this@GuardianActivity, "Error loading wards", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAlertDetails(alert: EmergencyAlert) {
        val ward = wardsAdapter.getCurrentList().find { it.id == alert.userId }
        val displayName = ward?.name?.takeIf { it.isNotBlank() } ?: ward?.email?.takeIf { it.isNotBlank() } ?: "Ward ${alert.userId.take(8)}"
        val locFallback = if (alert.location != null) "${alert.location!!.latitude}, ${alert.location!!.longitude}" else "Not available"
        lifecycleScope.launch {
            val locationStr = if (alert.location != null) {
                GeocodingHelper.getAddressFromLocation(this@GuardianActivity, alert.location!!) ?: locFallback
            } else locFallback
            if (isFinishing || isDestroyed) return@launch
            runOnUiThread {
                val builder = android.app.AlertDialog.Builder(this@GuardianActivity)
                    .setTitle("Emergency Alert: ${alert.alertType}")
                    .setMessage("From: $displayName\nTime: ${formatAlertTime(alert.timestamp)}\nStatus: ${alert.status}\nLocation: $locationStr")
                    .setPositiveButton("Mark as Resolved") { _, _ -> resolveAlert(alert.id) }
                if (alert.location != null) {
                    builder.setNeutralButton("Track on map") { _, _ -> openLiveMap(alert.userId, displayName) }
                }
                builder.setNegativeButton("Close", null).show()
            }
        }
    }

    private fun resolveAlert(alertId: String) {
        lifecycleScope.launch {
            try {
                emergencyService.updateAlertStatus(alertId, "RESOLVED")
                Toast.makeText(this@GuardianActivity, "Alert resolved", Toast.LENGTH_SHORT).show()
                loadAlerts()
            } catch (e: Exception) {
                Toast.makeText(this@GuardianActivity, "Failed to resolve alert", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (AuthManager.isLoggedIn()) {
            loadWards()
            loadAlerts()
        }
    }

    private fun viewWardDetails(wardId: String) {
        showWardDetailsDialog(wardId)
    }

    private fun openLiveMap(wardId: String, wardName: String) {
        startActivity(Intent(this, WardLiveMapActivity::class.java).apply {
            putExtra(WardLiveMapActivity.EXTRA_WARD_ID, wardId)
            putExtra(WardLiveMapActivity.EXTRA_WARD_NAME, wardName)
        })
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
                    val address = GeocodingHelper.getAddressFromLocation(this@GuardianActivity, location)
                    val placeStr = address ?: "Lat: ${String.format("%.6f", location.latitude)}, Lng: ${String.format("%.6f", location.longitude)}"
                    "$placeStr\nAccuracy: ${location.accuracy.toInt()}m\nLast updated: $timeAgo"
                } else {
                    "Location not available"
                }
                
                val linear = android.widget.LinearLayout(this@GuardianActivity).apply {
                    orientation = android.widget.LinearLayout.VERTICAL
                    setPadding(48, 24, 48, 16)
                    addView(android.widget.TextView(this@GuardianActivity).apply {
                        text = "Email: ${ward.email}\n\nLocation:\n$locationText"
                        setPadding(0, 0, 0, 24)
                        textSize = 14f
                    })
                    val lp = android.widget.LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.MATCH_PARENT, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = 12 }
                    addView(com.google.android.material.button.MaterialButton(this@GuardianActivity).apply { id = android.view.View.generateViewId(); text = "Track on map" }, lp)
                    addView(com.google.android.material.button.MaterialButton(this@GuardianActivity).apply { id = android.view.View.generateViewId(); text = "Add Reminder" }, lp)
                    addView(com.google.android.material.button.MaterialButton(this@GuardianActivity).apply { id = android.view.View.generateViewId(); text = "View location history" }, lp)
                }
                val scroll = android.widget.ScrollView(this@GuardianActivity).apply { addView(linear) }
                val dialog = androidx.appcompat.app.AlertDialog.Builder(this@GuardianActivity)
                    .setTitle("Ward: ${ward.name}")
                    .setView(scroll)
                    .setNegativeButton("Close", null)
                    .create()
                val trackBtn = linear.getChildAt(1) as? com.google.android.material.button.MaterialButton
                val reminderBtn = linear.getChildAt(2) as? com.google.android.material.button.MaterialButton
                val historyBtn = linear.getChildAt(3) as? com.google.android.material.button.MaterialButton
                trackBtn?.setOnClickListener { openLiveMap(wardId, ward.name); dialog.dismiss() }
                reminderBtn?.setOnClickListener { showAddReminderForWardDialog(wardId); dialog.dismiss() }
                historyBtn?.setOnClickListener { showLocationHistory(wardId, ward.name); dialog.dismiss() }
                dialog.show()
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
                
                val locationLines = mutableListOf<String>()
                for (loc in locations.take(10)) {
                    val time = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault())
                        .format(java.util.Date(loc.timestamp))
                    val address = GeocodingHelper.getAddressFromLocation(this@GuardianActivity, loc)
                    val placeStr = address ?: "Lat: ${String.format("%.6f", loc.latitude)}, Lng: ${String.format("%.6f", loc.longitude)}"
                    locationLines.add("$time\n$placeStr\nAccuracy: ${loc.accuracy.toInt()}m")
                }
                val locationText = locationLines.joinToString("\n\n")
                
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
                val guardianId = AuthManager.getUserId() ?: return@launch
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
        val trimmedEmail = email.trim()
        if (trimmedEmail.isEmpty()) {
            Toast.makeText(this, "Please enter an email", Toast.LENGTH_SHORT).show()
            return
        }
        android.util.Log.w("GuardianActivity", "Add ward: calling API with ward_email=$trimmedEmail")
        lifecycleScope.launch {
            try {
                userService.addWardByEmail(trimmedEmail)
                // Refresh guardian user from server so wards list includes the new ward, then reload UI
                val token = AuthManager.getToken()
                if (!token.isNullOrBlank()) {
                    val me = withContext(Dispatchers.IO) { ApiClient.api().getMe() }
                    AuthManager.setAuth(token, me)
                }
                Toast.makeText(this@GuardianActivity, "Ward added successfully!", Toast.LENGTH_SHORT).show()
                loadWards()
            } catch (e: HttpException) {
                val code = e.code()
                val body = e.response()?.errorBody()?.string() ?: ""
                val apiMsg = parseApiError(body)
                android.util.Log.e("GuardianActivity", "Add ward failed: code=$code body=$body apiMsg=$apiMsg", e)
                val msg = when (code) {
                    403 -> "Only guardian accounts can add wards."
                    404 -> apiMsg ?: "Ward not found. User must sign up as a ward (user) first."
                    400 -> apiMsg ?: "Invalid request. Check that the email is correct and the account is a ward (user)."
                    else -> apiMsg ?: "Failed to add ward ($code)."
                }
                Toast.makeText(this@GuardianActivity, msg, Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                android.util.Log.e("GuardianActivity", "Error adding ward", e)
                Toast.makeText(this@GuardianActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun parseApiError(body: String?): String? {
        if (body.isNullOrBlank() || !body.trim().startsWith("{")) return null
        return try {
            org.json.JSONObject(body.trim()).optString("error").takeIf { it.isNotEmpty() }
        } catch (_: Exception) {
            null
        }
    }

    private fun logout() {
        AuthManager.clear()
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
        holder.subtitleText.text = formatAlertTime(alert.timestamp)
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
