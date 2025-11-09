package com.example.background_vol_up_down_app.data.repository

import androidx.lifecycle.LiveData
import com.example.background_vol_up_down_app.data.local.dao.EmergencyEventDao
import com.example.background_vol_up_down_app.data.local.entities.EmergencyEvent
import com.example.background_vol_up_down_app.data.local.entities.EmergencyStatus
import com.example.background_vol_up_down_app.data.local.entities.EmergencyType
import kotlinx.coroutines.flow.Flow

class EmergencyRepository(
    private val emergencyEventDao: EmergencyEventDao
) {

    val allEvents: Flow<List<EmergencyEvent>> = emergencyEventDao.getAllEvents()
    val allEventsLiveData: LiveData<List<EmergencyEvent>> = emergencyEventDao.getAllEventsLiveData()
    val activeEvents: Flow<List<EmergencyEvent>> = emergencyEventDao.getActiveEvents()
    val activeEventsCount: LiveData<Int> = emergencyEventDao.getActiveEventsCount()

    suspend fun insertEvent(event: EmergencyEvent): Long {
        return emergencyEventDao.insert(event)
    }

    suspend fun updateEvent(event: EmergencyEvent) {
        emergencyEventDao.update(event)
    }

    suspend fun deleteEvent(event: EmergencyEvent) {
        emergencyEventDao.delete(event)
    }

    suspend fun getEventById(eventId: Long): EmergencyEvent? {
        return emergencyEventDao.getEventById(eventId)
    }

    fun getEventsByStatus(status: EmergencyStatus): Flow<List<EmergencyEvent>> {
        return emergencyEventDao.getEventsByStatus(status)
    }

    fun getEventsByType(type: EmergencyType): Flow<List<EmergencyEvent>> {
        return emergencyEventDao.getEventsByType(type)
    }

    suspend fun getUnsyncedEvents(): List<EmergencyEvent> {
        return emergencyEventDao.getUnsyncedEvents()
    }

    fun getEventsBetween(startTime: Long, endTime: Long): Flow<List<EmergencyEvent>> {
        return emergencyEventDao.getEventsBetween(startTime, endTime)
    }

    suspend fun markAsSynced(eventId: Long, syncTime: Long, serverId: String) {
        emergencyEventDao.markAsSynced(eventId, syncTime, serverId)
    }

    suspend fun updateStatus(eventId: Long, status: EmergencyStatus, resolvedAt: Long? = null) {
        emergencyEventDao.updateStatus(eventId, status, resolvedAt)
    }

    suspend fun markSmsSent(eventId: Long, recipients: String) {
        emergencyEventDao.markSmsSent(eventId, recipients)
    }

    suspend fun deleteOldEvents(daysOld: Int = 90) {
        val beforeTimestamp = System.currentTimeMillis() - (daysOld * 24 * 60 * 60 * 1000L)
        emergencyEventDao.deleteOldEvents(beforeTimestamp)
    }

    suspend fun getEventsInBounds(minLat: Double, maxLat: Double, minLng: Double, maxLng: Double): List<EmergencyEvent> {
        return emergencyEventDao.getEventsInBounds(minLat, maxLat, minLng, maxLng)
    }

    // Create a new emergency event
    suspend fun createEmergencyEvent(
        latitude: Double,
        longitude: Double,
        eventType: EmergencyType,
        address: String? = null,
        locationAccuracy: Float? = null
    ): Long {
        val event = EmergencyEvent(
            latitude = latitude,
            longitude = longitude,
            address = address,
            locationAccuracy = locationAccuracy,
            eventType = eventType,
            status = EmergencyStatus.ACTIVE
        )
        return insertEvent(event)
    }

    // Resolve an emergency
    suspend fun resolveEmergency(eventId: Long, status: EmergencyStatus = EmergencyStatus.RESOLVED) {
        updateStatus(eventId, status, System.currentTimeMillis())
    }
}
