package com.example.background_vol_up_down_app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.background_vol_up_down_app.R
import com.example.background_vol_up_down_app.data.local.database.HerSafeDatabase
import com.example.background_vol_up_down_app.data.local.entities.EmergencyEvent
import com.example.background_vol_up_down_app.data.repository.EmergencyRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HistoryActivity : AppCompatActivity() {

    private lateinit var repository: EmergencyRepository
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyState: LinearLayout
    private lateinit var adapter: HistoryAdapter
    private val events = mutableListOf<EmergencyEvent>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        // Initialize repository
        val database = HerSafeDatabase.getDatabase(this)
        repository = EmergencyRepository(database.emergencyEventDao())

        // Initialize views
        recyclerView = findViewById(R.id.recyclerHistory)
        emptyState = findViewById(R.id.emptyState)

        // Setup toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        // Setup RecyclerView
        adapter = HistoryAdapter(events)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Load events
        loadEvents()
    }

    private fun loadEvents() {
        lifecycleScope.launch {
            repository.allEvents.collect { eventsList ->
                events.clear()
                events.addAll(eventsList)
                runOnUiThread {
                    adapter.notifyDataSetChanged()
                    if (events.isEmpty()) {
                        emptyState.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    } else {
                        emptyState.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
}

class HistoryAdapter(
    private val events: List<EmergencyEvent>
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvEventType: TextView = view.findViewById(R.id.tvEventType)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvTimestamp: TextView = view.findViewById(R.id.tvTimestamp)
        val tvAddress: TextView = view.findViewById(R.id.tvAddress)
        val tvCoordinates: TextView = view.findViewById(R.id.tvCoordinates)
        val tvSmsSent: TextView = view.findViewById(R.id.tvSmsSent)
        val tvRecorded: TextView = view.findViewById(R.id.tvRecorded)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_emergency_event, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = events[position]

        holder.tvEventType.text = when (event.eventType.name) {
            "VOLUME_BUTTON_TRIGGER" -> "🚨 تنبيه زر الصوت"
            "MANUAL_TRIGGER" -> "🚨 تنبيه يدوي"
            "SAFE_JOURNEY_ALERT" -> "🚶 تنبيه رحلة آمنة"
            "UNSAFE_ZONE_ENTRY" -> "⚠️ دخول منطقة خطرة"
            else -> "طوارئ"
        }

        holder.tvStatus.text = when (event.status.name) {
            "ACTIVE" -> "نشط"
            "RESOLVED" -> "تم الحل"
            "FALSE_ALARM" -> "إنذار كاذب"
            "CANCELLED" -> "ملغي"
            else -> event.status.name
        }

        holder.tvTimestamp.text = dateFormat.format(Date(event.timestamp))

        if (event.address != null) {
            holder.tvAddress.text = "📍 ${event.address}"
            holder.tvAddress.visibility = View.VISIBLE
        } else {
            holder.tvAddress.visibility = View.GONE
        }

        holder.tvCoordinates.text = String.format(
            "%.6f, %.6f",
            event.latitude,
            event.longitude
        )

        holder.tvSmsSent.visibility = if (event.smsSent) View.VISIBLE else View.GONE
        holder.tvRecorded.visibility = if (event.hasAudioRecording || event.hasVideoRecording) View.VISIBLE else View.GONE
    }

    override fun getItemCount() = events.size
}
