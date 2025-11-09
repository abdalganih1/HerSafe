package com.example.background_vol_up_down_app.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.ServiceCompat
import com.example.background_vol_up_down_app.data.local.database.HerSafeDatabase
import com.example.background_vol_up_down_app.data.repository.*
import com.example.background_vol_up_down_app.utils.EmergencyManager
import com.example.background_vol_up_down_app.utils.NotificationHelper
import com.example.background_vol_up_down_app.utils.PreferencesHelper

/**
 * MonitoringService
 *
 * Foreground service that runs in the background to monitor for emergency triggers.
 *
 * NOTE: Volume button detection is extremely difficult on Android without root access
 * or AccessibilityService. This service provides the infrastructure for emergency
 * monitoring, but the actual trigger mechanism needs to be:
 *
 * 1. AccessibilityService (complex, requires user to enable in settings)
 * 2. Hardware key override (not reliable across devices)
 * 3. In-app floating button (most reliable alternative)
 * 4. Widget button (good alternative)
 *
 * For MVP, we recommend using an in-app emergency button or widget.
 */
class MonitoringService : Service() {

    private lateinit var emergencyManager: EmergencyManager
    private lateinit var preferencesHelper: PreferencesHelper

    companion object {
        private const val TAG = "MonitoringService"
        const val ACTION_START_MONITORING = "action_start_monitoring"
        const val ACTION_STOP_MONITORING = "action_stop_monitoring"
        const val ACTION_TRIGGER_EMERGENCY = "action_trigger_emergency"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")

        // Initialize database and repositories
        val database = HerSafeDatabase.getDatabase(this)
        val emergencyRepository = EmergencyRepository(database.emergencyEventDao())
        val trustedContactRepository = TrustedContactRepository(database.trustedContactDao())
        val locationRepository = LocationRepository(this)
        val safeZoneRepository = SafeZoneRepository(database.safeZoneDao())

        emergencyManager = EmergencyManager(
            this,
            emergencyRepository,
            trustedContactRepository,
            locationRepository,
            safeZoneRepository
        )

        preferencesHelper = PreferencesHelper(this)

        // Create notification channels
        NotificationHelper.createNotificationChannels(this)

        // Start as foreground service
        startForeground(
            NotificationHelper.getMonitoringNotificationId(),
            NotificationHelper.createMonitoringNotification(this)
        )

        Log.d(TAG, "Service started as foreground")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_MONITORING -> {
                Log.d(TAG, "Start monitoring action received")
                preferencesHelper.isMonitoringEnabled = true
                startMonitoring()
            }
            ACTION_STOP_MONITORING -> {
                Log.d(TAG, "Stop monitoring action received")
                preferencesHelper.isMonitoringEnabled = false
                stopMonitoring()
            }
            ACTION_TRIGGER_EMERGENCY -> {
                Log.d(TAG, "Emergency trigger action received")
                handleEmergencyTrigger()
            }
        }

        return START_STICKY // Service will restart if killed
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // Not a bound service
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        preferencesHelper.isMonitoringEnabled = false
    }

    private fun startMonitoring() {
        Log.d(TAG, "Monitoring started")
        // TODO: Initialize volume button monitoring (complex)
        // For now, service is just running in foreground
    }

    private fun stopMonitoring() {
        Log.d(TAG, "Monitoring stopped")
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun handleEmergencyTrigger() {
        Log.d(TAG, "Handling emergency trigger")
        emergencyManager.quickEmergencyTrigger()

        // Start recording service if enabled
        if (preferencesHelper.autoRecordAudio || preferencesHelper.autoRecordVideo) {
            val recordingIntent = Intent(this, EmergencyRecordingService::class.java).apply {
                action = EmergencyRecordingService.ACTION_START_RECORDING
            }
            startService(recordingIntent)
        }
    }
}
