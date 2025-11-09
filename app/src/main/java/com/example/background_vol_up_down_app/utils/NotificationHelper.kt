package com.example.background_vol_up_down_app.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.background_vol_up_down_app.MainActivity
import com.example.background_vol_up_down_app.R

object NotificationHelper {

    private const val EMERGENCY_CHANNEL_ID = "emergency_alerts"
    private const val EMERGENCY_CHANNEL_NAME = "تنبيهات الطوارئ"
    private const val EMERGENCY_NOTIFICATION_ID = 1001

    private const val JOURNEY_CHANNEL_ID = "safe_journey"
    private const val JOURNEY_CHANNEL_NAME = "الرحلة الآمنة"
    private const val JOURNEY_NOTIFICATION_ID = 1002

    private const val MONITORING_CHANNEL_ID = "monitoring_service"
    private const val MONITORING_CHANNEL_NAME = "خدمة المراقبة"
    private const val MONITORING_NOTIFICATION_ID = 1003

    private const val RECORDING_CHANNEL_ID = "recording_service"
    private const val RECORDING_CHANNEL_NAME = "التسجيل"
    private const val RECORDING_NOTIFICATION_ID = 1004

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Emergency Alerts Channel
            val emergencyChannel = NotificationChannel(
                EMERGENCY_CHANNEL_ID,
                EMERGENCY_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "إشعارات حالات الطوارئ"
                enableVibration(true)
                enableLights(true)
                setShowBadge(true)
            }

            // Safe Journey Channel
            val journeyChannel = NotificationChannel(
                JOURNEY_CHANNEL_ID,
                JOURNEY_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "تتبع الرحلة الآمنة"
                setShowBadge(true)
            }

            // Monitoring Service Channel
            val monitoringChannel = NotificationChannel(
                MONITORING_CHANNEL_ID,
                MONITORING_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "خدمة المراقبة في الخلفية"
                setShowBadge(false)
            }

            // Recording Service Channel
            val recordingChannel = NotificationChannel(
                RECORDING_CHANNEL_ID,
                RECORDING_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "التسجيل الصوتي والمرئي"
                enableVibration(false)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannel(emergencyChannel)
            notificationManager.createNotificationChannel(journeyChannel)
            notificationManager.createNotificationChannel(monitoringChannel)
            notificationManager.createNotificationChannel(recordingChannel)
        }
    }

    fun createEmergencyNotification(context: Context, message: String): Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, EMERGENCY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🚨 تنبيه طوارئ")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    fun createJourneyNotification(context: Context, destination: String): Notification {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, JOURNEY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("رحلة آمنة جارية")
            .setContentText("الوجهة: $destination")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    fun createMonitoringNotification(context: Context): Notification {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, MONITORING_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("HerSafe يعمل")
            .setContentText("حمايتك مفعلة في الخلفية")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    fun createRecordingNotification(context: Context, isRecording: Boolean): Notification {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val message = if (isRecording) "جاري التسجيل..." else "التسجيل متوقف"

        return NotificationCompat.Builder(context, RECORDING_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("🎥 تسجيل الطوارئ")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(isRecording)
            .setContentIntent(pendingIntent)
            .build()
    }

    fun showEmergencyNotification(context: Context, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = createEmergencyNotification(context, message)
        notificationManager.notify(EMERGENCY_NOTIFICATION_ID, notification)
    }

    fun showJourneyNotification(context: Context, destination: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = createJourneyNotification(context, destination)
        notificationManager.notify(JOURNEY_NOTIFICATION_ID, notification)
    }

    fun cancelEmergencyNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(EMERGENCY_NOTIFICATION_ID)
    }

    fun cancelJourneyNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(JOURNEY_NOTIFICATION_ID)
    }

    fun getMonitoringNotificationId() = MONITORING_NOTIFICATION_ID
    fun getJourneyNotificationId() = JOURNEY_NOTIFICATION_ID
    fun getRecordingNotificationId() = RECORDING_NOTIFICATION_ID
    fun getEmergencyNotificationId() = EMERGENCY_NOTIFICATION_ID
}
