package com.example.background_vol_up_down_app.services

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.ServiceCompat
import com.example.background_vol_up_down_app.data.local.database.HerSafeDatabase
import com.example.background_vol_up_down_app.data.local.entities.JourneyStatus
import com.example.background_vol_up_down_app.data.repository.LocationRepository
import com.example.background_vol_up_down_app.data.repository.SafeJourneyRepository
import com.example.background_vol_up_down_app.data.repository.TrustedContactRepository
import com.example.background_vol_up_down_app.utils.NotificationHelper
import com.example.background_vol_up_down_app.utils.PreferencesHelper
import com.example.background_vol_up_down_app.utils.SmsHelper
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel

class LocationTrackingService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var safeJourneyRepository: SafeJourneyRepository
    private lateinit var trustedContactRepository: TrustedContactRepository
    private lateinit var locationRepository: LocationRepository
    private lateinit var preferencesHelper: PreferencesHelper
    private val scope = CoroutineScope(Dispatchers.IO)

    private var currentJourneyId: String? = null
    private var destinationLat: Double = 0.0
    private var destinationLng: Double = 0.0
    private var lastLocation: Location? = null
    private var lastMovementTime: Long = System.currentTimeMillis()

    companion object {
        private const val TAG = "LocationTrackingService"
        const val ACTION_START_JOURNEY = "action_start_journey"
        const val ACTION_STOP_JOURNEY = "action_stop_journey"
        const val ACTION_COMPLETE_JOURNEY = "action_complete_journey"

        const val EXTRA_START_LAT = "start_lat"
        const val EXTRA_START_LNG = "start_lng"
        const val EXTRA_START_ADDRESS = "start_address"
        const val EXTRA_DEST_LAT = "dest_lat"
        const val EXTRA_DEST_LNG = "dest_lng"
        const val EXTRA_DEST_ADDRESS = "dest_address"
        const val EXTRA_CONTACTS = "contacts"

        private const val MIN_MOVEMENT_DISTANCE = 10f // meters
        private const val DESTINATION_RADIUS = 50f // meters
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")

        // Initialize
        val database = HerSafeDatabase.getDatabase(this)
        safeJourneyRepository = SafeJourneyRepository(
            database.safeJourneyDao(),
            database.locationPointDao()
        )
        trustedContactRepository = TrustedContactRepository(database.trustedContactDao())
        locationRepository = LocationRepository(this)
        preferencesHelper = PreferencesHelper(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Setup location callback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                handleLocationUpdate(locationResult.lastLocation)
            }
        }

        // Create notification channels
        NotificationHelper.createNotificationChannels(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_JOURNEY -> {
                val startLat = intent.getDoubleExtra(EXTRA_START_LAT, 0.0)
                val startLng = intent.getDoubleExtra(EXTRA_START_LNG, 0.0)
                val startAddress = intent.getStringExtra(EXTRA_START_ADDRESS)
                val destLat = intent.getDoubleExtra(EXTRA_DEST_LAT, 0.0)
                val destLng = intent.getDoubleExtra(EXTRA_DEST_LNG, 0.0)
                val destAddress = intent.getStringExtra(EXTRA_DEST_ADDRESS)
                val contacts = intent.getStringExtra(EXTRA_CONTACTS)

                startJourney(startLat, startLng, startAddress, destLat, destLng, destAddress, contacts)
            }
            ACTION_STOP_JOURNEY -> {
                stopJourney(JourneyStatus.CANCELLED)
            }
            ACTION_COMPLETE_JOURNEY -> {
                stopJourney(JourneyStatus.COMPLETED)
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        stopLocationUpdates()
        scope.cancel()
    }

    private fun startJourney(
        startLat: Double,
        startLng: Double,
        startAddress: String?,
        destLat: Double,
        destLng: Double,
        destAddress: String?,
        contacts: String?
    ) {
        Log.d(TAG, "Starting journey from ($startLat, $startLng) to ($destLat, $destLng)")

        destinationLat = destLat
        destinationLng = destLng

        // Start foreground service
        startForeground(
            NotificationHelper.getJourneyNotificationId(),
            NotificationHelper.createJourneyNotification(this, destAddress ?: "الوجهة")
        )

        scope.launch {
            try {
                // Create journey in database
                currentJourneyId = safeJourneyRepository.startNewJourney(
                    startLat = startLat,
                    startLon = startLng,
                    destLat = destLat,
                    destLon = destLng,
                    startAddress = startAddress,
                    destAddress = destAddress,
                    notifiedContacts = contacts
                )

                Log.d(TAG, "Journey created with ID: $currentJourneyId")

                // Send start SMS to contacts
                if (contacts != null) {
                    val contactsList = trustedContactRepository.getSmsEnabledContacts()
                    SmsHelper.sendSafeJourneyStartSms(
                        contactsList,
                        startAddress,
                        destAddress,
                        null
                    )
                }

                // Start location updates
                startLocationUpdates()

            } catch (e: Exception) {
                Log.e(TAG, "Error starting journey", e)
            }
        }
    }

    private fun stopJourney(status: JourneyStatus) {
        Log.d(TAG, "Stopping journey with status: $status")

        val journeyId = currentJourneyId ?: return

        scope.launch {
            try {
                // End journey in database
                safeJourneyRepository.endJourney(journeyId, status)

                // Send completion SMS if completed successfully
                if (status == JourneyStatus.COMPLETED) {
                    val contacts = trustedContactRepository.getSmsEnabledContacts()
                    val journey = safeJourneyRepository.getJourneyById(journeyId)
                    SmsHelper.sendSafeJourneyCompleteSms(
                        contacts,
                        journey?.destinationAddress
                    )
                }

                // Cancel notification
                NotificationHelper.cancelJourneyNotification(this@LocationTrackingService)

                // Stop service
                stopLocationUpdates()
                ServiceCompat.stopForeground(this@LocationTrackingService, ServiceCompat.STOP_FOREGROUND_REMOVE)
                stopSelf()

            } catch (e: Exception) {
                Log.e(TAG, "Error stopping journey", e)
            }
        }
    }

    private fun startLocationUpdates() {
        if (!hasLocationPermission()) {
            Log.e(TAG, "Location permission not granted")
            return
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            (preferencesHelper.journeyTrackingIntervalSeconds * 1000).toLong()
        ).apply {
            setMinUpdateIntervalMillis(15000) // Minimum 15 seconds
            setMaxUpdateDelayMillis(60000) // Maximum 1 minute
        }.build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            Log.d(TAG, "Location updates started")
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception starting location updates", e)
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        Log.d(TAG, "Location updates stopped")
    }

    private fun handleLocationUpdate(location: Location?) {
        location ?: return
        val journeyId = currentJourneyId ?: return

        Log.d(TAG, "Location update: ${location.latitude}, ${location.longitude}")

        scope.launch {
            try {
                // Save location point
                safeJourneyRepository.addLocationToJourney(
                    journeyId = journeyId,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracy = location.accuracy,
                    speed = location.speed,
                    bearing = location.bearing
                )

                // Check if reached destination
                val distanceToDestination = locationRepository.calculateDistance(
                    location.latitude, location.longitude,
                    destinationLat, destinationLng
                )

                if (distanceToDestination <= DESTINATION_RADIUS) {
                    Log.d(TAG, "Reached destination!")
                    stopJourney(JourneyStatus.COMPLETED)
                    return@launch
                }

                // Check for movement (stopped alert)
                if (lastLocation != null) {
                    val distanceMoved = locationRepository.calculateDistance(
                        lastLocation!!.latitude, lastLocation!!.longitude,
                        location.latitude, location.longitude
                    )

                    if (distanceMoved >= MIN_MOVEMENT_DISTANCE) {
                        lastMovementTime = System.currentTimeMillis()
                    } else {
                        // Check if stopped for too long
                        val stoppedDuration = System.currentTimeMillis() - lastMovementTime
                        val stoppedThreshold = preferencesHelper.stoppedAlertMinutes * 60 * 1000L

                        if (stoppedDuration >= stoppedThreshold) {
                            handleStoppedAlert(location)
                        }
                    }
                }

                lastLocation = location

            } catch (e: Exception) {
                Log.e(TAG, "Error handling location update", e)
            }
        }
    }

    private suspend fun handleStoppedAlert(location: Location) {
        val journeyId = currentJourneyId ?: return

        Log.d(TAG, "Stopped alert triggered")

        // Mark stopped alert in journey
        safeJourneyRepository.markStoppedAlert(journeyId)

        // Get address
        val address = locationRepository.getAddressFromLocation(location.latitude, location.longitude)

        // Send alert SMS
        val contacts = trustedContactRepository.getSmsEnabledContacts()
        SmsHelper.sendStoppedAlertSms(
            contacts,
            address,
            location.latitude,
            location.longitude,
            preferencesHelper.stoppedAlertMinutes
        )

        // Show notification
        NotificationHelper.showEmergencyNotification(
            this,
            "تنبيه: لم تتحرك لمدة ${preferencesHelper.stoppedAlertMinutes} دقيقة"
        )
    }

    private fun hasLocationPermission(): Boolean {
        val fineLocation = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val backgroundLocation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        return fineLocation && backgroundLocation
    }
}
