package com.example.background_vol_up_down_app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "safe_zones")
data class SafeZone(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Location
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Float = 100f, // Default radius for zone

    // Zone details
    val name: String? = null,
    val address: String? = null,

    // Safety rating (0-100)
    val safetyScore: Int = 50, // 0 = Very Dangerous, 100 = Very Safe

    // Statistics
    val incidentCount: Int = 0,
    val lastIncidentTimestamp: Long? = null,

    // Zone type
    val zoneType: ZoneType = ZoneType.CALCULATED,

    // Metadata
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val serverZoneId: String? = null,

    // User-defined zones
    val isUserDefined: Boolean = false,
    val notes: String? = null
)

enum class ZoneType {
    SAFE,           // User-marked as safe (home, work, etc)
    CALCULATED,     // Calculated from incident data
    DANGEROUS,      // High incident rate
    WARNING,        // Medium incident rate
    UNKNOWN         // Not enough data
}
