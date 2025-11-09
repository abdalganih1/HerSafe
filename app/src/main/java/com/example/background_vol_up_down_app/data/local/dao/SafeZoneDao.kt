package com.example.background_vol_up_down_app.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.background_vol_up_down_app.data.local.entities.SafeZone
import com.example.background_vol_up_down_app.data.local.entities.ZoneType
import kotlinx.coroutines.flow.Flow

@Dao
interface SafeZoneDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(zone: SafeZone): Long

    @Update
    suspend fun update(zone: SafeZone)

    @Delete
    suspend fun delete(zone: SafeZone)

    @Query("SELECT * FROM safe_zones WHERE id = :zoneId")
    suspend fun getZoneById(zoneId: Long): SafeZone?

    @Query("SELECT * FROM safe_zones ORDER BY safetyScore ASC")
    fun getAllZones(): Flow<List<SafeZone>>

    @Query("SELECT * FROM safe_zones ORDER BY safetyScore ASC")
    fun getAllZonesLiveData(): LiveData<List<SafeZone>>

    @Query("SELECT * FROM safe_zones WHERE zoneType = :type ORDER BY safetyScore ASC")
    fun getZonesByType(type: ZoneType): Flow<List<SafeZone>>

    @Query("SELECT * FROM safe_zones WHERE isUserDefined = 1 ORDER BY name ASC")
    fun getUserDefinedZones(): Flow<List<SafeZone>>

    @Query("SELECT * FROM safe_zones WHERE safetyScore < 30 ORDER BY safetyScore ASC")
    fun getDangerousZones(): Flow<List<SafeZone>>

    @Query("SELECT * FROM safe_zones WHERE safetyScore >= 70 ORDER BY safetyScore DESC")
    fun getSafeZones(): Flow<List<SafeZone>>

    @Query("SELECT * FROM safe_zones WHERE latitude BETWEEN :minLat AND :maxLat AND longitude BETWEEN :minLng AND :maxLng")
    suspend fun getZonesInBounds(minLat: Double, maxLat: Double, minLng: Double, maxLng: Double): List<SafeZone>

    @Query("SELECT * FROM safe_zones WHERE isSynced = 0")
    suspend fun getUnsyncedZones(): List<SafeZone>

    @Query("UPDATE safe_zones SET isSynced = 1, updatedAt = :syncTime, serverZoneId = :serverId WHERE id = :zoneId")
    suspend fun markAsSynced(zoneId: Long, syncTime: Long, serverId: String)

    @Query("UPDATE safe_zones SET incidentCount = incidentCount + 1, lastIncidentTimestamp = :timestamp, safetyScore = :newScore WHERE id = :zoneId")
    suspend fun incrementIncidentCount(zoneId: Long, timestamp: Long, newScore: Int)

    @Query("DELETE FROM safe_zones WHERE isUserDefined = 0 AND updatedAt < :beforeTimestamp")
    suspend fun deleteOldCalculatedZones(beforeTimestamp: Long)

    @Query("SELECT COUNT(*) FROM safe_zones WHERE zoneType = 'DANGEROUS'")
    fun getDangerousZonesCount(): LiveData<Int>

    // Find zones within a certain radius of a point
    @Query("""
        SELECT * FROM safe_zones
        WHERE (
            (latitude - :lat) * (latitude - :lat) +
            (longitude - :lng) * (longitude - :lng)
        ) <= (:radiusKm * :radiusKm / 12321.0)
        ORDER BY (
            (latitude - :lat) * (latitude - :lat) +
            (longitude - :lng) * (longitude - :lng)
        ) ASC
    """)
    suspend fun getZonesNearLocation(lat: Double, lng: Double, radiusKm: Double = 5.0): List<SafeZone>
}
