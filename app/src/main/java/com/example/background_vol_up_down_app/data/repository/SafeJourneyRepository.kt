package com.example.background_vol_up_down_app.data.repository

import androidx.lifecycle.LiveData
import com.example.background_vol_up_down_app.data.local.dao.LocationPointDao
import com.example.background_vol_up_down_app.data.local.dao.SafeJourneyDao
import com.example.background_vol_up_down_app.data.local.entities.JourneyStatus
import com.example.background_vol_up_down_app.data.local.entities.LocationPoint
import com.example.background_vol_up_down_app.data.local.entities.SafeJourney
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class SafeJourneyRepository(
    private val safeJourneyDao: SafeJourneyDao,
    private val locationPointDao: LocationPointDao
) {

    val allJourneys: Flow<List<SafeJourney>> = safeJourneyDao.getAllJourneys()
    val allJourneysLiveData: LiveData<List<SafeJourney>> = safeJourneyDao.getAllJourneysLiveData()
    val currentJourney: Flow<SafeJourney?> = safeJourneyDao.getCurrentJourneyFlow()
    val activeJourneysCount: LiveData<Int> = safeJourneyDao.getActiveJourneysCount()

    // Journey operations
    suspend fun insertJourney(journey: SafeJourney) {
        safeJourneyDao.insert(journey)
    }

    suspend fun updateJourney(journey: SafeJourney) {
        safeJourneyDao.update(journey)
    }

    suspend fun deleteJourney(journey: SafeJourney) {
        safeJourneyDao.delete(journey)
    }

    suspend fun getJourneyById(journeyId: String): SafeJourney? {
        return safeJourneyDao.getJourneyById(journeyId)
    }

    fun getJourneyByIdFlow(journeyId: String): Flow<SafeJourney?> {
        return safeJourneyDao.getJourneyByIdFlow(journeyId)
    }

    fun getJourneysByStatus(status: JourneyStatus): Flow<List<SafeJourney>> {
        return safeJourneyDao.getJourneysByStatus(status)
    }

    suspend fun getCurrentActiveJourney(): SafeJourney? {
        return safeJourneyDao.getCurrentJourney()
    }

    suspend fun updateJourneyStatus(journeyId: String, status: JourneyStatus, endTime: Long? = null) {
        safeJourneyDao.updateStatus(journeyId, status, endTime)
    }

    suspend fun markDeviationAlert(journeyId: String) {
        safeJourneyDao.markDeviationAlert(journeyId, System.currentTimeMillis())
    }

    suspend fun markStoppedAlert(journeyId: String) {
        safeJourneyDao.markStoppedAlert(journeyId, System.currentTimeMillis())
    }

    suspend fun deleteOldJourneys(daysOld: Int = 30) {
        val beforeTimestamp = System.currentTimeMillis() - (daysOld * 24 * 60 * 60 * 1000L)
        safeJourneyDao.deleteOldJourneys(beforeTimestamp)
    }

    // Location point operations
    suspend fun insertLocationPoint(point: LocationPoint): Long {
        return locationPointDao.insert(point)
    }

    suspend fun insertLocationPoints(points: List<LocationPoint>) {
        locationPointDao.insertAll(points)
    }

    suspend fun getPointsForJourney(journeyId: String): List<LocationPoint> {
        return locationPointDao.getPointsForJourney(journeyId)
    }

    fun getPointsForJourneyFlow(journeyId: String): Flow<List<LocationPoint>> {
        return locationPointDao.getPointsForJourneyFlow(journeyId)
    }

    suspend fun getLastPointForJourney(journeyId: String): LocationPoint? {
        return locationPointDao.getLastPointForJourney(journeyId)
    }

    suspend fun deletePointsForJourney(journeyId: String) {
        locationPointDao.deletePointsForJourney(journeyId)
    }

    suspend fun getPointCountForJourney(journeyId: String): Int {
        return locationPointDao.getPointCountForJourney(journeyId)
    }

    // Helper functions
    suspend fun startNewJourney(
        startLat: Double,
        startLon: Double,
        destLat: Double,
        destLon: Double,
        startAddress: String? = null,
        destAddress: String? = null,
        notifiedContacts: String? = null
    ): String {
        val journeyId = UUID.randomUUID().toString()
        val journey = SafeJourney(
            journeyId = journeyId,
            startLatitude = startLat,
            startLongitude = startLon,
            startAddress = startAddress,
            destinationLatitude = destLat,
            destinationLongitude = destLon,
            destinationAddress = destAddress,
            notifiedContacts = notifiedContacts,
            status = JourneyStatus.IN_PROGRESS
        )
        insertJourney(journey)
        return journeyId
    }

    suspend fun endJourney(journeyId: String, status: JourneyStatus = JourneyStatus.COMPLETED) {
        val journey = getJourneyById(journeyId)
        if (journey != null) {
            val endTime = System.currentTimeMillis()
            val durationMinutes = ((endTime - journey.startTime) / 60000).toInt()

            val updatedJourney = journey.copy(
                endTime = endTime,
                actualDurationMinutes = durationMinutes,
                status = status
            )
            updateJourney(updatedJourney)
        }
    }

    suspend fun addLocationToJourney(
        journeyId: String,
        latitude: Double,
        longitude: Double,
        accuracy: Float,
        speed: Float? = null,
        bearing: Float? = null,
        batteryLevel: Int? = null
    ): Long {
        val point = LocationPoint(
            journeyId = journeyId,
            latitude = latitude,
            longitude = longitude,
            accuracy = accuracy,
            speed = speed,
            bearing = bearing,
            batteryLevel = batteryLevel
        )
        return insertLocationPoint(point)
    }

    suspend fun getUnsyncedJourneys(): List<SafeJourney> {
        return safeJourneyDao.getUnsyncedJourneys()
    }

    suspend fun getUnsyncedLocationPoints(): List<LocationPoint> {
        return locationPointDao.getUnsyncedPoints()
    }

    suspend fun markJourneyAsSynced(journeyId: String) {
        safeJourneyDao.markAsSynced(journeyId, System.currentTimeMillis())
    }

    suspend fun markLocationPointsAsSynced(pointIds: List<Long>) {
        locationPointDao.markAsSynced(pointIds, System.currentTimeMillis())
    }
}
