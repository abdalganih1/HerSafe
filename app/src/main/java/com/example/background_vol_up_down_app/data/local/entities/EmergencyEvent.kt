package com.example.background_vol_up_down_app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "emergency_events")
data class EmergencyEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val timestamp: Long = System.currentTimeMillis(),

    // Location data
    val latitude: Double,
    val longitude: Double,
    val locationAccuracy: Float? = null,
    val address: String? = null,

    // Emergency details
    val eventType: EmergencyType = EmergencyType.VOLUME_BUTTON_TRIGGER,
    val status: EmergencyStatus = EmergencyStatus.ACTIVE,

    // Recording info
    val hasAudioRecording: Boolean = false,
    val hasVideoRecording: Boolean = false,
    val audioFilePath: String? = null,
    val videoFilePath: String? = null,

    // Sync status
    val isSynced: Boolean = false,
    val syncedAt: Long? = null,
    val serverEventId: String? = null,

    // SMS status
    val smsSent: Boolean = false,
    val smsRecipients: String? = null, // Comma-separated phone numbers

    // Notes
    val notes: String? = null,
    val resolvedAt: Long? = null
)

enum class EmergencyType {
    VOLUME_BUTTON_TRIGGER,
    MANUAL_TRIGGER,
    SAFE_JOURNEY_ALERT,
    UNSAFE_ZONE_ENTRY
}

enum class EmergencyStatus {
    ACTIVE,
    RESOLVED,
    FALSE_ALARM,
    CANCELLED
}
