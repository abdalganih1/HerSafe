package com.example.background_vol_up_down_app.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.background_vol_up_down_app.data.local.entities.EmergencyEvent
import com.example.background_vol_up_down_app.data.local.entities.EmergencyStatus
import com.example.background_vol_up_down_app.data.local.entities.EmergencyType
import kotlinx.coroutines.flow.Flow

@Dao
interface EmergencyEventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: EmergencyEvent): Long

    @Update
    suspend fun update(event: EmergencyEvent)

    @Delete
    suspend fun delete(event: EmergencyEvent)

    @Query("SELECT * FROM emergency_events WHERE id = :eventId")
    suspend fun getEventById(eventId: Long): EmergencyEvent?

    @Query("SELECT * FROM emergency_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<EmergencyEvent>>

    @Query("SELECT * FROM emergency_events ORDER BY timestamp DESC")
    fun getAllEventsLiveData(): LiveData<List<EmergencyEvent>>

    @Query("SELECT * FROM emergency_events WHERE status = :status ORDER BY timestamp DESC")
    fun getEventsByStatus(status: EmergencyStatus): Flow<List<EmergencyEvent>>

    @Query("SELECT * FROM emergency_events WHERE eventType = :type ORDER BY timestamp DESC")
    fun getEventsByType(type: EmergencyType): Flow<List<EmergencyEvent>>

    @Query("SELECT * FROM emergency_events WHERE isSynced = 0 ORDER BY timestamp ASC")
    suspend fun getUnsyncedEvents(): List<EmergencyEvent>

    @Query("SELECT * FROM emergency_events WHERE status = 'ACTIVE' ORDER BY timestamp DESC")
    fun getActiveEvents(): Flow<List<EmergencyEvent>>

    @Query("SELECT * FROM emergency_events WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getEventsBetween(startTime: Long, endTime: Long): Flow<List<EmergencyEvent>>

    @Query("UPDATE emergency_events SET isSynced = 1, syncedAt = :syncTime, serverEventId = :serverId WHERE id = :eventId")
    suspend fun markAsSynced(eventId: Long, syncTime: Long, serverId: String)

    @Query("UPDATE emergency_events SET status = :status, resolvedAt = :resolvedAt WHERE id = :eventId")
    suspend fun updateStatus(eventId: Long, status: EmergencyStatus, resolvedAt: Long?)

    @Query("UPDATE emergency_events SET smsSent = 1, smsRecipients = :recipients WHERE id = :eventId")
    suspend fun markSmsSent(eventId: Long, recipients: String)

    @Query("DELETE FROM emergency_events WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOldEvents(beforeTimestamp: Long)

    @Query("SELECT COUNT(*) FROM emergency_events WHERE status = 'ACTIVE'")
    fun getActiveEventsCount(): LiveData<Int>

    @Query("SELECT * FROM emergency_events WHERE latitude BETWEEN :minLat AND :maxLat AND longitude BETWEEN :minLng AND :maxLng")
    suspend fun getEventsInBounds(minLat: Double, maxLat: Double, minLng: Double, maxLng: Double): List<EmergencyEvent>
}
