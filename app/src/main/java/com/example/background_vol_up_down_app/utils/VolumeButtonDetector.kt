package com.example.background_vol_up_down_app.utils

import android.content.Context
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.util.Log

/**
 * Detects volume button patterns for emergency triggering
 * Monitors: 2 consecutive down button presses OR 1 press held for 3 seconds
 */
class VolumeButtonDetector(
    private val context: Context,
    private val onEmergencyTriggered: () -> Unit
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private var lastVolumeDownTime = 0L
    private var volumeDownPressCount = 0
    private var isLongPressActive = false
    private var currentVolume = 0

    private val handler = Handler(Looper.getMainLooper())
    private val resetCountRunnable = Runnable { resetPressCount() }
    private val longPressRunnable = Runnable { onLongPressDetected() }

    companion object {
        private const val TAG = "VolumeButtonDetector"
        private const val DOUBLE_PRESS_TIME_WINDOW_MS = 1000L // 1 second
        private const val LONG_PRESS_DURATION_MS = 3000L // 3 seconds
        private const val REQUIRED_PRESS_COUNT = 2
    }

    init {
        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    }

    /**
     * Check volume changes - should be called periodically from service
     */
    fun checkVolumeChange() {
        try {
            val newVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

            if (newVolume < currentVolume) {
                // Volume down detected
                onVolumeDownDetected()
            }

            currentVolume = newVolume
        } catch (e: Exception) {
            Log.e(TAG, "Error checking volume", e)
        }
    }

    private fun onVolumeDownDetected() {
        val currentTime = System.currentTimeMillis()

        Log.d(TAG, "Volume down detected - Count: $volumeDownPressCount")

        // Check if this is within the double-press time window
        if (currentTime - lastVolumeDownTime < DOUBLE_PRESS_TIME_WINDOW_MS) {
            volumeDownPressCount++

            if (volumeDownPressCount >= REQUIRED_PRESS_COUNT) {
                // Double press detected!
                Log.i(TAG, "Emergency: Double volume down press detected")
                triggerEmergency()
                return
            }
        } else {
            // Reset count if too much time passed
            volumeDownPressCount = 1
        }

        lastVolumeDownTime = currentTime

        // Start long press detection
        if (!isLongPressActive) {
            isLongPressActive = true
            handler.postDelayed(longPressRunnable, LONG_PRESS_DURATION_MS)
        }

        // Schedule count reset
        handler.removeCallbacks(resetCountRunnable)
        handler.postDelayed(resetCountRunnable, DOUBLE_PRESS_TIME_WINDOW_MS)
    }

    private fun onLongPressDetected() {
        if (isLongPressActive && volumeDownPressCount >= 1) {
            Log.i(TAG, "Emergency: Long volume down press detected (3 seconds)")
            triggerEmergency()
        }
    }

    private fun triggerEmergency() {
        resetPressCount()
        handler.removeCallbacks(longPressRunnable)
        isLongPressActive = false

        try {
            onEmergencyTriggered()
        } catch (e: Exception) {
            Log.e(TAG, "Error triggering emergency", e)
        }
    }

    private fun resetPressCount() {
        Log.d(TAG, "Resetting press count")
        volumeDownPressCount = 0
        handler.removeCallbacks(longPressRunnable)
        isLongPressActive = false
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        handler.removeCallbacks(resetCountRunnable)
        handler.removeCallbacks(longPressRunnable)
        isLongPressActive = false
        volumeDownPressCount = 0
    }
}
