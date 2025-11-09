package com.example.background_vol_up_down_app.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.background_vol_up_down_app.data.local.entities.JourneyStatus
import com.example.background_vol_up_down_app.data.local.entities.LocationPoint
import com.example.background_vol_up_down_app.data.local.entities.SafeJourney
import kotlinx.coroutines.flow.Flow

@Dao
interface SafeJourneyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(journey: SafeJourney)

    @Update
    suspend fun update(journey: SafeJourney)

    @Delete
    suspend fun delete(journey: SafeJourney)

    @Query("SELECT * FROM safe_journeys WHERE journeyId = :journeyId")
    suspend fun getJourneyById(journeyId: String): SafeJourney?

    @Query("SELECT * FROM safe_journeys WHERE journeyId = :journeyId")
    fun getJourneyByIdFlow(journeyId: String): Flow<SafeJourney?>

    @Query("SELECT * FROM safe_journeys ORDER BY startTime DESC")
    fun getAllJourneys(): Flow<List<SafeJourney>>

    @Query("SELECT * FROM safe_journeys ORDER BY startTime DESC")
    fun getAllJourneysLiveData(): LiveData<List<SafeJourney>>

    @Query("SELECT * FROM safe_journeys WHERE status = :status ORDER BY startTime DESC")
    fun getJourneysByStatus(status: JourneyStatus): Flow<List<SafeJourney>>

    @Query("SELECT * FROM safe_journeys WHERE status = 'IN_PROGRESS' ORDER BY startTime DESC LIMIT 1")
    suspend fun getCurrentJourney(): SafeJourney?

    @Query("SELECT * FROM safe_journeys WHERE status = 'IN_PROGRESS' ORDER BY startTime DESC LIMIT 1")
    fun getCurrentJourneyFlow(): Flow<SafeJourney?>

    @Query("UPDATE safe_journeys SET status = :status, endTime = :endTime WHERE journeyId = :journeyId")
    suspend fun updateStatus(journeyId: String, status: JourneyStatus, endTime: Long?)

    @Query("UPDATE safe_journeys SET hasDeviationAlert = 1, alertTimestamp = :timestamp WHERE journeyId = :journeyId")
    suspend fun markDeviationAlert(journeyId: String, timestamp: Long)

    @Query("UPDATE safe_journeys SET hasStoppedAlert = 1, alertTimestamp = :timestamp WHERE journeyId = :journeyId")
    suspend fun markStoppedAlert(journeyId: String, timestamp: Long)

    @Query("UPDATE safe_journeys SET isSynced = 1, syncedAt = :syncTime WHERE journeyId = :journeyId")
    suspend fun markAsSynced(journeyId: String, syncTime: Long)

    @Query("SELECT * FROM safe_journeys WHERE isSynced = 0")
    suspend fun getUnsyncedJourneys(): List<SafeJourney>

    @Query("DELETE FROM safe_journeys WHERE startTime < :beforeTimestamp AND status != 'IN_PROGRESS'")
    suspend fun deleteOldJourneys(beforeTimestamp: Long)

    @Query("SELECT COUNT(*) FROM safe_journeys WHERE status = 'IN_PROGRESS'")
    fun getActiveJourneysCount(): LiveData<Int>
}

@Dao
interface LocationPointDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(point: LocationPoint): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(points: List<LocationPoint>)

    @Update
    suspend fun update(point: LocationPoint)

    @Delete
    suspend fun delete(point: LocationPoint)

    @Query("SELECT * FROM location_points WHERE id = :pointId")
    suspend fun getPointById(pointId: Long): LocationPoint?

    @Query("SELECT * FROM location_points WHERE journeyId = :journeyId ORDER BY timestamp ASC")
    suspend fun getPointsForJourney(journeyId: String): List<LocationPoint>

    @Query("SELECT * FROM location_points WHERE journeyId = :journeyId ORDER BY timestamp ASC")
    fun getPointsForJourneyFlow(journeyId: String): Flow<List<LocationPoint>>

    @Query("SELECT * FROM location_points WHERE journeyId = :journeyId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastPointForJourney(journeyId: String): LocationPoint?

    @Query("SELECT * FROM location_points WHERE isSynced = 0 ORDER BY timestamp ASC")
    suspend fun getUnsyncedPoints(): List<LocationPoint>

    @Query("UPDATE location_points SET isSynced = 1, syncedAt = :syncTime WHERE id IN (:pointIds)")
    suspend fun markAsSynced(pointIds: List<Long>, syncTime: Long)

    @Query("DELETE FROM location_points WHERE journeyId = :journeyId")
    suspend fun deletePointsForJourney(journeyId: String)

    @Query("DELETE FROM location_points WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOldPoints(beforeTimestamp: Long)

    @Query("SELECT COUNT(*) FROM location_points WHERE journeyId = :journeyId")
    suspend fun getPointCountForJourney(journeyId: String): Int
}
