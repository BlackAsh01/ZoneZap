package com.zonezapapp.ui.reminders

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.zonezapapp.R
import com.zonezapapp.config.FirebaseConfig
import com.zonezapapp.data.Reminder
import com.zonezapapp.services.ReminderService
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class RemindersActivity : AppCompatActivity() {
    private lateinit var remindersRecyclerView: RecyclerView
    private lateinit var fab: com.google.android.material.floatingactionbutton.FloatingActionButton

    private val reminderService = ReminderService()
    private val remindersAdapter = RemindersAdapter { reminder ->
        showReminderActions(reminder)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminders)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Reminders"

        remindersRecyclerView = findViewById(R.id.remindersRecyclerView)
        fab = findViewById(R.id.fab)

        remindersRecyclerView.layoutManager = LinearLayoutManager(this)
        remindersRecyclerView.adapter = remindersAdapter

        fab.setOnClickListener {
            showAddReminderDialog()
        }

        loadReminders()
    }

    private fun loadReminders() {
        val userId = FirebaseConfig.auth.currentUser?.uid ?: return
        lifecycleScope.launch {
            reminderService.getUserRemindersFlow(userId)
                .onEach { reminders ->
                    remindersAdapter.submitList(reminders)
                }
                .launchIn(this)
        }
    }

    private fun showAddReminderDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_reminder, null)
        val titleEditText = dialogView.findViewById<TextInputEditText>(R.id.titleEditText)
        val descriptionEditText = dialogView.findViewById<TextInputEditText>(R.id.descriptionEditText)

        MaterialAlertDialogBuilder(this)
            .setTitle("New Reminder")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val title = titleEditText.text?.toString() ?: ""
                val description = descriptionEditText.text?.toString() ?: ""

                if (title.isNotEmpty()) {
                    addReminder(title, description)
                } else {
                    Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addReminder(title: String, description: String) {
        lifecycleScope.launch {
            try {
                val userId = FirebaseConfig.auth.currentUser?.uid ?: return@launch
                val scheduledTime = java.util.Date(System.currentTimeMillis() + 3600000) // 1 hour from now

                val reminder = Reminder(
                    userId = userId,
                    title = title,
                    description = description,
                    scheduledTime = Timestamp(scheduledTime),
                    type = "GENERAL"
                )

                reminderService.createReminder(userId, reminder)
                Toast.makeText(this@RemindersActivity, "Reminder added", Toast.LENGTH_SHORT).show()
                // Note: loadReminders() not needed - Flow will automatically update
            } catch (e: Exception) {
                Toast.makeText(this@RemindersActivity, "Failed to create reminder", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showReminderActions(reminder: Reminder) {
        AlertDialog.Builder(this)
            .setTitle(reminder.title)
            .setItems(arrayOf("Complete", "Delete")) { _, which ->
                when (which) {
                    0 -> completeReminder(reminder.id)
                    1 -> deleteReminder(reminder.id)
                }
            }
            .show()
    }

    private fun completeReminder(reminderId: String) {
        lifecycleScope.launch {
            try {
                reminderService.completeReminder(reminderId)
                // Note: loadReminders() not needed - Flow will automatically update
            } catch (e: Exception) {
                Toast.makeText(this@RemindersActivity, "Failed to complete reminder", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteReminder(reminderId: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete Reminder")
            .setMessage("Are you sure you want to delete this reminder?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    try {
                        reminderService.deleteReminder(reminderId)
                        // Note: loadReminders() not needed - Flow will automatically update
                    } catch (e: Exception) {
                        Toast.makeText(this@RemindersActivity, "Failed to delete reminder", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}

class RemindersAdapter(
    private val onReminderClick: (Reminder) -> Unit
) : RecyclerView.Adapter<RemindersAdapter.ViewHolder>() {
    private var reminders = listOf<Reminder>()

    fun submitList(newList: List<Reminder>) {
        reminders = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reminder, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val reminder = reminders[position]
        holder.bind(reminder, onReminderClick)
    }

    override fun getItemCount() = reminders.size

    class ViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        private val titleText: TextView = view.findViewById(R.id.titleText)
        private val descriptionText: TextView = view.findViewById(R.id.descriptionText)
        private val timeText: TextView = view.findViewById(R.id.timeText)
        private val typeText: TextView = view.findViewById(R.id.typeText)
        private val completeButton: MaterialButton = view.findViewById(R.id.completeButton)
        private val deleteButton: MaterialButton = view.findViewById(R.id.deleteButton)

        fun bind(reminder: Reminder, onReminderClick: (Reminder) -> Unit) {
            // Show title with guardian indicator if created by guardian
            val titleDisplay = if (reminder.createdBy != null) {
                "${reminder.title} (From Guardian)"
            } else {
                reminder.title
            }
            titleText.text = titleDisplay
            
            if (reminder.description.isNotEmpty()) {
                descriptionText.text = reminder.description
                descriptionText.visibility = android.view.View.VISIBLE
            } else {
                descriptionText.visibility = android.view.View.GONE
            }
            timeText.text = reminder.scheduledTime?.toDate()?.toString() ?: ""
            typeText.text = "Type: ${reminder.type}"

            if (reminder.isCompleted) {
                titleText.alpha = 0.6f
                titleText.paintFlags = titleText.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                completeButton.visibility = android.view.View.GONE
            } else {
                titleText.alpha = 1.0f
                val flags = titleText.paintFlags
                titleText.paintFlags = flags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
                completeButton.visibility = android.view.View.VISIBLE
            }

            itemView.setOnClickListener {
                onReminderClick(reminder)
            }

            completeButton.setOnClickListener {
                onReminderClick(reminder)
            }

            deleteButton.setOnClickListener {
                onReminderClick(reminder)
            }
        }
    }
}
