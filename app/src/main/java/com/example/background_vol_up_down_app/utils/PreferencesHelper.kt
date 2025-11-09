package com.example.background_vol_up_down_app.utils

import android.content.Context
import android.content.SharedPreferences

class PreferencesHelper(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "hersafe_prefs"

        // Keys
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_MONITORING_ENABLED = "monitoring_enabled"
        private const val KEY_AUTO_RECORD_AUDIO = "auto_record_audio"
        private const val KEY_AUTO_RECORD_VIDEO = "auto_record_video"
        private const val KEY_AUTO_LIVE_STREAM = "auto_live_stream"
        private const val KEY_VOLUME_BUTTON_DURATION = "volume_button_duration"
        private const val KEY_JOURNEY_TRACKING_INTERVAL = "journey_tracking_interval"
        private const val KEY_DEVIATION_THRESHOLD_METERS = "deviation_threshold_meters"
        private const val KEY_STOPPED_ALERT_MINUTES = "stopped_alert_minutes"
        private const val KEY_AUTO_SYNC_ENABLED = "auto_sync_enabled"
        private const val KEY_SYNC_WIFI_ONLY = "sync_wifi_only"
        private const val KEY_LAST_SYNC_TIMESTAMP = "last_sync_timestamp"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_PHONE = "user_phone"
        private const val KEY_EMERGENCY_MESSAGE = "emergency_message"
    }

    // First launch
    var isFirstLaunch: Boolean
        get() = prefs.getBoolean(KEY_FIRST_LAUNCH, true)
        set(value) = prefs.edit().putBoolean(KEY_FIRST_LAUNCH, value).apply()

    // Monitoring settings
    var isMonitoringEnabled: Boolean
        get() = prefs.getBoolean(KEY_MONITORING_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_MONITORING_ENABLED, value).apply()

    // Recording settings
    var autoRecordAudio: Boolean
        get() = prefs.getBoolean(KEY_AUTO_RECORD_AUDIO, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_RECORD_AUDIO, value).apply()

    var autoRecordVideo: Boolean
        get() = prefs.getBoolean(KEY_AUTO_RECORD_VIDEO, false)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_RECORD_VIDEO, value).apply()

    var autoLiveStream: Boolean
        get() = prefs.getBoolean(KEY_AUTO_LIVE_STREAM, false)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_LIVE_STREAM, value).apply()

    // Volume button settings
    var volumeButtonDuration: Long
        get() = prefs.getLong(KEY_VOLUME_BUTTON_DURATION, 3000) // 3 seconds default
        set(value) = prefs.edit().putLong(KEY_VOLUME_BUTTON_DURATION, value).apply()

    // Journey settings
    var journeyTrackingIntervalSeconds: Int
        get() = prefs.getInt(KEY_JOURNEY_TRACKING_INTERVAL, 30) // 30 seconds default
        set(value) = prefs.edit().putInt(KEY_JOURNEY_TRACKING_INTERVAL, value).apply()

    var deviationThresholdMeters: Float
        get() = prefs.getFloat(KEY_DEVIATION_THRESHOLD_METERS, 500f) // 500 meters default
        set(value) = prefs.edit().putFloat(KEY_DEVIATION_THRESHOLD_METERS, value).apply()

    var stoppedAlertMinutes: Int
        get() = prefs.getInt(KEY_STOPPED_ALERT_MINUTES, 5) // 5 minutes default
        set(value) = prefs.edit().putInt(KEY_STOPPED_ALERT_MINUTES, value).apply()

    // Sync settings
    var autoSyncEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_SYNC_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_SYNC_ENABLED, value).apply()

    var syncWifiOnly: Boolean
        get() = prefs.getBoolean(KEY_SYNC_WIFI_ONLY, false)
        set(value) = prefs.edit().putBoolean(KEY_SYNC_WIFI_ONLY, value).apply()

    var lastSyncTimestamp: Long
        get() = prefs.getLong(KEY_LAST_SYNC_TIMESTAMP, 0)
        set(value) = prefs.edit().putLong(KEY_LAST_SYNC_TIMESTAMP, value).apply()

    // User info
    var userName: String?
        get() = prefs.getString(KEY_USER_NAME, null)
        set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()

    var userPhone: String?
        get() = prefs.getString(KEY_USER_PHONE, null)
        set(value) = prefs.edit().putString(KEY_USER_PHONE, value).apply()

    // Custom emergency message
    var emergencyMessage: String?
        get() = prefs.getString(KEY_EMERGENCY_MESSAGE, null)
        set(value) = prefs.edit().putString(KEY_EMERGENCY_MESSAGE, value).apply()

    // Get default emergency message
    fun getDefaultEmergencyMessage(): String {
        return "⚠️ تنبيه طوارئ من HerSafe! أحتاج المساعدة فوراً!"
    }

    // Get effective emergency message (custom or default)
    fun getEffectiveEmergencyMessage(): String {
        return emergencyMessage ?: getDefaultEmergencyMessage()
    }

    // Clear all preferences
    fun clearAll() {
        prefs.edit().clear().apply()
    }

    // Reset to defaults
    fun resetToDefaults() {
        prefs.edit().apply {
            putBoolean(KEY_MONITORING_ENABLED, false)
            putBoolean(KEY_AUTO_RECORD_AUDIO, true)
            putBoolean(KEY_AUTO_RECORD_VIDEO, false)
            putBoolean(KEY_AUTO_LIVE_STREAM, false)
            putLong(KEY_VOLUME_BUTTON_DURATION, 3000)
            putInt(KEY_JOURNEY_TRACKING_INTERVAL, 30)
            putFloat(KEY_DEVIATION_THRESHOLD_METERS, 500f)
            putInt(KEY_STOPPED_ALERT_MINUTES, 5)
            putBoolean(KEY_AUTO_SYNC_ENABLED, true)
            putBoolean(KEY_SYNC_WIFI_ONLY, false)
        }.apply()
    }

    // Get all settings as map for debugging
    fun getAllSettings(): Map<String, Any?> {
        return mapOf(
            "isMonitoringEnabled" to isMonitoringEnabled,
            "autoRecordAudio" to autoRecordAudio,
            "autoRecordVideo" to autoRecordVideo,
            "autoLiveStream" to autoLiveStream,
            "volumeButtonDuration" to volumeButtonDuration,
            "journeyTrackingIntervalSeconds" to journeyTrackingIntervalSeconds,
            "deviationThresholdMeters" to deviationThresholdMeters,
            "stoppedAlertMinutes" to stoppedAlertMinutes,
            "autoSyncEnabled" to autoSyncEnabled,
            "syncWifiOnly" to syncWifiOnly,
            "userName" to userName,
            "userPhone" to userPhone
        )
    }
}
