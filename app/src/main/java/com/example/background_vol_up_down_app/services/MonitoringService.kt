package com.example.background_vol_up_down_app.services

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ServiceCompat
import com.example.background_vol_up_down_app.data.local.database.HerSafeDatabase
import com.example.background_vol_up_down_app.data.repository.*
import com.example.background_vol_up_down_app.utils.EmergencyManager
import com.example.background_vol_up_down_app.utils.NotificationHelper
import com.example.background_vol_up_down_app.utils.PreferencesHelper
import com.example.background_vol_up_down_app.utils.VolumeButtonDetector

/**
 * MonitoringService
 *
 * Foreground service that monitors volume button presses for emergency triggers:
 * - 2 consecutive volume down presses within 1 second
 * - 1 volume down press held for 3 seconds
 */
class MonitoringService : Service() {

    private lateinit var emergencyManager: EmergencyManager
    private lateinit var preferencesHelper: PreferencesHelper
    private lateinit var volumeDetector: VolumeButtonDetector
    private val handler = Handler(Looper.getMainLooper())
    private var isMonitoring = false

    companion object {
        private const val TAG = "MonitoringService"
        const val ACTION_START_MONITORING = "action_start_monitoring"
        const val ACTION_STOP_MONITORING = "action_stop_monitoring"
        const val ACTION_TRIGGER_EMERGENCY = "action_trigger_emergency"
        private const val VOLUME_CHECK_INTERVAL_MS = 200L // Check every 200ms
    }

    private val volumeCheckRunnable = object : Runnable {
        override fun run() {
            if (isMonitoring) {
                volumeDetector.checkVolumeChange()
                handler.postDelayed(this, VOLUME_CHECK_INTERVAL_MS)
            }
        }
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

        // Initialize volume button detector
        volumeDetector = VolumeButtonDetector(this) {
            Log.i(TAG, "Volume button emergency trigger activated!")
            handleEmergencyTrigger()
        }

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
        isMonitoring = false
        handler.removeCallbacks(volumeCheckRunnable)
        volumeDetector.cleanup()
        preferencesHelper.isMonitoringEnabled = false
    }

    private fun startMonitoring() {
        Log.d(TAG, "Monitoring started - Volume button detection active")
        isMonitoring = true
        handler.post(volumeCheckRunnable)
    }

    private fun stopMonitoring() {
        Log.d(TAG, "Monitoring stopped")
        isMonitoring = false
        handler.removeCallbacks(volumeCheckRunnable)
        volumeDetector.cleanup()
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
