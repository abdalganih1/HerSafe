package com.example.background_vol_up_down_app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "location_points")
data class LocationPoint(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Journey ID to group points together
    val journeyId: String,

    // Location data
    val latitude: Double,
    val longitude: Double,
    val altitude: Double? = null,
    val accuracy: Float,
    val speed: Float? = null,
    val bearing: Float? = null,

    // Timestamp
    val timestamp: Long = System.currentTimeMillis(),

    // Battery level at time of capture
    val batteryLevel: Int? = null,

    // Is this point synced to server
    val isSynced: Boolean = false,
    val syncedAt: Long? = null
)

@Entity(tableName = "safe_journeys")
data class SafeJourney(
    @PrimaryKey
    val journeyId: String,

    // Journey details
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,

    // Start and destination
    val startLatitude: Double,
    val startLongitude: Double,
    val startAddress: String? = null,

    val destinationLatitude: Double,
    val destinationLongitude: Double,
    val destinationAddress: String? = null,

    // Status
    val status: JourneyStatus = JourneyStatus.IN_PROGRESS,

    // Contacts notified
    val notifiedContacts: String? = null, // Comma-separated contact IDs

    // Journey metadata
    val estimatedDurationMinutes: Int? = null,
    val actualDurationMinutes: Int? = null,
    val distanceMeters: Float? = null,

    // Alerts
    val hasDeviationAlert: Boolean = false,
    val hasStoppedAlert: Boolean = false,
    val alertTimestamp: Long? = null,

    // Sync
    val isSynced: Boolean = false,
    val syncedAt: Long? = null,

    val notes: String? = null
)

enum class JourneyStatus {
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    ALERT_TRIGGERED,
    DEVIATED
}
