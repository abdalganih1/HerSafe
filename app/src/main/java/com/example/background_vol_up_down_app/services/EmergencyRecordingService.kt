package com.example.background_vol_up_down_app.services

import android.app.Service
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ServiceCompat
import com.example.background_vol_up_down_app.utils.NotificationHelper
import com.example.background_vol_up_down_app.utils.PreferencesHelper
import java.io.File
import java.io.IOException

/**
 * EmergencyRecordingService
 *
 * Service for recording audio/video during emergencies.
 *
 * Features:
 * - Audio recording in background
 * - Video recording (requires camera permission and complex implementation)
 * - Live streaming (requires RTMP/WebRTC setup)
 *
 * For MVP, we implement audio recording. Video and live streaming can be added later.
 */
class EmergencyRecordingService : Service() {

    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var audioFilePath: String? = null
    private lateinit var preferencesHelper: PreferencesHelper

    companion object {
        private const val TAG = "EmergencyRecordingService"
        const val ACTION_START_RECORDING = "action_start_recording"
        const val ACTION_STOP_RECORDING = "action_stop_recording"
        const val EXTRA_RECORDING_TYPE = "recording_type"

        const val TYPE_AUDIO = "audio"
        const val TYPE_VIDEO = "video"
        const val TYPE_STREAM = "stream"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        preferencesHelper = PreferencesHelper(this)

        // Create notification channels
        NotificationHelper.createNotificationChannels(this)

        // Start as foreground service
        startForeground(
            NotificationHelper.getRecordingNotificationId(),
            NotificationHelper.createRecordingNotification(this, false)
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_RECORDING -> {
                val recordingType = intent.getStringExtra(EXTRA_RECORDING_TYPE) ?: TYPE_AUDIO
                startRecording(recordingType)
            }
            ACTION_STOP_RECORDING -> {
                stopRecording()
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
        stopRecording()
    }

    private fun startRecording(recordingType: String) {
        if (isRecording) {
            Log.w(TAG, "Already recording")
            return
        }

        when (recordingType) {
            TYPE_AUDIO -> startAudioRecording()
            TYPE_VIDEO -> startVideoRecording()
            TYPE_STREAM -> startLiveStream()
            else -> Log.e(TAG, "Unknown recording type: $recordingType")
        }
    }

    private fun startAudioRecording() {
        try {
            Log.d(TAG, "Starting audio recording")

            // Create file for recording
            val recordingsDir = File(getExternalFilesDir(null), "recordings")
            if (!recordingsDir.exists()) {
                recordingsDir.mkdirs()
            }

            val timestamp = System.currentTimeMillis()
            val audioFile = File(recordingsDir, "emergency_audio_$timestamp.m4a")
            audioFilePath = audioFile.absolutePath

            // Initialize MediaRecorder
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(this)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(audioFilePath)

                try {
                    prepare()
                    start()
                    isRecording = true
                    Log.d(TAG, "Audio recording started: $audioFilePath")

                    // Update notification
                    val notification = NotificationHelper.createRecordingNotification(this@EmergencyRecordingService, true)
                    startForeground(NotificationHelper.getRecordingNotificationId(), notification)

                } catch (e: IOException) {
                    Log.e(TAG, "Failed to start recording", e)
                    releaseMediaRecorder()
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error starting audio recording", e)
            releaseMediaRecorder()
        }
    }

    private fun startVideoRecording() {
        // TODO: Implement video recording
        // This is complex as it requires:
        // 1. Camera access in background (not always possible)
        // 2. SurfaceView or TextureView for preview
        // 3. CameraX or Camera2 API
        // 4. Handling different device capabilities
        Log.w(TAG, "Video recording not yet implemented")
    }

    private fun startLiveStream() {
        // TODO: Implement live streaming
        // This requires:
        // 1. RTMP server setup
        // 2. Streaming library (e.g., rtmp-rtsp-stream-client-java)
        // 3. Network handling
        // 4. Server infrastructure
        Log.w(TAG, "Live streaming not yet implemented")
    }

    private fun stopRecording() {
        if (!isRecording) {
            Log.w(TAG, "Not currently recording")
            return
        }

        try {
            Log.d(TAG, "Stopping recording")

            mediaRecorder?.apply {
                stop()
                reset()
            }

            isRecording = false
            Log.d(TAG, "Recording stopped. File saved at: $audioFilePath")

            // TODO: Save file path to emergency event in database
            // TODO: Upload file to server if configured

            releaseMediaRecorder()

            // Update notification
            val notification = NotificationHelper.createRecordingNotification(this, false)
            startForeground(NotificationHelper.getRecordingNotificationId(), notification)

            // Stop service after a delay
            android.os.Handler(mainLooper).postDelayed({
                ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
                stopSelf()
            }, 3000)

        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recording", e)
            releaseMediaRecorder()
        }
    }

    private fun releaseMediaRecorder() {
        mediaRecorder?.apply {
            try {
                reset()
                release()
            } catch (e: Exception) {
                Log.e(TAG, "Error releasing MediaRecorder", e)
            }
        }
        mediaRecorder = null
    }

    fun getAudioFilePath(): String? = audioFilePath
}
