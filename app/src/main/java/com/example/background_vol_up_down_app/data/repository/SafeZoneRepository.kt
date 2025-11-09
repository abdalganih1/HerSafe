package com.example.background_vol_up_down_app.data.repository

import androidx.lifecycle.LiveData
import com.example.background_vol_up_down_app.data.local.dao.SafeZoneDao
import com.example.background_vol_up_down_app.data.local.entities.SafeZone
import com.example.background_vol_up_down_app.data.local.entities.ZoneType
import kotlinx.coroutines.flow.Flow

class SafeZoneRepository(
    private val safeZoneDao: SafeZoneDao
) {

    val allZones: Flow<List<SafeZone>> = safeZoneDao.getAllZones()
    val allZonesLiveData: LiveData<List<SafeZone>> = safeZoneDao.getAllZonesLiveData()
    val dangerousZones: Flow<List<SafeZone>> = safeZoneDao.getDangerousZones()
    val safeZones: Flow<List<SafeZone>> = safeZoneDao.getSafeZones()
    val userDefinedZones: Flow<List<SafeZone>> = safeZoneDao.getUserDefinedZones()
    val dangerousZonesCount: LiveData<Int> = safeZoneDao.getDangerousZonesCount()

    suspend fun insertZone(zone: SafeZone): Long {
        return safeZoneDao.insert(zone)
    }

    suspend fun updateZone(zone: SafeZone) {
        safeZoneDao.update(zone)
    }

    suspend fun deleteZone(zone: SafeZone) {
        safeZoneDao.delete(zone)
    }

    suspend fun getZoneById(zoneId: Long): SafeZone? {
        return safeZoneDao.getZoneById(zoneId)
    }

    fun getZonesByType(type: ZoneType): Flow<List<SafeZone>> {
        return safeZoneDao.getZonesByType(type)
    }

    suspend fun getZonesInBounds(minLat: Double, maxLat: Double, minLng: Double, maxLng: Double): List<SafeZone> {
        return safeZoneDao.getZonesInBounds(minLat, maxLat, minLng, maxLng)
    }

    suspend fun getZonesNearLocation(lat: Double, lng: Double, radiusKm: Double = 5.0): List<SafeZone> {
        return safeZoneDao.getZonesNearLocation(lat, lng, radiusKm)
    }

    suspend fun incrementIncidentCount(zoneId: Long, newSafetyScore: Int) {
        safeZoneDao.incrementIncidentCount(zoneId, System.currentTimeMillis(), newSafetyScore)
    }

    suspend fun deleteOldCalculatedZones(daysOld: Int = 180) {
        val beforeTimestamp = System.currentTimeMillis() - (daysOld * 24 * 60 * 60 * 1000L)
        safeZoneDao.deleteOldCalculatedZones(beforeTimestamp)
    }

    suspend fun getUnsyncedZones(): List<SafeZone> {
        return safeZoneDao.getUnsyncedZones()
    }

    suspend fun markAsSynced(zoneId: Long, serverId: String) {
        safeZoneDao.markAsSynced(zoneId, System.currentTimeMillis(), serverId)
    }

    // Helper functions
    suspend fun createUserSafeZone(
        name: String,
        latitude: Double,
        longitude: Double,
        radiusMeters: Float = 100f,
        address: String? = null
    ): Long {
        val zone = SafeZone(
            name = name,
            latitude = latitude,
            longitude = longitude,
            radiusMeters = radiusMeters,
            address = address,
            safetyScore = 100,
            zoneType = ZoneType.SAFE,
            isUserDefined = true
        )
        return insertZone(zone)
    }

    suspend fun recordIncidentAtLocation(latitude: Double, longitude: Double): Long? {
        // Find existing zone near this location
        val nearbyZones = getZonesNearLocation(latitude, longitude, 0.5) // 500m radius

        if (nearbyZones.isNotEmpty()) {
            // Update existing zone
            val zone = nearbyZones.first()
            val newSafetyScore = maxOf(0, zone.safetyScore - 10) // Decrease safety score
            incrementIncidentCount(zone.id, newSafetyScore)
            return zone.id
        } else {
            // Create new zone
            val newZone = SafeZone(
                latitude = latitude,
                longitude = longitude,
                safetyScore = 40, // Start with medium-low safety
                incidentCount = 1,
                lastIncidentTimestamp = System.currentTimeMillis(),
                zoneType = ZoneType.WARNING
            )
            return insertZone(newZone)
        }
    }

    // Check if location is in a dangerous zone
    suspend fun isLocationDangerous(latitude: Double, longitude: Double): Boolean {
        val nearbyZones = getZonesNearLocation(latitude, longitude, 0.5)
        return nearbyZones.any { it.safetyScore < 30 }
    }

    // Get safety score for location
    suspend fun getSafetyScoreForLocation(latitude: Double, longitude: Double): Int {
        val nearbyZones = getZonesNearLocation(latitude, longitude, 0.5)
        return if (nearbyZones.isNotEmpty()) {
            nearbyZones.minOf { it.safetyScore }
        } else {
            50 // Default neutral score
        }
    }

    // Calculate zone type based on safety score
    fun calculateZoneType(safetyScore: Int): ZoneType {
        return when {
            safetyScore >= 70 -> ZoneType.SAFE
            safetyScore >= 40 -> ZoneType.WARNING
            safetyScore >= 20 -> ZoneType.DANGEROUS
            else -> ZoneType.DANGEROUS
        }
    }
}
